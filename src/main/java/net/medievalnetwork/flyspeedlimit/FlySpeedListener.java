package net.medievalnetwork.flyspeedlimit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class FlySpeedListener implements Listener {

    private final FlySpeedLimit plugin;

    public FlySpeedListener(FlySpeedLimit plugin) {
        this.plugin = plugin;
    }

    /**
     * Clamp fly speed when a player joins.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        PluginConfig cfg = plugin.getPluginConfig2();
        if (!cfg.isEnforceOnJoin()) return;

        Player player = event.getPlayer();
        if (player.hasPermission("flyspeedlimit.bypass")) return;

        clampIfNeeded(player);
    }

    /**
     * Clamp fly speed whenever the player moves (catches /speed fly commands in real time).
     * LOWEST priority so we run after EssentialsX sets the speed.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isFlying()) return;
        if (player.hasPermission("flyspeedlimit.bypass")) return;

        clampIfNeeded(player);
    }

    // -------------------------------------------------------------------------

    private void clampIfNeeded(Player player) {
        PluginConfig cfg = plugin.getPluginConfig2();
        float current = player.getFlySpeed();
        float max = cfg.getMaxFlySpeedNative();

        if (current > max) {
            player.setFlySpeed(max);
            player.sendMessage(cfg.getSpeedClampedMessage(cfg.getMaxFlySpeed()));
        }
    }
}
