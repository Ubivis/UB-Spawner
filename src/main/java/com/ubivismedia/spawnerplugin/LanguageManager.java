package com.ubivismedia.spawnerplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.ChatColor;
import java.io.File;

public class LanguageManager {
    private final FileConfiguration langConfig;

    public LanguageManager(SpawnerPlugin plugin) {
        String language = plugin.getConfig().getString("language", "en");
        File langFile = new File(plugin.getDataFolder(), "language_" + language + ".yml");
        if (!langFile.exists()) {
            plugin.saveResource("language_en.yml", false);
            langFile = new File(plugin.getDataFolder(), "language_en.yml");
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', langConfig.getString(key, key));
    }
}
