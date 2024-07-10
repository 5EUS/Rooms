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

    private final List<ChunkLibrary> loadedLevels;

    private final Rooms plugin;

    private static final Random random = new Random();

    private static String PREFIX;

    public ChunkGenerator(Rooms plugin) {

        Logger.log(Level.INFO, "Started creating ChunkGenerator");
        this.plugin = plugin;
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
            Logger.log(Level.SEVERE, "Error loading levels from filesystem");
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

    // pastes in the northwest corner
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

        // get the algorithm to use
        switch (library.getAlgorithm()) {

            case 1:
                return generate1(width, height, length, library, location, we);


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
        if (library == null) {
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
     * Gets the correct position for the chunk based on the rotation
     * @param position the original position
     * @param size the size of the chunk
     * @param rotation the desired rotation
     * @return the corrected coords to paste
     */
    private static double[] rotatePosition(double[] position, int[] size, int rotation) {
        double[] result = new double[3];

        switch (rotation) {
            case 0: // No rotation
                result[0] = position[0];
                result[1] = position[1];
                result[2] = position[2];
                break;
            case 1: // 90 degrees
                result[0] = position[0];
                result[1] = position[1];
                result[2] = position[2] - size[2];
                break;
            case 2: // 180 degrees
                result[0] = position[0] - size[0];
                result[1] = position[1];
                result[2] = position[2] - size[2];
                break;
            case 3: // 270 degrees
                result[0] = position[0] - size[0];
                result[1] = position[1];
                result[2] = position[2];
                break;
        }

        return result;
    }


    private static boolean generate1(int width, int height, int length, ChunkLibrary library, Location location, com.sk89q.worldedit.world.World we) {
        // generate chunks based on schematics and size


        for (int i = 0; i < width; i++) {
            for (int j = 0; j < length; j++) {
                for (int k = 0; k < height; k++) {

                    // try to get an aligned clipboard holder
                    ClipboardHolder holder;
                    Chunk chunk;
                    int rotation = 0;

                    Logger.log(Level.INFO, "");
                    Logger.log(Level.INFO, "Starting chunk generation in grid location " + i + " " + j + " " + k);

                    // Check if the current position is on the edge
                    boolean isEdge = i == 0 || i == width - 1 || j == 0 || j == length - 1;
                    Logger.log(Level.INFO, "isEdge: " + isEdge);

                    // get a random chunk to start
                    chunk = library.getRandomChunk();

                    List<Direction.Directions> holes = chunk.getHoles();

                    if (isEdge) {
                        // if it is a corner it cannot be a straight chunk
                        if (isCorner(i, j, width, length)) {
                            Logger.log(Level.INFO, "Corner generator");

                            // try to get a straight chunk
                            int tries = 0;
                            int maxTries = 50; // small chance to produce an error
                            while (isStraight(chunk) || tries < maxTries || holes.size() > 2) {
                                chunk = library.getRandomChunk();
                                holes = chunk.getHoles();
                                tries++;
                            }

                            // get the rotation we need based on which corner we are in and which directions there are holes
                            int corner = whichCorner(i, j, width, length);
                            Logger.log(Level.INFO, "Corner: " + corner);
                            rotation = whichRotation(corner, holes);


                        } else {

                            Logger.log(Level.INFO, "Edge generator");

                            // get a chunk that contains a straight pathway
                            int tries = 0;
                            int maxTries = 50; // small chance to produce an error
                            while (!isStraight(chunk) || tries < maxTries) {
                                chunk = library.getRandomChunk();
                                holes = chunk.getHoles();
                                tries++;
                            }

                            // get the edge we are at
                            Direction.Directions edge = Direction.getEdge(i, j, width, length);
                            Logger.log(Level.INFO, "Edge: " + edge);
                            Logger.log(Level.INFO, "Holes: " + holes);

                             // get the rotation based on the edge
                            rotation = whichRotation(holes, edge);

                        }

                    } else {
                        // Get a regular chunk
                        rotation = random.nextInt(4);

                    }

                    holder = chunk.getHolders()[rotation];
                    int[] size = chunk.getSize();

                    Logger.log(Level.INFO, "Chunk name: " + chunk.getFullName());
                    Logger.log(Level.INFO, "Size: " + Arrays.toString(size));
                    Logger.log(Level.INFO, "Rotation: " + rotation * 90);

                    // TODO support for rectangular prism rotation
                    double[] to = new double[3];
                    to[0] = location.getX() + (i * (size[0]));
                    to[1] = location.getY() - (k * (size[1])); // go downwards
                    to[2] = location.getZ() + (j * (size[2]));

                    double[] adjustedTo = rotatePosition(to, size, rotation);
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

                    Logger.log(Level.INFO, "Percent done: " + (i / width * 1.0));

                }
            }
        }

        return true;
    }


    private static boolean isStraight(Chunk chunk) {

        boolean result = false;
        List<Direction.Directions> entryPoints = chunk.getEntryDirections();
        List<Direction.Directions> exitPoints = chunk.getExitDirections();
        for (Direction.Directions entryPoint : entryPoints) {

            if (exitPoints.contains(Direction.getOpposite(entryPoint))) {
                return true;
            }

        }
        return result;
    }

    private static boolean isCorner(int i, int j, int width, int length) {
        // Define the four corners
        int[][] corners = {
                {0, 0},
                {0, length - 1},
                {width - 1, 0},
                {width - 1, length - 1}
        };

        // Check if (i, j) matches any of the corners
        for (int[] corner : corners) {
            if (i == corner[0] && j == corner[1]) {
                return true;
            }
        }

        return false;
    }


    private static int whichCorner(int i, int j, int width, int length) {
        // Define the four corners with their names
        int[][] corners = {
                {0, 0},           // Top Left, northwest
                {0, length - 1},  // Top Right southwest
                {width - 1, 0},   // Bottom Left northeast
                {width - 1, length - 1}  // Bottom Right southeast
        };

        // Check if (i, j) matches any of the corners and return the corresponding name
        for (int k = 0; k < corners.length; k++) {
            if (i == corners[k][0] && j == corners[k][1]) {
                return k;
            }
        }

        return -1;
    }

    private static int whichRotation(int corner, List<Direction.Directions> holes) {

        int rotation = 0;

        List<Direction.Directions> cornerDirections = Direction.getCornerDirectionList(corner);

        for (int i = 0; Direction.hasCommonElement(holes, cornerDirections) && i < 4; i++) {

            // Create a new list to hold the opposite directions
            List<Direction.Directions> newHoles = new ArrayList<>();

            Logger.log(Level.INFO, "Holes:" + holes);
            Logger.log(Level.INFO, "Rotating direction by 90");
            for (Direction.Directions hole : holes) {
                newHoles.add(Direction.getRotated90(hole));
            }
            // Update the holes list to the new list
            holes = newHoles;

            Logger.log(Level.INFO, "Holes:" + holes);
            Logger.log(Level.INFO, "Corners: " + cornerDirections);
            // Increment the rotation counter
            rotation++;
        }

        return rotation;

    }

    private static int whichRotation(List<Direction.Directions> holes, Direction.Directions edge) {

        int rotation = 0;

        while (holes.contains(edge)) {
            // Create a new list to hold the opposite directions
            List<Direction.Directions> newHoles = new ArrayList<>();

            Logger.log(Level.INFO, "Holes:" + holes);
            Logger.log(Level.INFO, "Rotating direction by 90");
            for (Direction.Directions hole : holes) {
                newHoles.add(Direction.getRotated90(hole));
            }

            // Update the holes list to the new list
            holes = newHoles;

            Logger.log(Level.INFO, "Holes:" + holes);
            Logger.log(Level.INFO, "Edge: " + edge);
            // Increment the rotation counter
            rotation++;
        }

        return rotation;
    }

}
