package net.medievalnetwork.flyspeedlimit;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class FlySpeedLimit extends JavaPlugin {

    private PluginConfig cfg;

    @Override
    public void onEnable() {
        // Ensure the data folder exists before saving
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Copy config.yml from JAR to plugins/FlySpeedLimit/config.yml if absent
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }

        cfg = new PluginConfig(this);

        SpeedCommand speedCmd = new SpeedCommand(this);
        FlySpeedListener listener = new FlySpeedListener(this);

        // Wire every command declared in plugin.yml
        String[] speedCmds = {"speed", "flyspeed", "fspeed", "walkspeed", "wspeed"};
        for (String name : speedCmds) {
            PluginCommand cmd = getCommand(name);
            if (cmd != null) {
                cmd.setExecutor(speedCmd);
                cmd.setTabCompleter(speedCmd);
            } else {
                getLogger().warning("Could not find command '" + name + "' — check plugin.yml");
            }
        }

        String[] adminCmds = {"flyspeedlimit", "fsl"};
        for (String name : adminCmds) {
            PluginCommand cmd = getCommand(name);
            if (cmd != null) {
                cmd.setExecutor(new AdminCommand(this));
            } else {
                getLogger().warning("Could not find command '" + name + "' — check plugin.yml");
            }
        }

        getServer().getPluginManager().registerEvents(listener, this);

        getLogger().info("FlySpeedLimit enabled. Max fly: " + cfg.getMaxFlySpeed()
                + ", Max walk: " + cfg.getMaxWalkSpeed());
    }

    @Override
    public void onDisable() {
        getLogger().info("FlySpeedLimit disabled.");
    }

    public PluginConfig cfg() {
        return cfg;
    }
}
