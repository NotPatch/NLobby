package com.notpatch.nLobby.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationSerializer {

    public static String serialize(Location loc) {
        if (loc == null) return null;
        return String.format("%s,%s,%s,%s,%s,%s",
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch());
    }

    public static Location deserialize(String str) {
        if (str == null || str.isBlank()) return null;

        String[] parts = str.split(",");
        if (parts.length < 4) return null;

        try {
            String worldName = parts[0];
            World world = Bukkit.getWorld(worldName);
            if (world == null) return null;

            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0;

            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
