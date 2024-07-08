package me.fiveeus.rooms;

import me.fiveeus.rooms.ChunkSystem.ChunkGenerator;
import me.fiveeus.rooms.Commands.Chunks;
import me.fiveeus.rooms.Config.ConfigExtended;
import me.fiveeus.rooms.Config.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public final class Rooms extends JavaPlugin {

    private static Rooms plugin;

    private static ConfigExtended config;

    private static ChunkGenerator chunkGenerator;

    @Override
    public void onEnable() {
        // self reference
        Rooms.plugin = this;

        // Load config
        Rooms.config = new ConfigExtended(this, getDataFolder(), "config.yml");

        // set logging based on config
        Logger.logging = config.getBoolean("logs");

        // check default schem stuff
        getDefaults();

        // load ChunkGenerator
        chunkGenerator = new ChunkGenerator(this);

        // register listeners
        registerListeners();

        Logger.log(Level.INFO, "Rooms plugin successfully loaded.");
    }

    @Override
    public void onDisable() {
        Logger.log(Level.INFO, "Rooms plugin successfully unloaded.");
    }

    private void registerListeners() {
        this.getCommand("chunks").setExecutor(new Chunks());
        Logger.log(Level.INFO, "Registered listeners");
    }

    private void getDefaults() {

        // check for schematics folder
        File schematics = new File(getDataFolder(), "schematics");
        if (!schematics.exists()) {
            schematics.mkdirs();
            Logger.log(Level.INFO, "Created new schematics folder");
        }

        // check for default level folder in schematics folder
        File defaultSchemFolder = new File(schematics, "1");
        if (!defaultSchemFolder.exists()) {
            defaultSchemFolder.mkdirs();
            Logger.log(Level.INFO, "Created new level 1 folder");
        }

        // check for default level config file in default level folder in schematics folder :)
        File defaultLevelCfgFile = new File(defaultSchemFolder, "1.yml");
        if (!defaultLevelCfgFile.exists()) {
            defaultLevelCfgFile.getParentFile().mkdirs();
            saveResource("schematics" + File.separator + "1" + File.separator + "1.yml", false);
            Logger.log(Level.INFO, "Created new 1.yml");
        }

        // TODO check for default schem file

    }

    public static ConfigExtended getConfigInst() {
        return config;
    }

    public static ChunkGenerator getChunkGenerator() {
        return chunkGenerator;
    }

    public static Rooms getPlugin() {
        return plugin;
    }
}
