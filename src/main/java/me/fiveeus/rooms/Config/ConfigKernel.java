package me.fiveeus.rooms.Config;

import me.fiveeus.rooms.Rooms;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Simple abstraction class to handle creating and loading configuration files
 */

public class ConfigKernel {

    private final FileConfiguration config;

    private final File file;

    public ConfigKernel(Rooms plugin, File parentDir, String configName) {
        file = new File(parentDir, configName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(configName, false);
        }
        config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
