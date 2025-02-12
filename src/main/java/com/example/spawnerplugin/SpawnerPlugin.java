package com.example.spawnerplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;

public class SpawnerPlugin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    // Holds all active spawners
    private final Map<Location, SpawnerData> spawners = new HashMap<>();
    private NamespacedKey spawnerKey;

    @Override
    public void onEnable() {
        getLogger().info("SpawnerPlugin has been enabled!");

        // Create a NamespacedKey for our custom spawner data
        spawnerKey = new NamespacedKey(this, "custom_spawner");

        // Register events and command executor for /getspawner
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("getspawner").setExecutor(this);
        getCommand("getspawner").setTabCompleter(this);

        startSpawnerTask();
    }

    /**
     * Adds a spawner at the specified location with the given configuration.
     *
     * @param location The location to place the spawner.
     * @param type     The entity type to spawn.
     * @param radius   The activation radius for players.
     * @param limit    The maximum number of entities allowed nearby.
     * @param interval The spawn interval in ticks.
     */
    public void addSpawner(Location location, EntityType type, int radius, int limit, long interval) {
        spawners.put(location, new SpawnerData(type, radius, limit, interval));
        getLogger().info("Spawner created at " + location.toString() + " for " + type.toString());
    }

    /**
     * This task runs every second (20 ticks) and processes all registered spawners.
     */
    private void startSpawnerTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Location, SpawnerData> entry : spawners.entrySet()) {
                    Location location = entry.getKey();
                    SpawnerData data = entry.getValue();
                    World world = location.getWorld();
                    
                    if (world == null) continue;

                    // Count the number of nearby entities of the given type
                    long entityCount = world.getEntitiesByClass(data.type.getEntityClass()).stream()
                            .filter(e -> e.getLocation().distance(location) < data.radius)
                            .count();
                    
                    // Check if any player is within the activation radius
                    boolean hasNearbyPlayer = world.getPlayers().stream()
                            .anyMatch(p -> p.getLocation().distance(location) < data.radius);

                    // Spawn the entity if conditions are met
                    if (hasNearbyPlayer && entityCount < data.limit) {
                        world.spawnEntity(location, data.type);
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    /**
     * Command: /getspawner [entityType] [radius] [limit] [interval]
     * Gives the player a custom spawner item (a Nether Star) with embedded configuration.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("spawnerplugin.getspawner")) {
            player.sendMessage("You don't have permission to use this command.");
            return true;
        }

        // Set default values
        EntityType type = EntityType.ZOMBIE;
        int radius = 20;
        int limit = 10;
        long interval = 20L;

        if (args.length >= 1) {
            try {
                type = EntityType.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException ex) {
                player.sendMessage("Invalid entity type. Using default (ZOMBIE).");
            }
        }
        if (args.length >= 2) {
            try {
                radius = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                player.sendMessage("Invalid radius. Using default (20).");
            }
        }
        if (args.length >= 3) {
            try {
                limit = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                player.sendMessage("Invalid limit. Using default (10).");
            }
        }
        if (args.length >= 4) {
            try {
                interval = Long.parseLong(args[3]);
            } catch (NumberFormatException ex) {
                player.sendMessage("Invalid interval. Using default (20 ticks).");
            }
        }

        // Create the custom spawner item (using a Nether Star)
        ItemStack spawnerItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = spawnerItem.getItemMeta();
        meta.setDisplayName("Custom Spawner");
        // Store the configuration in the PersistentDataContainer
        String dataString = type.name() + "," + radius + "," + limit + "," + interval;
        meta.getPersistentDataContainer().set(spawnerKey, PersistentDataType.STRING, dataString);
        // Set lore to show the configuration parameters
        meta.setLore(List.of("Entity: " + type.name(), "Radius: " + radius, "Limit: " + limit, "Interval: " + interval + " ticks"));
        spawnerItem.setItemMeta(meta);

        player.getInventory().addItem(spawnerItem);
        player.sendMessage("Custom spawner item added to your inventory.");
        return true;
    }

    /**
     * Provide tab completion for the /getspawner command (suggests entity types).
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (EntityType type : EntityType.values()) {
                suggestions.add(type.name());
            }
            return suggestions;
        }
        return Collections.emptyList();
    }

    /**
     * Listens for a player right-clicking a block with the custom spawner item.
     * When detected, places a spawner at the clicked location using the embedded configuration.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only process right-click on a block
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (!"Custom Spawner".equals(meta.getDisplayName())) {
            return;
        }
        if (!meta.getPersistentDataContainer().has(spawnerKey, PersistentDataType.STRING)) {
            return;
        }
        String dataStr = meta.getPersistentDataContainer().get(spawnerKey, PersistentDataType.STRING);
        if (dataStr == null) return;
        String[] parts = dataStr.split(",");
        if (parts.length < 4) return;

        EntityType type;
        int radius;
        int limit;
        long interval;
        try {
            type = EntityType.valueOf(parts[0]);
            radius = Integer.parseInt(parts[1]);
            limit = Integer.parseInt(parts[2]);
            interval = Long.parseLong(parts[3]);
        } catch (Exception e) {
            player.sendMessage("Error reading spawner data. Spawner not placed.");
            return;
        }

        // Determine spawn location (one block above the clicked block)
        Location loc = event.getClickedBlock().getLocation().add(0, 1, 0);

        addSpawner(loc, type, radius, limit, interval);
        player.sendMessage("Spawner placed at " + loc.toString() + ".");
        event.setCancelled(true); // Prevent further processing of the event

        // Remove one spawner item from the player's hand
        int amount = item.getAmount();
        if (amount <= 1) {
            player.getInventory().removeItem(item);
        } else {
            item.setAmount(amount - 1);
        }
    }

    /**
     * Internal class to hold spawner configuration data.
     */
    static class SpawnerData {
        EntityType type;
        int radius;
        int limit;
        long interval;

        SpawnerData(EntityType type, int radius, int limit, long interval) {
            this.type = type;
            this.radius = radius;
            this.limit = limit;
            this.interval = interval;
        }
    }
}
