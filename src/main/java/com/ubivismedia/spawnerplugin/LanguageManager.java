package com.ubivismedia.spawnerplugin;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private final JavaPlugin plugin;
    private final Map<String, String> messages = new HashMap<>();

    public LanguageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadLanguage();
    }

    public void loadLanguage() {
        messages.clear();
        String langCode = plugin.getConfig().getString("language", "en");
        File langFolder = new File(plugin.getDataFolder(), "lang");
        File langFile = new File(langFolder, "language_" + langCode + ".yml");

        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file not found: " + langFile.getName() + ". Using default (en).");
            langFile = new File(langFolder, "language_en.yml");
        }

        if (!langFile.exists()) {
            plugin.getLogger().severe("Default language file (language_en.yml) not found in /lang folder!");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(langFile);
        for (String key : config.getKeys(false)) {
            messages.put(key, config.getString(key));
        }
        plugin.getLogger().info("Loaded language: " + langCode);
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "§c[Missing translation: " + key + "]");
    }
}
