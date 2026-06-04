package net.medievalnetwork.flyspeedlimit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class FlySpeedLimit extends JavaPlugin {

    private PluginConfig pluginConfig;
    private FlySpeedListener listener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        pluginConfig = new PluginConfig(this);
        listener = new FlySpeedListener(this);
        getServer().getPluginManager().registerEvents(listener, this);
        getLogger().info("FlySpeedLimit enabled. Max fly speed: " + pluginConfig.getMaxFlySpeed());
    }

    @Override
    public void onDisable() {
        getLogger().info("FlySpeedLimit disabled.");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("flyspeedlimit")) return false;
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("flyspeedlimit.admin")) {
                sender.sendMessage("\u00a7cYou do not have permission to do that.");
                return true;
            }
            reloadConfig();
            pluginConfig.reload();
            sender.sendMessage(pluginConfig.getMessage("reload-success"));
            return true;
        }
        sender.sendMessage("\u00a7eUsage: /flyspeedlimit reload");
        return true;
    }

    public PluginConfig getPluginConfig2() {
        return pluginConfig;
    }
}
