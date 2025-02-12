package com.ubivismedia.spawnerplugin;

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

import java.util.*;

public class SpawnerPlugin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    // Map to hold all active spawners
    private final Map<Location, SpawnerData> spawners = new HashMap<>();
    private NamespacedKey spawnerKey;

    @Override
    public void onEnable() {
        getLogger().info("SpawnerPlugin has been enabled!");
        spawnerKey = new NamespacedKey(this, "custom_spawner");

        // Register events and command executor for /getspawner
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("getspawner").setExecutor(this);
        getCommand("getspawner").setTabCompleter(this);

        startSpawnerTask();
    }

    /**
     * Registers a new spawner at the specified location using the provided configuration.
     *
     * @param location       The location where the spawner is placed.
     * @param type           The entity type to spawn.
     * @param radius         The radius within which a player must be present.
     * @param activeLimit    Maximum number of entities allowed near the spawner.
     * @param interval       The number of ticks between spawn attempts.
     * @param spawnLimit     The maximum total spawns allowed over the spawner's lifetime (0 = unlimited).
     * @param destroyOnLimit If true, the spawner destroys itself once spawnLimit is reached.
     */
    public void addSpawner(Location location, EntityType type, int radius, int activeLimit, long interval, int spawnLimit, boolean destroyOnLimit) {
        spawners.put(location, new SpawnerData(type, radius, activeLimit, interval, spawnLimit, destroyOnLimit));
        getLogger().info("Spawner created at " + location.toString() + " for " + type.toString());
    }

    /**
     * This task runs every tick to process each spawner:
     * - Checks if the spawner has reached its lifetime spawn limit.
     * - Increments its internal tick counter and only spawns when the interval is met.
     * - Checks if any players are nearby.
     * - Ensures the active entities are below the active limit before spawning.
     */
    private void startSpawnerTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<Location, SpawnerData>> iter = spawners.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<Location, SpawnerData> entry = iter.next();
                    Location location = entry.getKey();
                    SpawnerData data = entry.getValue();
                    World world = location.getWorld();
                    if (world == null) continue;

                    // Check lifetime spawn limit
                    if (data.spawnLimit > 0 && data.spawnCount >= data.spawnLimit) {
                        if (data.destroyOnLimit) {
                            getLogger().info("Spawner at " + location.toString() + " destroyed after reaching spawn limit.");
                            iter.remove();
                        }
                        continue;
                    }

                    // Increment tick counter and only process when interval is reached
                    data.tickCounter++;
                    if (data.tickCounter < data.interval) continue;
                    data.tickCounter = 0;

                    // Check for a nearby player
                    boolean hasNearbyPlayer = world.getPlayers().stream()
                            .anyMatch(p -> p.getLocation().distance(location) < data.radius);
                    if (!hasNearbyPlayer) continue;

                    // Count active entities of the spawner's type near the spawner
                    long activeCount = world.getEntitiesByClass(data.type.getEntityClass()).stream()
                            .filter(e -> e.getLocation().distance(location) < data.radius)
                            .count();

                    if (activeCount < data.activeLimit) {
                        world.spawnEntity(location, data.type);
                        data.spawnCount++;
                    }
                }
            }
        }.runTaskTimer(this, 1L, 1L); // Runs every tick
    }

    /**
     * Command: /getspawner [entityType] [radius] [activeLimit] [interval] [spawnLimit] [destroyOnLimit]
     * Provides the admin with a custom spawner item (a Nether Star) that contains its configuration.
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

        // Default values
        EntityType type = EntityType.ZOMBIE;
        int radius = 20;
        int activeLimit = 10;
        long interval = 20L;
        int spawnLimit = 0; // 0 = unlimited
        boolean destroyOnLimit = false;

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
                activeLimit = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                player.sendMessage("Invalid activeLimit. Using default (10).");
            }
        }
        if (args.length >= 4) {
            try {
                interval = Long.parseLong(args[3]);
            } catch (NumberFormatException ex) {
                player.sendMessage("Invalid interval. Using default (20 ticks).");
            }
        }
        if (args.length >= 5) {
            try {
                spawnLimit = Integer.parseInt(args[4]);
            } catch (NumberFormatException ex) {
                player.sendMessage("Invalid spawnLimit. Using default (0 - unlimited).");
            }
        }
        if (args.length >= 6) {
            destroyOnLimit = Boolean.parseBoolean(args[5]);
        }

        // Create the custom spawner item (using a Nether Star)
        ItemStack spawnerItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = spawnerItem.getItemMeta();
        meta.setDisplayName("Custom Spawner");
        // Save configuration data: type, radius, activeLimit, interval, spawnLimit, destroyOnLimit
        String dataString = type.name() + "," + radius + "," + activeLimit + "," + interval + "," + spawnLimit + "," + destroyOnLimit;
        meta.getPersistentDataContainer().set(spawnerKey, PersistentDataType.STRING, dataString);
        List<String> lore = new ArrayList<>();
        lore.add("Entity: " + type.name());
        lore.add("Radius: " + radius);
        lore.add("Active Limit: " + activeLimit);
        lore.add("Interval: " + interval + " ticks");
        lore.add("Spawn Limit: " + (spawnLimit == 0 ? "Unlimited" : spawnLimit));
        lore.add("Destroy on Limit: " + destroyOnLimit);
        meta.setLore(lore);
        spawnerItem.setItemMeta(meta);

        player.getInventory().addItem(spawnerItem);
        player.sendMessage("Custom spawner item added to your inventory.");
        return true;
    }

    /**
     * Provides tab completion for the /getspawner command (suggests entity types).
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
     * Listens for an admin right-clicking a block with the custom spawner item.
     * When detected, reads the embedded configuration and places a new spawner at that location.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (!"Custom Spawner".equals(meta.getDisplayName())) return;
        if (!meta.getPersistentDataContainer().has(spawnerKey, PersistentDataType.STRING)) return;

        String dataStr = meta.getPersistentDataContainer().get(spawnerKey, PersistentDataType.STRING);
        if (dataStr == null) return;
        String[] parts = dataStr.split(",");
        if (parts.length < 6) return;

        EntityType type;
        int radius, activeLimit, spawnLimit;
        long interval;
        boolean destroyOnLimit;
        try {
            type = EntityType.valueOf(parts[0]);
            radius = Integer.parseInt(parts[1]);
            activeLimit = Integer.parseInt(parts[2]);
            interval = Long.parseLong(parts[3]);
            spawnLimit = Integer.parseInt(parts[4]);
            destroyOnLimit = Boolean.parseBoolean(parts[5]);
        } catch (Exception e) {
            player.sendMessage("Error reading spawner data. Spawner not placed.");
            return;
        }

        // Determine the placement location (one block above the clicked block)
        Location loc = event.getClickedBlock().getLocation().add(0, 1, 0);
        addSpawner(loc, type, radius, activeLimit, interval, spawnLimit, destroyOnLimit);
        player.sendMessage("Spawner placed at " + loc.toString() + ".");
        event.setCancelled(true);

        // Remove one spawner item from the player's hand
        int amount = item.getAmount();
        if (amount <= 1) {
            player.getInventory().removeItem(item);
        } else {
            item.setAmount(amount - 1);
        }
    }

    /**
     * Internal class to hold the spawner configuration and runtime counters.
     */
    static class SpawnerData {
        EntityType type;
        int radius;
        int activeLimit;
        long interval;
        int spawnLimit; // total spawns allowed (0 = unlimited)
        boolean destroyOnLimit;
        int spawnCount = 0;
        long tickCounter = 0;

        SpawnerData(EntityType type, int radius, int activeLimit, long interval, int spawnLimit, boolean destroyOnLimit) {
            this.type = type;
            this.radius = radius;
            this.activeLimit = activeLimit;
            this.interval = interval;
            this.spawnLimit = spawnLimit;
            this.destroyOnLimit = destroyOnLimit;
        }
    }
}
