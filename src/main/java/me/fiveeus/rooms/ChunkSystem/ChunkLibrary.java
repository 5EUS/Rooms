package me.fiveeus.rooms.ChunkSystem;

import com.sk89q.worldedit.session.ClipboardHolder;
import me.fiveeus.rooms.Config.ConfigExtended;
import me.fiveeus.rooms.Config.Logger;
import me.fiveeus.rooms.Rooms;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class ChunkLibrary {
    private final List<Chunk> chunks;

    private final String level;

    private final Rooms plugin;

    private File levelFolder;

    private ConfigExtended config;

    private final Random random;

    private int algorithm;

    public ChunkLibrary(String level, Rooms plugin) {

        this.level = level;
        this.plugin = plugin;
        this.chunks = new ArrayList<>();

        // may be predictable
        this.random = new Random();

        initialize();

    }

    private void initialize() {

        String fullName = level + ".yml";
        levelFolder = new File(plugin.getDataFolder(), "schematics" + File.separator + this.level);

        // load config instance for level
        config = new ConfigExtended(plugin, levelFolder, fullName);

        algorithm = config.getInt("algorithm", 1);

        // get all schematic files in the directory
        File[] schematicFiles = levelFolder.listFiles((dir, nameF) -> nameF.endsWith(".schem"));

        if (schematicFiles == null || schematicFiles.length == 0) {
            Logger.log(Level.SEVERE, "No schematics found for level " + level);
            return;
        }

        // create Chunk instances for each schematic file and add them to the chunkList
        for (File file : schematicFiles) {
            String fileName = file.getName();
            Chunk chunk = new Chunk(plugin, fileName, levelFolder, config, level);
            chunks.add(chunk);
            Logger.log(Level.INFO, "Loaded chunk " + fileName + " for level " + level);
        }

    }

    public String getLevel() {
        return level;
    }

    public int size() {
        return chunks.size();
    }

    public List<Chunk> getChunks() {
        return chunks;
    }

    public Chunk getAligned() {

        Chunk result = chunks.get(random.nextInt(chunks.size()));


        return result;

    }

    public void unloadChunks() {
        chunks.clear();
    }

    public int getAlgorithm() {
        return algorithm;
    }

    public Chunk getRandomChunk() {

        return chunks.get(random.nextInt(chunks.size()));

    }
}
