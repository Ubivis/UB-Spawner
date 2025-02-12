package com.ubivismedia.spawnerplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class SpawnerPlugin extends JavaPlugin {
    private SpawnerManager spawnerManager;
    private LanguageManager languageManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        languageManager = new LanguageManager(this);
        spawnerManager = new SpawnerManager(this, languageManager);

        getServer().getPluginManager().registerEvents(spawnerManager, this);
        getCommand("listspawners").setExecutor(new SpawnerCommand(spawnerManager, languageManager));
        getCommand("getspawner").setExecutor(new GetSpawnerCommand(this, languageManager));
        getServer().getPluginManager().registerEvents(new SpawnerPlacementListener(this), this);
    }
}
