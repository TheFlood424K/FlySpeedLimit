package net.medievalnetwork.flyspeedlimit;

public class PluginConfig {

    private final FlySpeedLimit plugin;
    private float maxFlySpeed;
    private float maxWalkSpeed;
    private boolean enforceOnJoin;

    public PluginConfig(FlySpeedLimit plugin) {
        this.plugin = plugin;
        load();
    }

    public void reload() {
        load();
    }

    private void load() {
        maxFlySpeed = clamp((float) plugin.getConfig().getDouble("max-fly-speed", 2.0));
        maxWalkSpeed = clamp((float) plugin.getConfig().getDouble("max-walk-speed", 10.0));
        enforceOnJoin = plugin.getConfig().getBoolean("enforce-on-join", true);
    }

    private float clamp(float value) {
        return Math.max(0.0f, Math.min(10.0f, value));
    }

    /**
     * Converts an EssentialsX-scale value (0-10) to Minecraft's native fly/walk speed (0.0-1.0).
     * EssentialsX formula: nativeSpeed = essSpeed / 10
     */
    public float toNative(float essentialsScale) {
        return clamp(essentialsScale) / 10.0f;
    }

    public float getMaxFlySpeed() { return maxFlySpeed; }
    public float getMaxWalkSpeed() { return maxWalkSpeed; }
    public boolean isEnforceOnJoin() { return enforceOnJoin; }

    public String msg(String key) {
        return plugin.getConfig()
                .getString("messages." + key, "&eFlySpeedLimit: unknown message '" + key + "'")
                .replace('&', '\u00a7');
    }

    public String speedClampedMsg(float max) {
        return msg("speed-clamped").replace("{max}", String.valueOf((int) max));
    }
}
