package com.kitamor.kcheatcheck;

import com.kitamor.kcheatcheck.command.KontCommand;
import com.kitamor.kcheatcheck.listener.PlayerListener;
import com.kitamor.kcheatcheck.manager.CheckManager;
import com.kitamor.kcheatcheck.manager.GuiManager;
import com.kitamor.kcheatcheck.manager.LanguageManager;
import com.kitamor.kcheatcheck.manager.LocationManager;
import org.bukkit.plugin.java.JavaPlugin;

public class KCheatCheck extends JavaPlugin {

    private static KCheatCheck instance;
    private CheckManager checkManager;
    private LanguageManager languageManager;
    private LocationManager locationManager;
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.languageManager = new LanguageManager(this);
        this.locationManager = new LocationManager(this);
        this.guiManager = new GuiManager(this);

        this.checkManager = new CheckManager(this);
        this.checkManager.load();

        KontCommand kontCmd = new KontCommand(this);
        getCommand("kont").setExecutor(kontCmd);
        getCommand("kont").setTabCompleter(kontCmd);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("kCheatCheck aktif edildi!");
    }

    @Override
    public void onDisable() {
        if (this.checkManager != null) {
            this.checkManager.save();
        }
        getLogger().info("kCheatCheck deaktif edildi!");
    }

    public static KCheatCheck getInstance() {
        return instance;
    }

    public CheckManager getCheckManager() {
        return checkManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }
}
