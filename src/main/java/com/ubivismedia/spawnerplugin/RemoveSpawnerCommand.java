package com.ubivismedia.spawnerplugin;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveSpawnerCommand implements CommandExecutor {
    private final SpawnerManager spawnerManager;
    private final LanguageManager languageManager;

    public RemoveSpawnerCommand(SpawnerManager spawnerManager, LanguageManager languageManager) {
        this.spawnerManager = spawnerManager;
        this.languageManager = languageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(languageManager.getMessage("command_only_players"));
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage("Usage: /removespawner <x> <y> <z>");
            return true;
        }

        try {
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            int z = Integer.parseInt(args[2]);

            Location location = new Location(((Player) sender).getWorld(), x, y, z);

            if (spawnerManager.getSpawners().containsKey(location)) {
                spawnerManager.removeSpawner(location);
                sender.sendMessage(languageManager.getMessage("spawner_removed").replace("{location}", x + ", " + y + ", " + z));
            } else {
                sender.sendMessage(languageManager.getMessage("spawner_not_found").replace("{location}", x + ", " + y + ", " + z));
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid coordinates.");
        }

        return true;
    }
}
