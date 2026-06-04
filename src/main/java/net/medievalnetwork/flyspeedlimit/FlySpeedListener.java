package net.medievalnetwork.flyspeedlimit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class FlySpeedListener implements Listener {

    private final FlySpeedLimit plugin;

    public FlySpeedListener(FlySpeedLimit plugin) {
        this.plugin = plugin;
    }

    /**
     * Clamp on join to catch speeds set before the plugin was loaded.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.cfg().isEnforceOnJoin()) return;
        clampFly(event.getPlayer(), false);
    }

    /**
     * Clamp while flying — runs at MONITOR priority so it fires after EssentialsX
     * has already applied the speed, then corrects it if it's too high.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isFlying()) return;
        clampFly(player, true);
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
