package net.medievalnetwork.flyspeedlimit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class FlySpeedListener implements Listener {

    private final FlySpeedLimit plugin;
    private final BlockPlaceRateLimiter rateLimiter;

    public FlySpeedListener(FlySpeedLimit plugin, BlockPlaceRateLimiter rateLimiter) {
        this.plugin = plugin;
        this.rateLimiter = rateLimiter;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.cfg().isEnforceOnJoin()) return;
        clampFly(event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isFlying()) return;
        clampFly(player, true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        rateLimiter.remove(event.getPlayer().getUniqueId());
    }

    private void clampFly(Player player, boolean notify) {
        if (player.hasPermission("flyspeedlimit.bypass")) return;
        float max = plugin.cfg().toNative(plugin.cfg().getMaxFlySpeed());
        if (player.getFlySpeed() > max) {
            player.setFlySpeed(max);
            if (notify) player.sendMessage(plugin.cfg().speedClampedMsg(plugin.cfg().getMaxFlySpeed()));
        }
    }
}
