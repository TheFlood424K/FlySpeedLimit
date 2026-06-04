package net.medievalnetwork.flyspeedlimit;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles /speed, /flyspeed, /fspeed, /walkspeed, /wspeed.
 *
 * Permission model:
 *   flyspeedlimit.speed       → implies both .speed.fly and .speed.walk (parent node)
 *   flyspeedlimit.speed.fly   → gates /flyspeed, /fspeed, and /speed fly
 *   flyspeedlimit.speed.walk  → gates /walkspeed, /wspeed, and /speed walk
 *   flyspeedlimit.speed.others→ allows targeting another player
 *
 * Per-player max speed:
 *   flyspeedlimit.maxfly.<n>  → raises the effective fly cap to n for this player
 *   flyspeedlimit.maxwalk.<n> → raises the effective walk cap to n for this player
 *   The highest matching permission node wins; falls back to config value.
 *
 * Aliases are routed here by examining the command label:
 *   /flyspeed | /fspeed   → treated as "/speed fly <args>"
 *   /walkspeed | /wspeed  → treated as "/speed walk <args>"
 */
public class SpeedCommand implements CommandExecutor, TabCompleter {

    private final FlySpeedLimit plugin;

    public SpeedCommand(FlySpeedLimit plugin) {
        this.plugin = plugin;
    }

    // -------------------------------------------------------------------------
    // Command dispatch
    // -------------------------------------------------------------------------

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        String lbl = label.toLowerCase();

        // Fly-only aliases: /flyspeed <n> [player], /fspeed <n> [player]
        if (lbl.equals("flyspeed") || lbl.equals("fspeed")) {
            return handleDirect(sender, "fly", args);
        }

        // Walk-only aliases: /walkspeed <n> [player], /wspeed <n> [player]
        if (lbl.equals("walkspeed") || lbl.equals("wspeed")) {
            return handleDirect(sender, "walk", args);
        }

        // /speed <fly|walk> <n> [player]
        if (args.length < 2 || args.length > 3) {
            // Show only the sub-commands the sender is allowed to use
            sender.sendMessage(buildUsage(sender));
            return true;
        }

