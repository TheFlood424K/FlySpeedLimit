package net.medievalnetwork.flyspeedlimit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class FlySpeedLimit extends JavaPlugin {

    private PluginConfig pluginConfig;
    private BlockPlaceRateLimiter rateLimiter;
    private SpeedCommand speedCommand;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        pluginConfig = new PluginConfig(this);
        rateLimiter  = new BlockPlaceRateLimiter(this);
        speedCommand = new SpeedCommand(this);

        getServer().getPluginManager().registerEvents(
                new FlySpeedListener(this, rateLimiter, speedCommand), this);
        getServer().getPluginManager().registerEvents(rateLimiter, this);

        if (getCommand("speed") != null) {
            getCommand("speed").setExecutor(speedCommand);
            getCommand("speed").setTabCompleter(speedCommand);
        }
        if (getCommand("flyspeedlimit") != null) {
            getCommand("flyspeedlimit").setExecutor(this);
        }

        getLogger().info("FlySpeedLimit enabled. Max fly: " + pluginConfig.getMaxFlySpeed()
                + ", Max walk: " + pluginConfig.getMaxWalkSpeed());
    }

    @Override
    public void onDisable() {
        getLogger().info("FlySpeedLimit disabled.");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("flyspeedlimit")) return false;
        if (!sender.hasPermission("flyspeedlimit.admin")) {
            sender.sendMessage(pluginConfig.msg("no-permission"));
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            pluginConfig.reload();
            sender.sendMessage(pluginConfig.msg("reload-success"));
            return true;
        }
        sender.sendMessage("\u00a7eUsage: /flyspeedlimit reload");
        return true;
    }

    public PluginConfig cfg() {
        return pluginConfig;
    }
}
