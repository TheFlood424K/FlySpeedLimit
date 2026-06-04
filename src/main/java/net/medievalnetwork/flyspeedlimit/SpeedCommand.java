package net.medievalnetwork.flyspeedlimit;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SpeedCommand implements CommandExecutor, TabCompleter {

    private final FlySpeedLimit plugin;

    public SpeedCommand(FlySpeedLimit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String lbl = label.toLowerCase();

        switch (lbl) {
            case "flyspeed", "fspeed" -> { return handle(sender, "fly", args); }
            case "walkspeed", "wspeed" -> { return handle(sender, "walk", args); }
            default -> {
                // /speed <fly|walk> <n> [player]
                if (args.length < 2) {
                    sender.sendMessage(plugin.cfg().msg("usage-speed"));
                    return true;
                }
                return handle(sender, args[0], Arrays.copyOfRange(args, 1, args.length));
            }
        }
    }

    /**
     * @param type "fly" or "walk"
     * @param args [speed] or [speed, player]
     */
    private boolean handle(CommandSender sender, String type, String[] args) {
        boolean isFly = type.equalsIgnoreCase("fly") || type.equalsIgnoreCase("f");
        boolean isWalk = type.equalsIgnoreCase("walk") || type.equalsIgnoreCase("w");

        if (!isFly && !isWalk) {
            sender.sendMessage(plugin.cfg().msg("usage-speed"));
            return true;
        }

        String perm = isFly ? "flyspeedlimit.speed.fly" : "flyspeedlimit.speed.walk";
        if (!sender.hasPermission(perm)) {
            sender.sendMessage(plugin.cfg().msg("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(isFly
                    ? plugin.cfg().msg("usage-flyspeed")
                    : plugin.cfg().msg("usage-walkspeed"));
            return true;
        }

        Float speed = parseSpeed(args[0]);
        if (speed == null) {
            sender.sendMessage(plugin.cfg().msg("invalid-number"));
            return true;
        }

        Player target;
        if (args.length >= 2) {
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
            if (!(sender instanceof Player p)) {
                sender.sendMessage("&cYou must specify a player from console.");
                return true;
            }
            target = p;
        }

        if (isFly)  applyFly(sender, target, speed);
        if (isWalk) applyWalk(sender, target, speed);
        return true;
    }

    private void applyFly(CommandSender sender, Player target, float requested) {
        float cap = effectiveCap(target, "fly");
        float clamped = Math.min(requested, cap);

        if (clamped < requested) {
            target.sendMessage(plugin.cfg().msg("speed-clamped")
                    .replace("{max}", fmt(cap)));
        }

        target.setFlySpeed(plugin.cfg().toNative(clamped));
        String s = fmt(clamped);
        if (sender.equals(target)) {
            sender.sendMessage(plugin.cfg().msg("fly-set-self").replace("{speed}", s));
        } else {
            sender.sendMessage(plugin.cfg().msg("fly-set-other")
                    .replace("{player}", target.getName()).replace("{speed}", s));
            target.sendMessage(plugin.cfg().msg("fly-set-self").replace("{speed}", s));
        }
    }

    private void applyWalk(CommandSender sender, Player target, float requested) {
        float cap = effectiveCap(target, "walk");
        float clamped = Math.min(requested, cap);
        target.setWalkSpeed(plugin.cfg().toNative(clamped));
        String s = fmt(clamped);
        if (sender.equals(target)) {
            sender.sendMessage(plugin.cfg().msg("walk-set-self").replace("{speed}", s));
        } else {
            sender.sendMessage(plugin.cfg().msg("walk-set-other")
                    .replace("{player}", target.getName()).replace("{speed}", s));
            target.sendMessage(plugin.cfg().msg("walk-set-self").replace("{speed}", s));
        }
    }

    float effectiveCap(Player player, String type) {
        if (player.hasPermission("flyspeedlimit.bypass")) return 10f;
        String prefix = "flyspeedlimit.max" + type + ".";
        for (int i = 10; i >= 1; i--) {
            if (player.hasPermission(prefix + i)) return i;
        }
        return type.equals("fly")
                ? plugin.cfg().getMaxFlySpeed()
                : plugin.cfg().getMaxWalkSpeed();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        String lbl = alias.toLowerCase();
        List<String> speeds = Arrays.asList("1","2","3","4","5","6","7","8","9","10");

        if (lbl.equals("flyspeed") || lbl.equals("fspeed")) {
            if (args.length == 1) return filter(args[0], speeds);
            if (args.length == 2 && sender.hasPermission("flyspeedlimit.speed.others"))
                return onlinePlayers(args[1]);
            return List.of();
        }
        if (lbl.equals("walkspeed") || lbl.equals("wspeed")) {
            if (args.length == 1) return filter(args[0], speeds);
            if (args.length == 2 && sender.hasPermission("flyspeedlimit.speed.others"))
                return onlinePlayers(args[1]);
            return List.of();
        }
        // /speed
        if (args.length == 1) {
            List<String> types = new ArrayList<>();
            if (sender.hasPermission("flyspeedlimit.speed.fly"))  types.add("fly");
            if (sender.hasPermission("flyspeedlimit.speed.walk")) types.add("walk");
            return filter(args[0], types);
        }
        if (args.length == 2) return filter(args[1], speeds);
        if (args.length == 3 && sender.hasPermission("flyspeedlimit.speed.others"))
            return onlinePlayers(args[2]);
        return List.of();
    }

    private Float parseSpeed(String s) {
        try {
            float v = Float.parseFloat(s);
            return (v >= 0 && v <= 10) ? v : null;
        } catch (NumberFormatException e) { return null; }
    }

    private String fmt(float v) {
        return (v == (int) v) ? String.valueOf((int) v) : String.valueOf(v);
    }

    private List<String> filter(String token, List<String> source) {
        String t = token.toLowerCase();
        return source.stream().filter(s -> s.toLowerCase().startsWith(t)).toList();
    }

    private List<String> onlinePlayers(String token) {
        List<String> names = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
        return filter(token, names);
    }
}
