package me.sunmc.particlelib.command;

import me.sunmc.particlelib.ParticleLib;
import me.sunmc.particlelib.config.ConfigManager;
import me.sunmc.particlelib.registry.EffectRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Handles the {@code /particlelib} (alias {@code /plib}) command.
 *
 * <p>Subcommands:</p>
 * <ul>
 *   <li>{@code info}      — prints version and active effect count</li>
 *   <li>{@code list}      — lists all registered effect IDs</li>
 *   <li>{@code play <id>} — plays an effect at the executing player's location</li>
 *   <li>{@code reload}    — reloads {@code config.yml} via {@link ConfigManager}</li>
 * </ul>
 *
 * <p>All subcommands require the {@code particlelib.admin} permission.
 * Tab-completion is provided for subcommand names and effect IDs.</p>
 */
public final class ParticleLibCommand implements CommandExecutor, TabCompleter {

    private static final String PREFIX = "§b[ParticleLib]§r ";

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("particlelib.admin")) {
            sender.sendMessage(PREFIX + "§cNo permission.");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "info" -> handleInfo(sender);
            case "list" -> handleList(sender);
            case "reload" -> handleReload(sender);
            case "play" -> handlePlay(sender, args);
            default -> {
                sendHelp(sender, label);
                yield true;
            }
        };
    }

    private boolean handleInfo(@NotNull CommandSender sender) {
        sender.sendMessage(PREFIX + "§7Version: §f"
                + ParticleLib.class.getPackage().getImplementationVersion());
        sender.sendMessage(PREFIX + "§7Registered effects: §f"
                + EffectRegistry.get().registeredIds().size());
        return true;
    }

    private boolean handleList(@NotNull CommandSender sender) {
        Set<String> ids = EffectRegistry.get().registeredIds();
        sender.sendMessage(PREFIX + "§7Effects (" + ids.size() + "):");
        // Print in rows of 5 for readability
        StringBuilder row = new StringBuilder("  §f");
        int i = 0;
        for (String id : ids) {
            row.append(id);
            if (++i % 5 == 0) {
                sender.sendMessage(row.toString());
                row = new StringBuilder("  §f");
            } else {
                row.append(", ");
            }
        }
        if (row.length() > 4) sender.sendMessage(row.toString());
        return true;
    }

    private boolean handleReload(@NotNull CommandSender sender) {
        Plugin plugin = ParticleLib.getInstance();
        if (plugin instanceof JavaPlugin jp) {
            ConfigManager.get().reload(jp);
            sender.sendMessage(PREFIX + "§aConfig reloaded.");
        } else {
            sender.sendMessage(PREFIX + "§cReload not available in shaded mode.");
        }
        return true;
    }

    private boolean handlePlay(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PREFIX + "§cOnly players can use /plib play.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(PREFIX + "§cUsage: /plib play <effectId>");
            return true;
        }
        String id = args[1].toLowerCase();
        if (!EffectRegistry.get().contains(id)) {
            sender.sendMessage(PREFIX + "§cUnknown effect: §f" + id);
            return true;
        }
        ParticleLib.api().play(id, player.getLocation());
        sender.sendMessage(PREFIX + "§aPlaying §f" + id + "§a at your location.");
        return true;
    }

    private void sendHelp(@NotNull CommandSender sender, @NotNull String label) {
        sender.sendMessage(PREFIX + "§7Commands:");
        sender.sendMessage("  §f/" + label + " info        §7— version & stats");
        sender.sendMessage("  §f/" + label + " list        §7— list registered effects");
        sender.sendMessage("  §f/" + label + " play <id>   §7— play effect at your location");
        sender.sendMessage("  §f/" + label + " reload      §7— reload config");
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender,
                                               @NotNull Command command,
                                               @NotNull String alias,
                                               @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("particlelib.admin")) return List.of();
        if (args.length == 1) {
            return Stream.of("info", "list", "play", "reload")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("play")) {
            return EffectRegistry.get().registeredIds().stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .sorted()
                    .toList();
        }
        return List.of();
    }
}