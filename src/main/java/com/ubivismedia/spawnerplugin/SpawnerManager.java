package com.ubivismedia.spawnerplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class SpawnerManager implements Listener {
    private final Map<Location, Spawner> spawners = new HashMap<>();
    private final LanguageManager languageManager;

    public SpawnerManager(SpawnerPlugin plugin, LanguageManager languageManager) {
        this.languageManager = languageManager;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
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
                    .replace("{limit}", String.valueOf(spawner.getSpawnLimit()));
            player.sendMessage(msg);
        }
    }

    @EventHandler
    public void onSpawnerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Location loc = event.getClickedBlock().getLocation();
            if (spawners.containsKey(loc)) {
                event.getPlayer().sendMessage(languageManager.getMessage("spawner_removed").replace("{location}", locToString(loc)));
                spawners.remove(loc);
                event.getClickedBlock().setType(Material.AIR);
            }
        }
    }

    private String locToString(Location loc) {
        return "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }
}
