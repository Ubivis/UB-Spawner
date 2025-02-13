package com.ubivismedia.spawnerplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Iterator;
import java.util.Map;

public class SpawnerActivationTask implements Runnable {
    private final SpawnerPlugin plugin;
    private final SpawnerManager spawnerManager;

    public SpawnerActivationTask(SpawnerPlugin plugin, SpawnerManager spawnerManager) {
        this.plugin = plugin;
        this.spawnerManager = spawnerManager;
    }

    @Override
    public void run() {
        Iterator<Map.Entry<Location, Spawner>> iterator = spawnerManager.getSpawners().entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Location, Spawner> entry = iterator.next();
            Location location = entry.getKey();
            Spawner spawner = entry.getValue();

            boolean playerNearby = location.getWorld().getPlayers().stream()
                    .anyMatch(player -> player.getLocation().distance(location) <= spawner.getRadius());

            if (playerNearby && spawner.canSpawn()) {
                EntityType entityType = EntityType.valueOf(spawner.getEntityType());
                location.getWorld().spawnEntity(location, entityType);
                spawner.incrementSpawnCount();

                if (spawner.hasReachedSpawnLimit()) {
                    iterator.remove();

                    Entity armorStand = location.getWorld().getNearbyEntities(location, 1, 2, 1).stream()
                            .filter(entity -> entity.getUniqueId().equals(spawner.getArmorStandUUID()))
                            .findFirst()
                            .orElse(null);
                    if (armorStand != null) {
                        armorStand.remove();
                    }

                    Bukkit.getLogger().info("Spawner at " + location + " removed after reaching max spawns.");
                }
            }
        }
    }
}
