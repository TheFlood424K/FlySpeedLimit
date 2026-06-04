package net.medievalnetwork.flyspeedlimit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class FlySpeedLimit extends JavaPlugin {

    private PluginConfig pluginConfig;
    private BlockPlaceRateLimiter rateLimiter;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        pluginConfig = new PluginConfig(this);
        rateLimiter = new BlockPlaceRateLimiter(this);

        getServer().getPluginManager().registerEvents(new FlySpeedListener(this, rateLimiter), this);
        getServer().getPluginManager().registerEvents(rateLimiter, this);

        SpeedCommand speedCommand = new SpeedCommand(this);
        if (getCommand("speed") != null) {
            getCommand("speed").setExecutor(speedCommand);
            getCommand("speed").setTabCompleter(speedCommand);
        }
        if (getCommand("flyspeedlimit") != null) {
            getCommand("flyspeedlimit").setExecutor(this);
        }

        getLogger().info("FlySpeedLimit enabled. Max fly speed: " + pluginConfig.getMaxFlySpeed()
                + ", Max walk speed: " + pluginConfig.getMaxWalkSpeed());
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
