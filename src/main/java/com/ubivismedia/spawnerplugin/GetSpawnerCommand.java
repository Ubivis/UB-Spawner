package com.ubivismedia.spawnerplugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GetSpawnerCommand implements CommandExecutor, TabCompleter {
    private final SpawnerPlugin plugin;
    private final LanguageManager languageManager;

    public GetSpawnerCommand(SpawnerPlugin plugin, LanguageManager languageManager) {
        this.plugin = plugin;
        this.languageManager = languageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(languageManager.getMessage("command_only_players"));
            return true;
        }

        Player player = (Player) sender;

        // Default values
        String entityType = "ZOMBIE";
        int radius = 20;
        int limit = 10;
        long interval = 20L;
        int spawnLimit = 0; // 0 means unlimited spawns
        boolean destroyOnLimit = false;

        if (args.length >= 1) {
            if (Arrays.stream(EntityType.values()).map(Enum::name).collect(Collectors.toList()).contains(args[0].toUpperCase())) {
                entityType = args[0].toUpperCase();
            } else {
                player.sendMessage("Invalid entity type. Available options: " + Arrays.stream(EntityType.values()).map(Enum::name).collect(Collectors.joining(", ")));
                return true;
            }
        }
        if (args.length >= 2) {
            try { radius = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
        }
        if (args.length >= 3) {
            try { limit = Integer.parseInt(args[2]); } catch (NumberFormatException ignored) {}
        }
        if (args.length >= 4) {
            try { interval = Long.parseLong(args[3]); } catch (NumberFormatException ignored) {}
        }
        if (args.length >= 5) {
            try { spawnLimit = Integer.parseInt(args[4]); } catch (NumberFormatException ignored) {}
        }
        if (args.length >= 6) {
            destroyOnLimit = Boolean.parseBoolean(args[5]);
        }

        // Create spawner item
        ItemStack spawnerItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = spawnerItem.getItemMeta();
        if (meta == null) return false;

        meta.setDisplayName(languageManager.getMessage("spawner_item_name"));

        // Store data in the item
        NamespacedKey key = new NamespacedKey(plugin, "spawner_data");
        String spawnerData = entityType + "," + radius + "," + limit + "," + interval + "," + spawnLimit + "," + destroyOnLimit;
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, spawnerData);

        // Add lore for better visibility
        List<String> lore = new ArrayList<>();
        lore.add("Entity: " + entityType);
        lore.add("Radius: " + radius);
        lore.add("Limit: " + limit);
        lore.add("Interval: " + interval + " ticks");
        lore.add("Spawn Limit: " + (spawnLimit == 0 ? "Unlimited" : spawnLimit));
        lore.add("Destroy on Limit: " + destroyOnLimit);
        meta.setLore(lore);

        spawnerItem.setItemMeta(meta);

        player.getInventory().addItem(spawnerItem);
        player.sendMessage(languageManager.getMessage("spawner_received"));
        player.sendMessage("Usage: /getspawner <entity> <radius> <activeLimit> <interval> <spawnLimit> <destroyOnLimit>");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.stream(EntityType.values())
                    .map(Enum::name)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return List.of("10", "20", "30", "40", "50"); // Radius suggestions
        } else if (args.length == 3) {
            return List.of("5", "10", "15", "20", "25"); // Active entity limit suggestions
        } else if (args.length == 4) {
            return List.of("20", "40", "60", "80", "100"); // Spawn interval suggestions (ticks)
        } else if (args.length == 5) {
            return List.of("0", "10", "20", "50", "100"); // Total spawn limit suggestions
        } else if (args.length == 6) {
            return List.of("true", "false"); // Destroy on limit suggestions
        }
        return new ArrayList<>();
    }
}