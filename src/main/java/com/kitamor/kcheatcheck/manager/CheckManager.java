package com.kitamor.kcheatcheck.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kitamor.kcheatcheck.KCheatCheck;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class CheckManager {

    private final KCheatCheck plugin;
    private final File checksFile;
    private final File historyFile;
    private final Gson gson;

    // Current checks: UUID -> ActiveCheck
    private Map<UUID, ActiveCheck> activeChecks;
    // History: UUID -> List of CheckRecord
    private Map<UUID, List<CheckRecord>> checkHistory;

    public CheckManager(KCheatCheck plugin) {
        this.plugin = plugin;
        this.checksFile = new File(plugin.getDataFolder(), "active_checks.json");
        this.historyFile = new File(plugin.getDataFolder(), "history.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.activeChecks = new HashMap<>();
        this.checkHistory = new HashMap<>();
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        try {
            if (checksFile.exists()) {
                Type type = new TypeToken<Map<UUID, ActiveCheck>>() {}.getType();
                try (FileReader reader = new FileReader(checksFile)) {
                    Map<UUID, ActiveCheck> loaded = gson.fromJson(reader, type);
                    if (loaded != null) {
                        activeChecks = loaded;
                    }
                }
            }
            if (historyFile.exists()) {
                Type type = new TypeToken<Map<UUID, List<CheckRecord>>>() {}.getType();
                try (FileReader reader = new FileReader(historyFile)) {
                    Map<UUID, List<CheckRecord>> loaded = gson.fromJson(reader, type);
                    if (loaded != null) {
                        checkHistory = loaded;
                    }
                }
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not load data: " + e.getMessage());
        }
    }

    public void save() {
        try {
            try (FileWriter writer = new FileWriter(checksFile)) {
                gson.toJson(activeChecks, writer);
            }
            try (FileWriter writer = new FileWriter(historyFile)) {
                gson.toJson(checkHistory, writer);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data: " + e.getMessage());
        }
    }

    public boolean isChecked(UUID uuid) {
        return activeChecks.containsKey(uuid);
    }

    public void startCheck(UUID target, String staffName, String targetLoc, String staffLoc, String areaName) {
        activeChecks.put(target, new ActiveCheck(staffName, targetLoc, staffLoc, areaName));
        save();
    }

    public ActiveCheck endCheck(UUID target, String staffName, String result) {
        ActiveCheck check = activeChecks.remove(target);
        
        CheckRecord record = new CheckRecord(staffName, result, System.currentTimeMillis());
        checkHistory.computeIfAbsent(target, k -> new ArrayList<>()).add(record);
        
        save();
        return check;
    }

    public ActiveCheck getActiveCheck(UUID target) {
        return activeChecks.get(target);
    }

    public String getCheckingStaff(UUID target) {
        ActiveCheck check = activeChecks.get(target);
        return check != null ? check.getStaffName() : null;
    }

    public Map<UUID, ActiveCheck> getActiveChecks() {
        return activeChecks;
    }

    public Map<UUID, List<CheckRecord>> getAllHistory() {
        return checkHistory;
    }

    public List<CheckRecord> getHistory(UUID target) {
        return checkHistory.getOrDefault(target, new ArrayList<>());
    }

    public static class ActiveCheck {
        private String staffName;
        private String targetOriginalLoc;
        private String staffOriginalLoc;
        private String areaName;

        public ActiveCheck(String staffName, String targetOriginalLoc, String staffOriginalLoc, String areaName) {
            this.staffName = staffName;
            this.targetOriginalLoc = targetOriginalLoc;
            this.staffOriginalLoc = staffOriginalLoc;
            this.areaName = areaName;
        }

        public String getStaffName() { return staffName; }
        public String getTargetOriginalLoc() { return targetOriginalLoc; }
        public String getStaffOriginalLoc() { return staffOriginalLoc; }
        public String getAreaName() { return areaName; }
    }

    public static class CheckRecord {
        private String staffName;
        private String result;
        private long date;

        public CheckRecord(String staffName, String result, long date) {
            this.staffName = staffName;
            this.result = result;
            this.date = date;
        }

        public String getStaffName() { return staffName; }
        public String getResult() { return result; }
        public long getDate() { return date; }
    }
}
