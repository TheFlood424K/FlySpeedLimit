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
    private final SpeedCommand speedCommand;

    public FlySpeedListener(FlySpeedLimit plugin, BlockPlaceRateLimiter rateLimiter,
                             SpeedCommand speedCommand) {
        this.plugin = plugin;
        this.rateLimiter = rateLimiter;
        this.speedCommand = speedCommand;
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
        // Use the same cap resolution as SpeedCommand so permission overrides are respected
        float max = plugin.cfg().toNative(speedCommand.effectiveCap(player, "fly"));
        if (player.getFlySpeed() > max) {
            player.setFlySpeed(max);
            if (notify) player.sendMessage(
                    plugin.cfg().speedClampedMsg(speedCommand.effectiveCap(player, "fly")));
        }
    }
}
