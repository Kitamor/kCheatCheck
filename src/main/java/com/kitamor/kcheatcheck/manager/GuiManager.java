package com.kitamor.kcheatcheck.manager;

import com.kitamor.kcheatcheck.KCheatCheck;
import com.kitamor.kcheatcheck.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.*;

public class GuiManager {
    private final KCheatCheck plugin;
    public static final String GUI_PREFIX_KEY = "gui_title_base";

    public GuiManager(KCheatCheck plugin) {
        this.plugin = plugin;
    }

    public void openHistoryGui(Player player, int page) {
        Map<UUID, List<CheckManager.CheckRecord>> allHistory = plugin.getCheckManager().getAllHistory();
        List<UUID> uuids = new ArrayList<>(allHistory.keySet());

        int pageSize = 45;
        int maxPage = (int) Math.ceil((double) uuids.size() / pageSize);
        if (maxPage == 0) maxPage = 1;
        if (page < 1) page = 1;
        if (page > maxPage) page = maxPage;

        String title = ColorUtil.color(plugin.getLanguageManager().getMsg("gui_title").replace("{page}", String.valueOf(page)));
        Inventory inv = Bukkit.createInventory(null, 54, title);

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, uuids.size());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        for (int i = start; i < end; i++) {
            UUID uuid = uuids.get(i);
            OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
            List<CheckManager.CheckRecord> records = allHistory.get(uuid);

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(target);
                String pName = target.getName() != null ? target.getName() : uuid.toString();
                meta.setDisplayName(ColorUtil.color(plugin.getLanguageManager().getMsg("gui_player_name").replace("{player}", pName)));
                
                List<String> lore = new ArrayList<>();
                for (CheckManager.CheckRecord record : records) {
                    String dateStr = sdf.format(new Date(record.getDate()));
                    lore.add(ColorUtil.color(plugin.getLanguageManager().getMsg("gui_record_format")
                            .replace("{date}", dateStr)
                            .replace("{staff}", record.getStaffName())
                            .replace("{result}", record.getResult())));
                }
                meta.setLore(lore);
                head.setItemMeta(meta);
            }
            inv.setItem(i - start, head);
        }

        // Navigation buttons
        if (page > 1) {
            ItemStack prev = createControlItem(Material.ARROW, plugin.getLanguageManager().getMsg("gui_prev"), page - 1);
            inv.setItem(45, prev);
        }
        if (page < maxPage) {
            ItemStack next = createControlItem(Material.ARROW, plugin.getLanguageManager().getMsg("gui_next"), page + 1);
            inv.setItem(53, next);
        }

        player.openInventory(inv);
    }

    public void openPlayerHistoryGui(Player player, OfflinePlayer target, int page) {
        List<CheckManager.CheckRecord> records = plugin.getCheckManager().getHistory(target.getUniqueId());

        int pageSize = 45;
        int maxPage = (int) Math.ceil((double) records.size() / pageSize);
        if (maxPage == 0) maxPage = 1;
        if (page < 1) page = 1;
        if (page > maxPage) page = maxPage;

        String pName = target.getName() != null ? target.getName() : target.getUniqueId().toString();
        String title = ColorUtil.color(plugin.getLanguageManager().getMsg("gui_player_history_title")
                .replace("{player}", pName)
                .replace("{page}", String.valueOf(page)));
        
        Inventory inv = Bukkit.createInventory(null, 54, title);

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, records.size());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        for (int i = start; i < end; i++) {
            CheckManager.CheckRecord record = records.get(i);

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String dateStr = sdf.format(new Date(record.getDate()));
                meta.setDisplayName(ColorUtil.color("&e" + dateStr));
                
                List<String> lore = new ArrayList<>();
                lore.add(ColorUtil.color("&8Staff: &c" + record.getStaffName()));
                lore.add(ColorUtil.color("&8Result: &b" + record.getResult()));
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(i - start, item);
        }

        // Navigation buttons
        if (page > 1) {
            ItemStack prev = createControlItem(Material.ARROW, plugin.getLanguageManager().getMsg("gui_prev"), page - 1);
            inv.setItem(45, prev);
        }
        if (page < maxPage) {
            ItemStack next = createControlItem(Material.ARROW, plugin.getLanguageManager().getMsg("gui_next"), page + 1);
            inv.setItem(53, next);
        }

        player.openInventory(inv);
    }

    private ItemStack createControlItem(Material mat, String name, int targetPage) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.color(name));
            List<String> lore = new ArrayList<>();
            lore.add("Page:" + targetPage);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
