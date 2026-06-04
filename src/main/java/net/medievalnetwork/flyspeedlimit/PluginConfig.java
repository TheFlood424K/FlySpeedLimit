package net.medievalnetwork.flyspeedlimit;

public final class PluginConfig {

    private final FlySpeedLimit plugin;
    private float maxFlySpeed;
    private float maxWalkSpeed;
    private boolean enforceOnJoin;
    private boolean blockPlaceRateLimit;

    public PluginConfig(FlySpeedLimit plugin) {
        this.plugin = plugin;
        load();
    }

    public void reload() {
        plugin.reloadConfig();
        load();
    }

    private void load() {
        maxFlySpeed       = clamp((float) plugin.getConfig().getDouble("max-fly-speed", 2.0));
        maxWalkSpeed      = clamp((float) plugin.getConfig().getDouble("max-walk-speed", 10.0));
        enforceOnJoin     = plugin.getConfig().getBoolean("enforce-on-join", true);
        blockPlaceRateLimit = plugin.getConfig().getBoolean("block-place-rate-limit", true);
    }

    private float clamp(float v) {
        return Math.max(0f, Math.min(10f, v));
    }

    /** EssentialsX 0-10 scale -> Minecraft native 0.0-1.0 */
    public float toNative(float ess) {
        return clamp(ess) / 10f;
    }

    public float getMaxFlySpeed()          { return maxFlySpeed; }
    public float getMaxWalkSpeed()         { return maxWalkSpeed; }
    public boolean isEnforceOnJoin()       { return enforceOnJoin; }
    public boolean isBlockPlaceRateLimit() { return blockPlaceRateLimit; }

    public String msg(String key) {
        String raw = plugin.getConfig().getString("messages." + key,
                "&eFlySpeedLimit: missing message key '" + key + "'");
        return raw.replace('&', '\u00a7');
    }
}
