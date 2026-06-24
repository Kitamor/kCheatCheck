package com.kitamor.kcheatcheck.listener;

import com.kitamor.kcheatcheck.KCheatCheck;
import com.kitamor.kcheatcheck.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private final KCheatCheck plugin;

    public PlayerListener(KCheatCheck plugin) {
        this.plugin = plugin;
    }

    private void cancelIfChecked(org.bukkit.event.Cancellable event, Player player) {
        if (plugin.getCheckManager().isChecked(player.getUniqueId())) {
            event.setCancelled(true);
            String msg = plugin.getLanguageManager().getMsg("cannot_do_while_checked");
            if (msg != null && !msg.isEmpty()) {
                player.sendMessage(ColorUtil.color(plugin.getLanguageManager().getMsg("prefix") + msg));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
            event.getFrom().getBlockY() != event.getTo().getBlockY() ||
            event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            
            if (plugin.getCheckManager().isChecked(event.getPlayer().getUniqueId())) {
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) { cancelIfChecked(event, event.getPlayer()); }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) { cancelIfChecked(event, event.getPlayer()); }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) { cancelIfChecked(event, event.getPlayer()); }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) { cancelIfChecked(event, event.getPlayer()); }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            cancelIfChecked(event, (Player) event.getEntity());
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            cancelIfChecked(event, (Player) event.getDamager());
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (plugin.getCheckManager().isChecked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            String msg = plugin.getLanguageManager().getMsg("cannot_do_while_checked");
            if (msg != null && !msg.isEmpty()) {
                event.getPlayer().sendMessage(ColorUtil.color(plugin.getLanguageManager().getMsg("prefix") + msg));
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.getCheckManager().isChecked(player.getUniqueId())) {
            event.setCancelled(true);
            
            String msg = event.getMessage();
            String formatted = ColorUtil.color(plugin.getLanguageManager().getMsg("chat_format_target")
                    .replace("{player}", player.getName())
                    .replace("{message}", msg));
            
            player.sendMessage(formatted);
            
            String staffName = plugin.getCheckManager().getCheckingStaff(player.getUniqueId());
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getUniqueId().equals(player.getUniqueId())) {
                    continue;
                }
                if (p.hasPermission("kcheatcheck.admin") || p.getName().equalsIgnoreCase(staffName)) {
                    p.sendMessage(formatted);
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.getCheckManager().isChecked(player.getUniqueId())) {
            String msg = plugin.getLanguageManager().getMsg("quit_while_checked")
                    .replace("{player}", player.getName());
            
            String prefix = plugin.getLanguageManager().getMsg("prefix");
            Bukkit.broadcast(ColorUtil.color(prefix + msg), "kcheatcheck.admin");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getCheckManager().isChecked(player.getUniqueId())) {
            String msg = plugin.getLanguageManager().getMsg("join_while_checked")
                    .replace("{player}", player.getName());
            
            String prefix = plugin.getLanguageManager().getMsg("prefix");
            Bukkit.broadcast(ColorUtil.color(prefix + msg), "kcheatcheck.admin");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        String genTitleBase = ColorUtil.color(plugin.getLanguageManager().getMsg("gui_title").replace("{page}", ""));
        
        String pHistTitleFormat = ColorUtil.color(plugin.getLanguageManager().getMsg("gui_player_history_title"));
        String pHistBase1 = "";
        String pHistBase2 = "";
        if (pHistTitleFormat.contains("{player}") && pHistTitleFormat.contains("{page}")) {
            pHistBase1 = pHistTitleFormat.substring(0, pHistTitleFormat.indexOf("{player}"));
            pHistBase2 = pHistTitleFormat.substring(pHistTitleFormat.indexOf("{player}") + "{player}".length(), pHistTitleFormat.indexOf("{page}"));
        }

        if (title.startsWith(genTitleBase)) {
            event.setCancelled(true);
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            if (clicked.getType() == Material.ARROW && clicked.hasItemMeta() && clicked.getItemMeta().hasLore()) {
                String pageStr = clicked.getItemMeta().getLore().get(0).replace("Page:", "");
                try {
                    int page = Integer.parseInt(pageStr);
                    if (event.getWhoClicked() instanceof Player) {
                        plugin.getGuiManager().openHistoryGui((Player) event.getWhoClicked(), page);
                    }
                } catch (NumberFormatException ignored) {}
            }
        } else if (!pHistBase1.isEmpty() && title.startsWith(pHistBase1) && title.contains(pHistBase2)) {
            event.setCancelled(true);
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            if (clicked.getType() == Material.ARROW && clicked.hasItemMeta() && clicked.getItemMeta().hasLore()) {
                String pageStr = clicked.getItemMeta().getLore().get(0).replace("Page:", "");
                try {
                    int page = Integer.parseInt(pageStr);
                    
                    int startIdx = pHistBase1.length();
                    int endIdx = title.indexOf(pHistBase2);
                    String targetName = title.substring(startIdx, endIdx);
                    
                    org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
                    
                    if (event.getWhoClicked() instanceof Player) {
                        plugin.getGuiManager().openPlayerHistoryGui((Player) event.getWhoClicked(), target, page);
                    }
                } catch (Exception ignored) {}
            }
        }
    }
}
