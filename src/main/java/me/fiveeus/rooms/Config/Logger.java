package me.fiveeus.rooms.Config;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class Logger {

    public static boolean logging = false;

    public final static String LOGPREFIX = "[Rooms] ";

    public static void log(Level level, String message) {

        if (logging) {

            message = LOGPREFIX + message;

            Bukkit.getLogger().log(level, message);

            // print to ops
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp()) {
                    player.sendMessage(message);
                }
            }
        }
    }
}
