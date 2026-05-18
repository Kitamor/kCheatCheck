package com.kitamor.kcheatcheck.util;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class PapiUtil {
    private static Boolean enabled = null;

    public static boolean isEnabled() {
        if (enabled == null) {
            enabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        }
        return enabled;
    }

    public static String replace(OfflinePlayer player, String text) {
        if (text == null || text.isEmpty()) return text;
        if (isEnabled()) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }
}
