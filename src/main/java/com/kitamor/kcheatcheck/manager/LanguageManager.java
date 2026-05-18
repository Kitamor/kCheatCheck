package com.kitamor.kcheatcheck.manager;

import com.kitamor.kcheatcheck.KCheatCheck;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LanguageManager {
    private final KCheatCheck plugin;
    private FileConfiguration langConfig;

    public LanguageManager(KCheatCheck plugin) {
        this.plugin = plugin;
        loadLang();
    }

    public void reload() {
        plugin.reloadConfig();
        loadLang();
    }

    public void loadLang() {
        String lang = plugin.getConfig().getString("language", "tr");
        File langFile = new File(plugin.getDataFolder() + File.separator + "lang", lang + ".yml");
        
        if (!langFile.exists()) {
            langFile.getParentFile().mkdirs();
            try {
                plugin.saveResource("lang/" + lang + ".yml", false);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Could not find default language file in jar for: " + lang);
            }
        }
        
        if (langFile.exists()) {
            langConfig = YamlConfiguration.loadConfiguration(langFile);
        } else {
            langConfig = new YamlConfiguration();
        }
        
        // Fallback to internal if available
        InputStream defLangStream = plugin.getResource("lang/" + lang + ".yml");
        if (defLangStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defLangStream));
            langConfig.setDefaults(defConfig);
        }
    }

    public String getMsg(String path) {
        return getMsg(null, path);
    }

    public String getMsg(org.bukkit.OfflinePlayer player, String path) {
        if (langConfig == null) return "Message not found: " + path;
        String msg = langConfig.getString(path, "Message not found: " + path);
        return com.kitamor.kcheatcheck.util.PapiUtil.replace(player, msg);
    }
}
