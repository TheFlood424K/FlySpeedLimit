package net.medievalnetwork.flyspeedlimit;

public class PluginConfig {

    private final FlySpeedLimit plugin;

    // EssentialsX uses (value / 10) to convert its 0-10 scale to Minecraft's 0-1 float.
    // We store the Essentials-scale max (e.g. 2) and convert when applying.
    private float maxFlySpeed;
    private boolean enforceOnJoin;

    public PluginConfig(FlySpeedLimit plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        maxFlySpeed = (float) plugin.getConfig().getDouble("max-fly-speed", 2.0);
        enforceOnJoin = plugin.getConfig().getBoolean("enforce-on-join", true);

        // Clamp to Minecraft/EssentialsX valid range (0.0 - 10.0)
        if (maxFlySpeed < 0.0f) maxFlySpeed = 0.0f;
        if (maxFlySpeed > 10.0f) maxFlySpeed = 10.0f;
    }

    public void reload() {
        load();
    }

    /**
     * Returns the max fly speed in EssentialsX scale (0–10).
     */
    public float getMaxFlySpeed() {
        return maxFlySpeed;
    }

    /**
     * Returns the max fly speed in Minecraft's native float scale (0.0–1.0).
     * Minecraft default = 0.1f, which EssentialsX calls "1".
     * EssentialsX formula: mcSpeed = essSpeed / 10
     */
    public float getMaxFlySpeedNative() {
        return maxFlySpeed / 10.0f;
    }

    public boolean isEnforceOnJoin() {
        return enforceOnJoin;
    }

    public String getMessage(String key) {
        String raw = plugin.getConfig().getString("messages." + key, "&eFlySpeedLimit: unknown message '" + key + "'");
        return colorize(raw);
    }

    public String getSpeedClampedMessage(float max) {
        return getMessage("speed-clamped").replace("{max}", String.valueOf((int) max));
    }

    private String colorize(String input) {
        return input.replace('&', '\u00a7');
    }
}
