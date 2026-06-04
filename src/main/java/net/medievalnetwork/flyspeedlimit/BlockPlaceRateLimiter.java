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
 * Prevents fastplace advantage that comes from elevated fly speed.
 * Players flying above baseline speed are given a proportional block-placement
 * cooldown so their blocks-per-second rate stays the same as a baseline flier.
 */
public final class BlockPlaceRateLimiter implements Listener {

    /** Baseline native fly speed (Minecraft default = 0.1 = EssentialsX speed 1). */
    private static final float BASELINE_NATIVE = 0.1f;
    /** Minimum ms between placements at baseline speed. */
    private static final long  BASE_INTERVAL_MS = 250L;

    private final FlySpeedLimit plugin;
    private final Map<UUID, Long> lastPlace = new HashMap<>();

    public BlockPlaceRateLimiter(FlySpeedLimit plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (!plugin.cfg().isBlockPlaceRateLimit()) return;

        Player player = event.getPlayer();
        if (!player.isFlying()) return;
        if (player.hasPermission("flyspeedlimit.bypass")) return;

        float native_ = player.getFlySpeed();
        if (native_ <= BASELINE_NATIVE + 0.001f) return; // at or below baseline, no throttle needed

        float ratio = native_ / BASELINE_NATIVE;
        long required = (long) (BASE_INTERVAL_MS * ratio);

        long now = System.currentTimeMillis();
        long last = lastPlace.getOrDefault(player.getUniqueId(), 0L);

        if (now - last < required) {
            event.setCancelled(true);
            return;
        }

        lastPlace.put(player.getUniqueId(), now);
    }
}
