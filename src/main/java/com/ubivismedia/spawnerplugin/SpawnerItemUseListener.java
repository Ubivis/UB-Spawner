package com.ubivismedia.spawnerplugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class SpawnerItemUseListener implements Listener {
    private final SpawnerPlugin plugin;

    public SpawnerItemUseListener(SpawnerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawnerUse(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getClickedBlock() == null) {
            return;
        }

        ItemStack item = event.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !item.getType().equals(Material.SPAWNER)) {
            return;
        }

        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "spawner_data");

        if (!data.has(key, PersistentDataType.STRING)) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation().add(0.5, 0, 0.5);

        String[] parts = data.get(key, PersistentDataType.STRING).split(",");
        String entityType = parts[0];
        int activationRadius = Integer.parseInt(parts[1]);
        int concurrentSpawns = Integer.parseInt(parts[2]);
        int spawnInterval = Integer.parseInt(parts[3]);
        int maxSpawns = Integer.parseInt(parts[4]);

        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setMarker(true);
        stand.setCustomName("spawner:" + entityType);
        stand.setCustomNameVisible(false);
        stand.setGravity(false);

        UUID armorStandUUID = stand.getUniqueId();

        Spawner spawner = new Spawner(entityType, activationRadius, concurrentSpawns, spawnInterval, maxSpawns, armorStandUUID);
        plugin.getSpawnerManager().addSpawner(location, spawner);

        item.setAmount(item.getAmount() - 1);
        player.sendMessage(plugin.getLanguageManager().getMessage("spawner_placed").replace("{entity}", entityType));
    }
}
