package me.fiveeus.rooms.ChunkSystem;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.fiveeus.rooms.Config.Logger;
import me.fiveeus.rooms.Rooms;
import me.fiveeus.rooms.Utilities;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class ChunkGenerator {

    private List<ChunkLibrary> loadedLevels;

    private final Rooms plugin;

    private Random random;

    private static String PREFIX;

    public ChunkGenerator(Rooms plugin) {

        Logger.log(Level.INFO, "Started creating ChunkGenerator");
        this.plugin = plugin;
        this.random = new Random();
        PREFIX = Utilities.getPrefix();

        // new list of levels
        loadedLevels = new ArrayList<>();

        // get libraries from filesystem
        if (!addFileLibraries()) {
            Logger.log(Level.INFO, "No chunk libraries found on filesystem");
        }

        Logger.log(Level.INFO, "ChunkGenerator loaded filesystem levels");

    }

    private boolean addFileLibraries() {

        // load chunk libraries from filesystem
        // loop through folders in the schematics directory
        Path startDir = Paths.get(plugin.getDataFolder().getAbsolutePath() + File.separator + "schematics");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(startDir)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    File folder = new File(entry.toUri());
                    Logger.log(Level.INFO, "Started loading level " + folder.getName());
                    loadLibrary(folder.getName());
                }
            }
        } catch (IOException | DirectoryIteratorException e) {
            e.printStackTrace();
        }

        return true;
    }

    public ChunkLibrary getChunkLibrary(String level) {

        for (ChunkLibrary library : loadedLevels) {
            if (library.getLevel().equalsIgnoreCase(level)) {
                return library;
            }
        }

        return null;
    }


    public boolean loadLibrary(String level) {

        if (getChunkLibrary(level)!= null) {
            Logger.log(Level.SEVERE, "Level " + level + " is already loaded");
            return false;
        }

        ChunkLibrary library = new ChunkLibrary(level, plugin);
        if (library.size() == 0) {
            Logger.log(Level.SEVERE, "No chunks found in level " + level);
            return false;
        }

        loadedLevels.add(library);
        Logger.log(Level.INFO, "Level " + level + " successfully loaded!");
        return true;
    }

    public boolean generate(String level, Location location, int width, int length, int height) {

        // get the library for the level
        ChunkLibrary library = getChunkLibrary(level);

        // Check if library is invalid
        if (library.size() == 0) {
            Logger.log(Level.SEVERE, "ChunkLibrary empty for level " + level);
            return false;
        }

        // create a new edit session
        World world = location.getWorld();
        com.sk89q.worldedit.world.World we = BukkitAdapter.adapt(world);

        // generate chunks based on schematics and size
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < length; j++) {
                for (int k = 0; k < height; k++) {

                    // try to get an aligned chunk
                    Chunk chunk = library.getAligned();

                    int[] size = chunk.getSize();

                    int rotation = random.nextInt(4);
                    ClipboardHolder holder = chunk.getHolders()[rotation];
                    Logger.log(Level.INFO, "Rotation: " + rotation * 90);

                    // TODO support for rectangular prism rotation
                    double[] to = new double[3];
                    to[0] = location.getX() + (i * (size[0] - 1));
                    to[1] = location.getY() + (k * (size[2] - 1));
                    to[2] = location.getZ() + (j * (size[1] - 1));

                    double[] adjustedTo = rotatePosition2(to, size, rotation);
                    Logger.log(Level.INFO, "from " + Arrays.toString(to) + " to: " + Arrays.toString(adjustedTo));


                    try (EditSession editSession = WorldEdit.getInstance().newEditSession(we)) {

                        Operation operation = holder
                                .createPaste(editSession)
                                .to(BlockVector3.at(adjustedTo[0], adjustedTo[1], adjustedTo[2]))
                                .build();
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        Logger.log(Level.SEVERE,
                                "WorldEditException! Unable to generate chunk " + chunk.getName());
                        return false;
                    }

                }
            }
        }

        return true;
    }


    public void sendLibraries(Player player) {

        player.sendMessage(PREFIX + ChatColor.AQUA + " Loaded levels:");
        player.sendMessage();

        for (ChunkLibrary library : loadedLevels) {

            player.sendMessage(ChatColor.GRAY + " - "
                    + ChatColor.AQUA + library.getLevel() + ChatColor.GRAY + " loaded:");

            for (Chunk chunk : library.getChunks()) {

                player.sendMessage(
                        ChatColor.AQUA + "    * " + ChatColor.GRAY + chunk.getFullName()
                                .replace(".schem", ""));
            }

            player.sendMessage();

        }

    }

    public boolean unloadLevel(String arg) {

        ChunkLibrary library = getChunkLibrary(arg);
        if (library.size() == 0) {
            Logger.log(Level.SEVERE, "No ChunkLibrary found for level " + arg);
            return false;
        }

        library.unloadChunks();
        loadedLevels.remove(library);
        Logger.log(Level.INFO, "Level " + arg + " unloaded successfully");

        return true;
    }

    /**
     * BIG NOTE
     * The level creation process will have to be streamlined in a way that ensure the schematics are copied
     * in the same relative place to ensure pasting correctly.
     *
     * Gets the correct position for the chunk based on the rotation
     * @param position the original position
     * @param size the size of the chunk
     * @param rotation the desired rotation
     * @return the corrected coords to paste
     */
    private double[] rotatePosition2(double[] position, int[] size, int rotation) {
        double[] result = new double[3];
        double radians = Math.toRadians(rotation * 90);

        // Handle the rotation for each possible case
        switch (rotation) {
            case 0: // No rotation
                result[0] = position[0];
                result[1] = position[1];
                result[2] = position[2];
                break;
            case 1: // 90 degrees clockwise
                result[0] = position[0];
                result[1] = position[1];
                result[2] = position[2] - size[2] + 1;
                break;
            case 2: // 180 degrees
                result[0] = position[0] - size[0] + 1;
                result[1] = position[1];
                result[2] = position[2] - size[2] + 1;
                break;
            case 3: // 270 degrees clockwise
                result[0] = position[0] - size[0] + 1;
                result[1] = position[1];
                result[2] = position[2];
                break;
        }

        return result;
    }
}
