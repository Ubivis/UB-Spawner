package com.ubivismedia.spawnerplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
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

            String locationString = locToString(loc);
            String messageText = languageManager.getMessage("spawner_list_entry")
                    .replace("{location}", locationString)
                    .replace("{entity}", spawner.getEntityType())
                    .replace("{limit}", String.valueOf(spawner.getMaxSpawns()));

            Component messageComponent = Component.text(messageText)
                    .append(Component.space())
                    .append(Component.text("[" + languageManager.getMessage("spawner_remove_button") + "]")
                            .color(NamedTextColor.RED)
                            .clickEvent(ClickEvent.runCommand("/removespawner " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()))
                    );

            player.sendMessage(messageComponent);
        }
    }

    private String locToString(Location loc) {
        return "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }
}
