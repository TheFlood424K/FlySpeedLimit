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
 * Implements an EssentialsX-compatible /speed command:
 *   /speed <walk|fly> <0-10> [player]
 *
 * Also registered as aliases: /flyspeed /fspeed /walkspeed /wspeed
 *
 * To ensure this plugin wins over EssentialsX, add to plugins/Essentials/config.yml:
 *   disabled-commands:
 *     - speed
 */
public class SpeedCommand implements CommandExecutor, TabCompleter {

    private final FlySpeedLimit plugin;

    public SpeedCommand(FlySpeedLimit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("flyspeedlimit.speed")) {
            sender.sendMessage(plugin.cfg().msg("no-permission"));
            return true;
        }

        if (args.length < 2 || args.length > 3) {
            sender.sendMessage(plugin.cfg().msg("invalid-type"));
            return true;
        }

        String type = args[0].toLowerCase();
        Float speed = parseSpeed(args[1]);
        if (speed == null) {
            sender.sendMessage(plugin.cfg().msg("invalid-number"));
            return true;
        }

        Player target;
        if (args.length == 3) {
            if (!sender.hasPermission("flyspeedlimit.speed.others")) {
                sender.sendMessage(plugin.cfg().msg("no-permission"));
                return true;
            }
            target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage(plugin.cfg().msg("player-not-found"));
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.cfg().msg("invalid-type"));
                return true;
            }
            target = (Player) sender;
        }

        switch (type) {
            case "fly", "f", "flyspeed", "fspeed" -> applyFly(sender, target, speed);
            case "walk", "w", "walkspeed", "wspeed" -> applyWalk(sender, target, speed);
            default -> sender.sendMessage(plugin.cfg().msg("invalid-type"));
        }
        return true;
    }

    private void applyFly(CommandSender sender, Player target, float requested) {
        float allowed = target.hasPermission("flyspeedlimit.bypass")
                ? requested
                : Math.min(requested, plugin.cfg().getMaxFlySpeed());
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
        float allowed = target.hasPermission("flyspeedlimit.bypass")
                ? requested
                : Math.min(requested, plugin.cfg().getMaxWalkSpeed());
        target.setWalkSpeed(plugin.cfg().toNative(allowed));
        String s = fmt(allowed);
        if (sender.equals(target)) {
            sender.sendMessage(plugin.cfg().msg("walk-set-self").replace("{speed}", s));
        } else {
            sender.sendMessage(plugin.cfg().msg("walk-set-other")
                    .replace("{player}", target.getName()).replace("{speed}", s));
        }
    }

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

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return partial(args[0], Arrays.asList("fly", "walk"));
        if (args.length == 2) return partial(args[1],
                Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
        if (args.length == 3 && sender.hasPermission("flyspeedlimit.speed.others")) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
            return partial(args[2], names);
        }
        return List.of();
    }

    private List<String> partial(String token, List<String> source) {
        String lower = token.toLowerCase();
        return source.stream().filter(s -> s.toLowerCase().startsWith(lower)).toList();
    }
}
