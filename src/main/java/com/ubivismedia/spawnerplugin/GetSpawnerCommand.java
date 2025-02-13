package com.ubivismedia.spawnerplugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
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
        FileConfiguration config = plugin.getConfig();

        String entityType = config.getString("default_entityType", "ZOMBIE");
        int activationRadius = config.getInt("default_activationRadius", 10);
        int concurrentSpawns = config.getInt("default_concurrentSpawns", 5);
        int spawnInterval = config.getInt("default_spawnInterval", 10) * 20; // Convert seconds to ticks
        int maxSpawns = config.getInt("default_maxSpawns", 50);

        if (args.length >= 1) entityType = args[0].toUpperCase();
        if (args.length >= 2) activationRadius = Integer.parseInt(args[1]);
        if (args.length >= 3) concurrentSpawns = Integer.parseInt(args[2]);
        if (args.length >= 4) spawnInterval = Integer.parseInt(args[3]) * 20;
        if (args.length >= 5) maxSpawns = Integer.parseInt(args[4]);

        try {
            EntityType.valueOf(entityType);
        } catch (IllegalArgumentException e) {
            player.sendMessage(languageManager.getMessage("getspawner_invalid_entity"));
            return true;
        }

        ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
        ItemMeta meta = spawnerItem.getItemMeta();
        if (meta == null) return false;

        meta.setDisplayName(languageManager.getMessage("spawner_item_name"));

        NamespacedKey key = new NamespacedKey(plugin, "spawner_data");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, entityType + "," + activationRadius + "," + concurrentSpawns + "," + spawnInterval + "," + maxSpawns);

        List<String> lore = new ArrayList<>();
        lore.add(languageManager.getMessage("spawner_lore_entity").replace("{entity}", entityType));
        lore.add(languageManager.getMessage("spawner_lore_radius").replace("{radius}", String.valueOf(activationRadius)));
        lore.add(languageManager.getMessage("spawner_lore_concurrent").replace("{concurrent}", String.valueOf(concurrentSpawns)));
        lore.add(languageManager.getMessage("spawner_lore_interval").replace("{interval}", String.valueOf(spawnInterval / 20)));
        lore.add(languageManager.getMessage("spawner_lore_maxspawns").replace("{maxspawns}", String.valueOf(maxSpawns)));
        meta.setLore(lore);

        spawnerItem.setItemMeta(meta);
        player.getInventory().addItem(spawnerItem);
        player.sendMessage(languageManager.getMessage("spawner_received"));

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
            return List.of("<activationRadius>");
        } else if (args.length == 3) {
            return List.of("<concurrentSpawns>");
        } else if (args.length == 4) {
            return List.of("<spawnInterval (seconds)>");
        } else if (args.length == 5) {
            return List.of("<maxSpawns>");
        }
        return new ArrayList<>();
    }
}
