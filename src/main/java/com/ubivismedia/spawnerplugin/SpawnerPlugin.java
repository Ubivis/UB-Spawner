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
        getServer().getPluginManager().registerEvents(new SpawnerItemUseListener(this), this);

        getCommand("listspawners").setExecutor(new SpawnerCommand(spawnerManager, languageManager));
        getCommand("getspawner").setExecutor(new GetSpawnerCommand(this, languageManager));
        getCommand("spawner").setExecutor(new SpawnerAdminCommand(this));

        getServer().getScheduler().runTaskTimer(this, new SpawnerActivationTask(this, spawnerManager), 0L, 20L);
    }

    public SpawnerManager getSpawnerManager() {
        return spawnerManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }
}
