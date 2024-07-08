package me.fiveeus.rooms;

import me.fiveeus.rooms.Config.Logger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class Utilities {

    private static final String noPermission = Rooms.getConfigInst().getString("no-permission");

    public static boolean checkPermission(CommandSender sender, String permission) {

        if (noPermission == null) {
            Logger.log(Level.SEVERE, "no permission message specified in config.yml");
            return false;
        }

        if (sender.hasPermission(permission)) {
            return true;
        } else {
            sender.sendMessage(noPermission);
            return false;
        }
    }

    public static String getPrefix() {

        String prefix = Rooms.getConfigInst().getString("prefix");
        if (prefix == null) {
            Logger.log(Level.SEVERE, "no prefix specified in config.yml");
            return ChatColor.RED + "[Sprooms] ";
        }
        return ChatColor.translateAlternateColorCodes('&', prefix);

    }
}
