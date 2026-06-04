package net.medievalnetwork.flyspeedlimit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Prevents players from gaining a block-placement speed advantage through high fly speed.
 *
 * Root cause: Minecraft's block placement rate is not independently throttled by the server.
 * Players flying at high speed send significantly more movement packets per second, which
 * incidentally allows them to place blocks faster than a player at default fly speed.
 * This listener enforces a per-player placement cooldown that scales proportionally
 * with the player's current fly speed, so faster fliers are held to the same
 * effective blocks-per-second rate as a player flying at the baseline speed.
 *
 * The cooldown is only applied while the player is flying and has an elevated fly speed.
 * Walking players and players at or below the baseline speed are unaffected.
 */
public class BlockPlaceRateLimiter implements Listener {

    /**
     * Minecraft's default creative fly speed on the EssentialsX 0-10 scale.
     * EssentialsX maps native 0.1 → scale 2 (native = scale / 10, default native = 0.1).
     * We treat anything at or below this as "baseline" — no cooldown needed.
     */
    private static final float BASELINE_SPEED = 1.0f;

    /**
     * Base interval in milliseconds between block placements at baseline fly speed.
     * Vanilla allows approximately 4 placements per second (250 ms) when not moving.
     * We use a slightly relaxed value to avoid false positives on laggy connections.
     */
    private static final long BASE_INTERVAL_MS = 250L;

    /** Last placement timestamp per player UUID. */
    private final Map<UUID, Long> lastPlace = new HashMap<>();

    private final FlySpeedLimit plugin;

    public BlockPlaceRateLimiter(FlySpeedLimit plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        // Only restrict while actively flying.
        if (!player.isFlying()) return;

        // Bypass permission skips the rate limiter entirely.
        if (player.hasPermission("flyspeedlimit.bypass")) return;

        // Convert native fly speed back to EssentialsX scale for readability.
        float essSpeed = player.getFlySpeed() * 10.0f;

        // No restriction for players at or below the baseline speed.
        if (essSpeed <= BASELINE_SPEED) return;

        // Scale the required cooldown proportionally to speed.
        // A player at 2× baseline speed must wait 2× as long between placements,
        // keeping their effective blocks-per-second identical to a baseline player.
        long requiredInterval = (long) (BASE_INTERVAL_MS * (essSpeed / BASELINE_SPEED));

        long now = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();
        long last = lastPlace.getOrDefault(uuid, 0L);

        if (now - last < requiredInterval) {
            event.setCancelled(true);
            // Send a block update so the client reverts the optimistic placement.
            player.sendBlockChange(
                    event.getBlock().getLocation(),
                    event.getBlock().getBlockData()
            );
            return;
        }

        lastPlace.put(uuid, now);
    }

    /** Clean up stored timestamps when a player quits to avoid memory leaks. */
    public void remove(UUID uuid) {
        lastPlace.remove(uuid);
    }
}
