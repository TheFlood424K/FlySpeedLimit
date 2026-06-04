package net.medievalnetwork.flyspeedlimit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Player;

public final class FlySpeedListener implements Listener {

    private final FlySpeedLimit plugin;

    public FlySpeedListener(FlySpeedLimit plugin) {
        this.plugin = plugin;
    }

    /** Clamp fly speed on join if enforce-on-join is enabled. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.cfg().isEnforceOnJoin()) return;
        Player player = event.getPlayer();
        enforceFlySpeed(player);
    }

    /** Real-time enforcement: catches EssentialsX or other plugins setting speed above the cap. */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isFlying()) return;
        enforceFlySpeed(player);
    }

    private void enforceFlySpeed(Player player) {
        if (player.hasPermission("flyspeedlimit.bypass")) return;

        float cap = resolveFlyCap(player);
        float nativeCap = plugin.cfg().toNative(cap);
        float current = player.getFlySpeed();

        if (current > nativeCap + 0.001f) {
            player.setFlySpeed(nativeCap);
            player.sendMessage(plugin.cfg().msg("speed-clamped")
                    .replace("{max}", fmt(cap)));
        }
    }

    private float resolveFlyCap(Player player) {
        for (int i = 10; i >= 1; i--) {
            if (player.hasPermission("flyspeedlimit.maxfly." + i)) return i;
        }
        return plugin.cfg().getMaxFlySpeed();
    }

    private String fmt(float v) {
        return (v == (int) v) ? String.valueOf((int) v) : String.valueOf(v);
    }
}
