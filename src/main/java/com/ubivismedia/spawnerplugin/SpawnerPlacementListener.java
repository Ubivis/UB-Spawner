package com.ubivismedia.spawnerplugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class SpawnerPlacementListener implements Listener {
    private final SpawnerPlugin plugin;

    public SpawnerPlacementListener(SpawnerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "spawner_data");

        if (!data.has(key, PersistentDataType.STRING)) return;

        String spawnerData = data.get(key, PersistentDataType.STRING);
        if (spawnerData == null) return;

        String[] parts = spawnerData.split(",");
        if (parts.length < 6) return;

        String entityType = parts[0];
        int radius = Integer.parseInt(parts[1]);
        int limit = Integer.parseInt(parts[2]);
        long interval = Long.parseLong(parts[3]);
        int spawnLimit = Integer.parseInt(parts[4]);
        boolean destroyOnLimit = Boolean.parseBoolean(parts[5]);

        Block block = event.getBlockPlaced();
        if (block.getType() == Material.SPAWNER) {
            CreatureSpawner spawner = (CreatureSpawner) block.getState();
            spawner.setSpawnedType(EntityType.valueOf(entityType));
            spawner.update();
        }
    }
}
