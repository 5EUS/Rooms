package me.fiveeus.rooms.ChunkSystem;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.fiveeus.rooms.Config.ConfigExtended;
import me.fiveeus.rooms.Config.Logger;
import me.fiveeus.rooms.Rooms;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class Chunk {

    private int width;

    private int height;

    private int length;

    private final String level;

    private List<Direction.Directions> entryDirection;

    private List<Direction.Directions> exitDirection;

    private List<Direction.Directions> holes;

    private final String chunkName;

    /**
     * 4 clipboards per chunk, one for each direction
     */
    private ClipboardHolder[] holders;

    private final File folder;

    private final Rooms plugin;

    private final ConfigExtended config;



    public Chunk(Rooms plugin, String chunkName, File folder, ConfigExtended config, String level) {

        // get info references
        this.level = level;
        this.plugin = plugin;
        this.chunkName = chunkName;
        this.folder = folder;
        this.config = config;
        this.holders = new ClipboardHolder[4];

        // set components
        setComponents();

        // get clipboards
        getClipboards();
    }


    private void setComponents() {

        // initialize components

        // iterate through config sections
        Set<String> keys = config.getKeys(false);
        for (String key : keys) {

            Object value = config.get(key);

            // check if section exists
            if (!(value instanceof ConfigurationSection)) {
                continue;
            }

            String standardName = chunkName.replace(".schem", "");

            // if the key corresponds to the chunk
            if (!keys.contains(standardName)) {
                Logger.log(Level.SEVERE, "Invalid chunk name in configuration: " + standardName);
                continue;
            }

            if (!key.equalsIgnoreCase(standardName)) {
                continue;
            }

            // load chunk components
            Logger.log(Level.INFO, "Loading component " + key + " for level " + level);
            ConfigurationSection section = config.getConfigurationSection(key);

            this.width = section.getInt("width");
            this.length = section.getInt("length");
            this.height = section.getInt("height");
            this.entryDirection = new ArrayList<>();
            List<String> entryDirectionS = section.getStringList("entryDirection");
            if (entryDirectionS.isEmpty()) {
                Logger.log(Level.SEVERE, "No entry points found for chunk " + chunkName);
            }
            for (String entryDirection : entryDirectionS) {
                this.entryDirection.add(Direction.fromString(entryDirection));
            }
            this.exitDirection = new ArrayList<>();
            List<String> exitDirectionS = section.getStringList("exitDirection");
            if (exitDirectionS.isEmpty()) {
                Logger.log(Level.SEVERE, "No entry points found for chunk " + chunkName);
            }
            for (String exitDirection : exitDirectionS) {
                this.exitDirection.add(Direction.fromString(exitDirection));
            }
            this.holes = new ArrayList<>();
            holes.addAll(this.entryDirection);
            holes.addAll(this.exitDirection);
            Logger.log(Level.INFO, "Chunk " + chunkName + " width set to " + width);
            Logger.log(Level.INFO, "Chunk " + chunkName + " length set to " + length);
            Logger.log(Level.INFO, "Chunk " + chunkName + " height set to " + height);
            Logger.log(Level.INFO, "Chunk " + chunkName + " entry points set to " + entryDirection);
            Logger.log(Level.INFO, "Chunk " + chunkName + " exit points set to " + exitDirection);
            Logger.log(Level.INFO, "Loaded chunk " + chunkName + " components from yml.");

        }
    }

    private void getClipboards() {

        Clipboard clipboard;

        // load level-specific schematic folder
        if (!folder.exists()) {
            Logger.log(Level.SEVERE, "Chunk folder " + folder + " does not exist");
            return;
        }

        // load schematics corresponging to $chunkName
        File file = new File(folder, chunkName);
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            Bukkit.getLogger().log(Level.SEVERE, "Invalid format for " + file);
            return;
        }

        // read the clipboard from the schematic file
        try (ClipboardReader reader = format.getReader(Files.newInputStream(file.toPath()))) {
            clipboard = reader.read();
        } catch (IOException e) {
            Logger.log(Level.SEVERE, "Error loading " + file + " schematic file");
            e.printStackTrace();
            return;
        }

        Logger.log(Level.INFO, "Created base clipboard for chunk " + chunkName);

        // create 4 clipboards per chunk
        for (int i = 0; i < 4; i++) {

            // create a new clipboard
            ClipboardHolder holder = new ClipboardHolder(clipboard);

            // add it to the holders array
            holders[i] = holder;

            // get rotation and transform
            int angle = i * 90;
            Transform transform = new AffineTransform().rotateY(angle);

            // rotate the clipboard
            holder.setTransform(transform);
            Logger.log(Level.INFO,
                    "Created clipboard holder for chunk " + chunkName + " rotated " + angle + " degrees");
        }

    }

    public List<Direction.Directions> getEntryDirections() {
        return entryDirection;
    }

    public List<Direction.Directions> getExitDirections() {
        return exitDirection;
    }

    public int getWidth() {
        return width;
    }

    public int getLength() {
        return length;
    }

    public int getHeight() {
        return height;
    }

    public ClipboardHolder[] getHolders() {
        return holders;
    }

    public int[] getSize() {
        return new int[]{width, height, length};
    }

    public String getName() {
        return chunkName;
    }

    public String getFullName() {
        return level + "_" + chunkName;
    }

    public List<Direction.Directions> getHoles() {
        return holes;
    }
}
