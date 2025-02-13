package com.ubivismedia.spawnerplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnerCommand implements CommandExecutor {
    private final SpawnerManager spawnerManager;
    private final LanguageManager languageManager;

    public SpawnerCommand(SpawnerManager spawnerManager, LanguageManager languageManager) {
        this.spawnerManager = spawnerManager;
        this.languageManager = languageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(languageManager.getMessage("command_only_players"));
            return true;
        }

        Player player = (Player) sender;
        spawnerManager.listSpawners(player);
        return true;
    }
}
