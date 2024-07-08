package me.fiveeus.rooms.Commands;

import me.fiveeus.rooms.ChunkSystem.ChunkGenerator;
import me.fiveeus.rooms.Rooms;
import me.fiveeus.rooms.Utilities;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Chunks implements CommandExecutor {

    private static final String PREFIX = Utilities.getPrefix();

    private static final Rooms plugin = Rooms.getPlugin();

    private static final ChunkGenerator chunkGenerator = Rooms.getChunkGenerator();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // check if player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        // check command
        if (!label.equalsIgnoreCase("chunks")) {
            return true;
        }

        // check permission
        if (!Utilities.checkPermission(player, "chunks")) {
            return true;
        }

        // check arguments
        // /chunk [command] [label] [args]
        // /chunk generate 1 4 4 4: generates level 1 with size 4x4x4 chunks
        // /chunk list 1: list the schematics for level 1

        if (args.length < 1) {
            sendHelpMessage(player);
            return true;
        }


        switch (args[0].toLowerCase()) {


            case "generate":


                if (args.length < 2) {
                    sendHelpMessage(player);
                    player.sendMessage(ChatColor.RED + "ERROR: No label specified");
                    return true;
                }

                String level = args[1];

                if (args.length < 3) {
                    sendHelpMessage(player);
                    player.sendMessage(ChatColor.RED + "ERROR: No width specified");
                    return true;
                }

                if (args.length < 4) {
                    sendHelpMessage(player);
                    player.sendMessage(ChatColor.RED + "ERROR: No length specified");
                    return true;
                }

                if (args.length < 5) {
                    sendHelpMessage(player);
                    player.sendMessage(ChatColor.RED + "ERROR: No height specified");
                    return true;
                }

                int width = Integer.parseInt(args[2]);
                int length = Integer.parseInt(args[3]);
                int height = Integer.parseInt(args[4]);

                // TODO: no maximum size
                if (width <= 0 || length <= 0 || height <= 0) {
                    player.sendMessage(ChatColor.RED + "ERROR: Size must be a positive integer");
                    return true;
                }

                // load the level into memory if it isn't
                // creating a new level after startup initialization
                chunkGenerator.loadLibrary(level);

                // generate the chunks
                if (!chunkGenerator.generate(level, player.getLocation(), width, length, height)) {
                    player.sendMessage(ChatColor.RED + "ERROR: Failed to generate chunks.");
                    return true;
                }
                player.sendMessage(Utilities.getPrefix() + " " + ChatColor.GRAY + "Generated chunks for level " + ChatColor.AQUA + level);

                break;

            case "list":
                chunkGenerator.sendLibraries(player);
                break;

            case "load":

                if (args.length < 2) {
                    sendHelpMessage(player);
                    player.sendMessage(ChatColor.RED + "ERROR: No label specified");
                    return true;
                }

                if (!chunkGenerator.loadLibrary(args[1])) {
                    player.sendMessage(ChatColor.RED + "ERROR: Level unable to load or syntax error");
                }
                player.sendMessage(Utilities.getPrefix() + " " + ChatColor.GRAY + "Loaded level " + ChatColor.AQUA + args[1]);
                break;


            case "unload":

                if (args.length < 2) {
                    sendHelpMessage(player);
                    player.sendMessage(ChatColor.RED + "ERROR: No label specified");
                    return true;
                }

                if (!chunkGenerator.unloadLevel(args[1])) {
                    player.sendMessage(ChatColor.RED + "ERROR: Level not loaded or syntax error");
                }
                player.sendMessage(Utilities.getPrefix() + " " + ChatColor.GRAY + "Unloaded level " + ChatColor.AQUA + args[1]);

                break;

            default:
                sendHelpMessage(player);
                break;
        }

        return true;

    }


    private static void sendHelpMessage(Player player) {

        player.sendMessage(PREFIX + " /chunk usage:");
        player.sendMessage("");
        player.sendMessage("/chunk [command] [label] [args]");
        player.sendMessage("Commands: generate, list");
        player.sendMessage("Label: label of the level, an integer");
        player.sendMessage("Args: sizeX, sizeY, sizeZ, [schematic file]");
        player.sendMessage("Example: /chunk generate 1 4 4 4: generates level 1 with size 4x4x4 chunks");
        player.sendMessage("");

    }
}
