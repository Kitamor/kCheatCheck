package com.kitamor.kcheatcheck.manager;

import com.kitamor.kcheatcheck.KCheatCheck;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class LocationManager {
    private final KCheatCheck plugin;
    private File locFile;
    private FileConfiguration locConfig;

    public LocationManager(KCheatCheck plugin) {
        this.plugin = plugin;
        setup();
    }

    private void setup() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        locFile = new File(plugin.getDataFolder(), "locations.yml");
        if (!locFile.exists()) {
            try {
                locFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create locations.yml!");
            }
        }
        locConfig = YamlConfiguration.loadConfiguration(locFile);
    }

    public void addArea(String name, Location loc) {
        locConfig.set("areas." + name, serialize(loc));
        save();
    }

    public void removeArea(String name) {
        locConfig.set("areas." + name, null);
        save();
    }

    public Location getArea(String name) {
        String str = locConfig.getString("areas." + name);
        if (str == null) return null;
        return deserialize(str);
    }

    public Set<String> getAreas() {
        if (!locConfig.contains("areas")) return null;
        return locConfig.getConfigurationSection("areas").getKeys(false);
    }

    public void save() {
        try {
            locConfig.save(locFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save locations.yml!");
        }
    }

    public static String serialize(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }

    public static Location deserialize(String str) {
        if (str == null || str.isEmpty()) return null;
        String[] parts = str.split(",");
        if (parts.length < 6) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        try {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
