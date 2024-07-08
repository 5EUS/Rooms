package me.fiveeus.rooms.Config;

import me.fiveeus.rooms.Rooms;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigExtended extends ConfigKernel {
    public ConfigExtended(Rooms plugin, File parentDir, String configName) {
        super(plugin, parentDir, configName);
    }

    public void setString(String path, String value) {
        getConfig().set(path, value);
        saveConfig();
    }

    public String getString(String path, String defaultValue) {
        return getConfig().getString(path, defaultValue);
    }

    public String getString(String path) {
        return getConfig().getString(path);
    }

    public void setInt(String path, int value) {
        getConfig().set(path, value);
        saveConfig();
    }

    public int getInt(String path, int defaultValue) {
        return getConfig().getInt(path, defaultValue);
    }

    public int getInt(String path) {
        return getConfig().getInt(path);
    }

    public List<String> getGetStringList(String path) {
        return getConfig().getStringList(path);
    }

    public void setBoolean(String path, boolean value) {
        getConfig().set(path, value);
        saveConfig();
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return getConfig().getBoolean(path, defaultValue);
    }

    public boolean getBoolean(String path) {
        return getConfig().getBoolean(path);
    }

    public void setDouble(String path, double value) {
        getConfig().set(path, value);
        saveConfig();
    }

    public double getDouble(String path, double defaultValue) {
        return getConfig().getDouble(path, defaultValue);
    }

    public double getDouble(String path) {
        return getConfig().getDouble(path);
    }

    public void setList(String path, List<String> value) {
        getConfig().set(path, value);
        saveConfig();
    }

    public List<String> getList(String path) {
        return getConfig().getStringList(path);
    }

    public void setMap(String path, Map<String, String> value) {
        getConfig().set(path, value);
        saveConfig();
    }

    public List<Map<?,?>> getMapList(String path) {
        return getConfig().getMapList(path);
    }

    public Set<String> getKeys(boolean b) {
        return getConfig().getKeys(b);
    }

    public Object get(String key) {
        return getConfig().get(key);
    }

    public ConfigurationSection getConfigurationSection(String key) {
        return getConfig().getConfigurationSection(key);
    }
}