        String type = args[0].toLowerCase();
        String[] rest = Arrays.copyOfRange(args, 1, args.length); // <n> [player]
        return handleDirect(sender, type, rest);
    }

    /**
     * Core handler once the type ("fly" or "walk") is resolved.
     *
     * @param type  "fly" or "walk"
     * @param args  [<speed>] or [<speed>, <player>]
     */
    private boolean handleDirect(CommandSender sender, String type, String[] args) {
        boolean isFly  = type.equals("fly")  || type.equals("f")
                      || type.equals("flyspeed") || type.equals("fspeed");
        boolean isWalk = type.equals("walk") || type.equals("w")
                      || type.equals("walkspeed") || type.equals("wspeed");

        if (!isFly && !isWalk) {
            sender.sendMessage(buildUsage(sender));
            return true;
        }

        // Permission check for the requested sub-type
        String requiredPerm = isFly ? "flyspeedlimit.speed.fly" : "flyspeedlimit.speed.walk";
        if (!sender.hasPermission(requiredPerm)) {
            sender.sendMessage(plugin.cfg().msg("no-permission"));
            return true;
        }

        if (args.length < 1 || args.length > 2) {
            sender.sendMessage(buildUsage(sender));
            return true;
        }

        Float speed = parseSpeed(args[0]);
        if (speed == null) {
            sender.sendMessage(plugin.cfg().msg("invalid-number"));
            return true;
        }

        Player target;
        if (args.length == 2) {
            if (!sender.hasPermission("flyspeedlimit.speed.others")) {
                sender.sendMessage(plugin.cfg().msg("no-permission"));
                return true;
            }
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.cfg().msg("player-not-found"));
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(buildUsage(sender));
                return true;
            }
            target = (Player) sender;
        }

        if (isFly)  applyFly(sender, target, speed);
        if (isWalk) applyWalk(sender, target, speed);
        return true;
    }

    // -------------------------------------------------------------------------
    // Speed application
    // -------------------------------------------------------------------------

    private void applyFly(CommandSender sender, Player target, float requested) {
        float cap = effectiveCap(target, "fly");
        float allowed = Math.min(requested, cap);
        target.setFlySpeed(plugin.cfg().toNative(allowed));
        String s = fmt(allowed);
        if (sender.equals(target)) {
            sender.sendMessage(plugin.cfg().msg("fly-set-self").replace("{speed}", s));
        } else {
            sender.sendMessage(plugin.cfg().msg("fly-set-other")
                    .replace("{player}", target.getName()).replace("{speed}", s));
        }
    }

    private void applyWalk(CommandSender sender, Player target, float requested) {
        float cap = effectiveCap(target, "walk");
        float allowed = Math.min(requested, cap);
        target.setWalkSpeed(plugin.cfg().toNative(allowed));
        String s = fmt(allowed);
        if (sender.equals(target)) {
            sender.sendMessage(plugin.cfg().msg("walk-set-self").replace("{speed}", s));
        } else {
            sender.sendMessage(plugin.cfg().msg("walk-set-other")
                    .replace("{player}", target.getName()).replace("{speed}", s));
        }
    }

    // -------------------------------------------------------------------------
    // Effective cap resolution
    // -------------------------------------------------------------------------

    /**
     * Returns the effective max speed for a player.
     *
     * Resolution order (highest wins):
     *   1. flyspeedlimit.bypass          → 10 (no cap)
     *   2. flyspeedlimit.maxfly.<n>      → highest n the player has
     *   3. config value                  → fallback
     *
     * @param player the player whose permissions are checked
     * @param type   "fly" or "walk"
     */
    float effectiveCap(Player player, String type) {
        if (player.hasPermission("flyspeedlimit.bypass")) return 10.0f;

        String prefix = "flyspeedlimit.max" + type + ".";
        float best = -1;
        for (int i = 1; i <= 10; i++) {
            if (player.hasPermission(prefix + i)) {
                best = i;
            }
        }
        if (best >= 0) return best;

        return type.equals("fly")
                ? plugin.cfg().getMaxFlySpeed()
                : plugin.cfg().getMaxWalkSpeed();
    }

    // -------------------------------------------------------------------------
    // Tab completion
    // -------------------------------------------------------------------------

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        String lbl = alias.toLowerCase();
        boolean isFlyAlias  = lbl.equals("flyspeed")  || lbl.equals("fspeed");
        boolean isWalkAlias = lbl.equals("walkspeed") || lbl.equals("wspeed");

        // /flyspeed <n> [player]  or  /walkspeed <n> [player]
        if (isFlyAlias || isWalkAlias) {
            if (args.length == 1) return speedSuggestions(args[0]);
            if (args.length == 2 && sender.hasPermission("flyspeedlimit.speed.others"))
                return onlinePlayers(args[1]);
            return List.of();
        }

        // /speed <type> <n> [player]
        if (args.length == 1) {
            List<String> types = new ArrayList<>();
            if (sender.hasPermission("flyspeedlimit.speed.fly"))  types.add("fly");
            if (sender.hasPermission("flyspeedlimit.speed.walk")) types.add("walk");
            return partial(args[0], types);
        }
        if (args.length == 2) return speedSuggestions(args[1]);
        if (args.length == 3 && sender.hasPermission("flyspeedlimit.speed.others"))
            return onlinePlayers(args[2]);
        return List.of();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Float parseSpeed(String input) {
        try {
            float v = Float.parseFloat(input);
            return (v >= 0 && v <= 10) ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String fmt(float value) {
        return (value == (int) value) ? String.valueOf((int) value) : String.valueOf(value);
    }

    private String buildUsage(CommandSender sender) {
        boolean canFly  = sender.hasPermission("flyspeedlimit.speed.fly");
        boolean canWalk = sender.hasPermission("flyspeedlimit.speed.walk");
        if (canFly && canWalk) return plugin.cfg().msg("usage-speed");
        if (canFly)            return plugin.cfg().msg("usage-flyspeed");
        if (canWalk)           return plugin.cfg().msg("usage-walkspeed");
        return plugin.cfg().msg("no-permission");
    }

    private List<String> speedSuggestions(String token) {
        return partial(token, Arrays.asList("0","1","2","3","4","5","6","7","8","9","10"));
    }

    private List<String> onlinePlayers(String token) {
        List<String> names = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
        return partial(token, names);
    }

    private List<String> partial(String token, List<String> source) {
        String lower = token.toLowerCase();
        return source.stream().filter(s -> s.toLowerCase().startsWith(lower)).toList();
    }
}
