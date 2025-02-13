package com.ubivismedia.spawnerplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class SpawnerManager implements Listener {
    private final Map<Location, Spawner> spawners = new HashMap<>();
    private final LanguageManager languageManager;

    public SpawnerManager(SpawnerPlugin plugin, LanguageManager languageManager) {
        this.languageManager = languageManager;
    }

    public void addSpawner(Location location, Spawner spawner) {
        spawners.put(location, spawner);
        Bukkit.getLogger().info("Spawner registered at: " + location + " for entity: " + spawner.getEntityType());
    }

    public void removeSpawner(Location location) {
        spawners.remove(location);
        Bukkit.getLogger().info("Spawner removed from: " + location);
    }

    public Map<Location, Spawner> getSpawners() {
        return spawners;
    }

    public void listSpawners(Player player) {
        if (spawners.isEmpty()) {
            player.sendMessage(languageManager.getMessage("no_spawners"));
            return;
        }

        player.sendMessage(languageManager.getMessage("spawner_list_header"));
        for (Map.Entry<Location, Spawner> entry : spawners.entrySet()) {
            Location loc = entry.getKey();
            Spawner spawner = entry.getValue();
            String msg = languageManager.getMessage("spawner_list_entry")
                    .replace("{location}", locToString(loc))
                    .replace("{entity}", spawner.getEntityType())
                    .replace("{limit}", String.valueOf(spawner.getMaxSpawns()));
            player.sendMessage(msg);
        }
    }

    private String locToString(Location loc) {
        return "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }
}
