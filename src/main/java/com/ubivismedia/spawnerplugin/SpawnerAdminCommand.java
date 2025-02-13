package com.ubivismedia.spawnerplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SpawnerAdminCommand implements CommandExecutor {
    private final SpawnerPlugin plugin;

    public SpawnerAdminCommand(SpawnerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getLanguageManager().loadLanguage();
            sender.sendMessage("SpawnerPlugin config and language reloaded.");
            return true;
        }
        return false;
    }
}
