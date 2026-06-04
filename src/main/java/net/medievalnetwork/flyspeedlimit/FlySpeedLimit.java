package net.medievalnetwork.flyspeedlimit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class FlySpeedLimit extends JavaPlugin {

    private PluginConfig pluginConfig;
    private BlockPlaceRateLimiter rateLimiter;
    private SpeedCommand speedCommand;

    @Override
    public void onEnable() {
        // Save config.yml from the JAR to the data folder if it doesn't exist yet.
        // saveDefaultConfig() relies on the resource being present in the JAR;
        // with filtering=false on config.yml it is guaranteed to be there intact.
        saveDefaultConfig();

        pluginConfig = new PluginConfig(this);
        rateLimiter  = new BlockPlaceRateLimiter(this);
        speedCommand = new SpeedCommand(this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(
                new FlySpeedListener(this, rateLimiter, speedCommand), this);
        getServer().getPluginManager().registerEvents(rateLimiter, this);

        // Wire every command to its executor.
        // All permission checks are done inside the executor — no 'permission:'
        // field in plugin.yml so Paper never pre-filters by permission node.
        wireCommand("speed",         speedCommand, speedCommand);
        wireCommand("flyspeed",      speedCommand, speedCommand);
        wireCommand("walkspeed",     speedCommand, speedCommand);
        // plugin.yml aliases (fspeed, wspeed) resolve to the parent command,
        // so we don't need to wire them separately — Paper handles that.
        wireCommand("flyspeedlimit", this, null);

        getLogger().info("FlySpeedLimit enabled. Max fly: " + pluginConfig.getMaxFlySpeed()
                + ", Max walk: " + pluginConfig.getMaxWalkSpeed());
    }

    @Override
    public void onDisable() {
        getLogger().info("FlySpeedLimit disabled.");
    }

    /** Null-safe helper to wire executor + optional tab completer. */
    private void wireCommand(String name,
                             org.bukkit.command.CommandExecutor executor,
                             org.bukkit.command.TabCompleter completer) {
        PluginCommand cmd = getCommand(name);
        if (cmd == null) {
            getLogger().warning("Command '" + name + "' not found in plugin.yml — skipping.");
            return;
        }
        cmd.setExecutor(executor);
        if (completer != null) cmd.setTabCompleter(completer);
    }

    // -------------------------------------------------------------------------
    // /flyspeedlimit reload
    // -------------------------------------------------------------------------

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
