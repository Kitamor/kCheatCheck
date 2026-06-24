package com.kitamor.kcheatcheck.command;

import com.kitamor.kcheatcheck.KCheatCheck;
import com.kitamor.kcheatcheck.manager.CheckManager;
import com.kitamor.kcheatcheck.manager.LocationManager;
import com.kitamor.kcheatcheck.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

public class KontCommand implements CommandExecutor, TabCompleter {

    private final KCheatCheck plugin;

    public KontCommand(KCheatCheck plugin) {
        this.plugin = plugin;
    }

    private String getMsg(String path) {
        return getMsg(null, path);
    }

    private String getMsg(CommandSender sender, String path) {
        if (sender instanceof Player) {
            return ColorUtil.color(plugin.getLanguageManager().getMsg((Player) sender, path));
        }
        return ColorUtil.color(plugin.getLanguageManager().getMsg(path));
    }

    private String getSubCmd(String path) {
        return plugin.getConfig().getString("commands.subcommands." + path, path);
    }

    private String getMainCmd() {
        return plugin.getConfig().getString("commands.main", "kont");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("kcheatcheck.admin")) {
            sender.sendMessage(getMsg("no_permission"));
            return true;
        }

        String cmdAl = getSubCmd("al");
        String cmdKonus = getSubCmd("konus");
        String cmdBitir = getSubCmd("bitir");
        String cmdListe = getSubCmd("liste");
        String cmdAktifListe = getSubCmd("aktifliste");
        String cmdGecmis = getSubCmd("gecmis");
        String cmdReload = getSubCmd("reload");
        String cmdAlanEkle = getSubCmd("alanekle");
        String cmdAlanSil = getSubCmd("alansil");
        String prefix = getMsg("prefix");

        if (args.length == 0) {
            sender.sendMessage(prefix + ColorUtil.color("&cKullanım: /" + getMainCmd() + " <" + cmdAl + "|" + cmdKonus + "|" + cmdBitir + "|" + cmdListe + "|" + cmdAktifListe + "|" + cmdGecmis + "|" + cmdAlanEkle + "|" + cmdAlanSil + "|" + cmdReload + ">"));
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);

        if (subCommand.equalsIgnoreCase(cmdReload)) {
            plugin.getLanguageManager().reload();
            sender.sendMessage(prefix + getMsg("reload_success"));
            return true;
        }

        if (subCommand.equalsIgnoreCase(cmdAlanEkle)) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Sadece oyuncular kullanabilir.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(prefix + getMsg("usage_alanekle").replace("{command}", getMainCmd()).replace("{subcommand}", cmdAlanEkle));
                return true;
            }
            String areaName = args[1];
            plugin.getLocationManager().addArea(areaName, ((Player) sender).getLocation());
            sender.sendMessage(prefix + getMsg("area_added").replace("{area}", areaName));
            return true;
        }

        if (subCommand.equalsIgnoreCase(cmdAlanSil)) {
            if (args.length < 2) {
                sender.sendMessage(prefix + getMsg("usage_alansil").replace("{command}", getMainCmd()).replace("{subcommand}", cmdAlanSil));
                return true;
            }
            String areaName = args[1];
            if (plugin.getLocationManager().getArea(areaName) == null) {
                sender.sendMessage(prefix + getMsg("area_not_found"));
                return true;
            }
            plugin.getLocationManager().removeArea(areaName);
            sender.sendMessage(prefix + getMsg("area_removed").replace("{area}", areaName));
            return true;
        }

        if (subCommand.equalsIgnoreCase(cmdListe)) {
            if (sender instanceof Player) {
                plugin.getGuiManager().openHistoryGui((Player) sender, 1);
            } else {
                sender.sendMessage("Sadece oyuncular GUI'yi görebilir.");
            }
            return true;
        }

        if (subCommand.equalsIgnoreCase(cmdAl)) {
            if (args.length < 2) {
                sender.sendMessage(prefix + getMsg("usage_al").replace("{command}", getMainCmd()).replace("{subcommand}", cmdAl));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(prefix + getMsg("player_not_found"));
                return true;
            }

            CheckManager manager = plugin.getCheckManager();
            if (manager.isChecked(target.getUniqueId())) {
                sender.sendMessage(prefix + getMsg("check_already_in"));
                return true;
            }

            String selectedArea = null;
            Location areaLoc = null;
            Set<String> areas = plugin.getLocationManager().getAreas();
            if (areas != null && !areas.isEmpty()) {
                for (String area : areas) {
                    boolean occupied = false;
                    for (CheckManager.ActiveCheck active : manager.getActiveChecks().values()) {
                        if (area.equals(active.getAreaName())) {
                            occupied = true;
                            break;
                        }
                    }
                    if (!occupied) {
                        selectedArea = area;
                        areaLoc = plugin.getLocationManager().getArea(area);
                        break;
                    }
                }
            }

            String targetOriginalLocStr = null;
            String staffOriginalLocStr = null;

            if (selectedArea != null && areaLoc != null) {
                targetOriginalLocStr = LocationManager.serialize(target.getLocation());
                target.teleport(areaLoc);
                if (sender instanceof Player) {
                    staffOriginalLocStr = LocationManager.serialize(((Player) sender).getLocation());
                    ((Player) sender).teleport(areaLoc);
                }
            } else if (areas != null && !areas.isEmpty()) {
                sender.sendMessage(prefix + getMsg("areas_full"));
            }

            manager.startCheck(target.getUniqueId(), sender.getName(), targetOriginalLocStr, staffOriginalLocStr, selectedArea);
            sender.sendMessage(prefix + getMsg("check_started").replace("{player}", target.getName()));
            target.sendMessage(prefix + getMsg("check_started_target").replace("{staff}", sender.getName()));

        } else if (subCommand.equalsIgnoreCase(cmdKonus)) {
            if (args.length < 3) {
                sender.sendMessage(prefix + getMsg("usage_konus").replace("{command}", getMainCmd()).replace("{subcommand}", cmdKonus));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(prefix + getMsg("player_not_found"));
                return true;
            }

            CheckManager manager = plugin.getCheckManager();
            if (!manager.isChecked(target.getUniqueId())) {
                sender.sendMessage(prefix + getMsg("check_not_in"));
                return true;
            }

            String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            String formatted = getMsg("chat_format_staff").replace("{player}", sender.getName()).replace("{message}", message);
            target.sendMessage(formatted);
            
            if (!(sender instanceof Player)) {
                sender.sendMessage(formatted);
            }
            
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getUniqueId().equals(target.getUniqueId())) {
                    continue;
                }
                if (p.hasPermission("kcheatcheck.admin") || (sender instanceof Player && p.getUniqueId().equals(((Player) sender).getUniqueId()))) {
                    p.sendMessage(formatted);
                }
            }

        } else if (subCommand.equalsIgnoreCase(cmdBitir)) {
            if (args.length < 3) {
                sender.sendMessage(prefix + getMsg("usage_bitir").replace("{command}", getMainCmd()).replace("{subcommand}", cmdBitir));
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            CheckManager manager = plugin.getCheckManager();
            
            if (!manager.isChecked(target.getUniqueId())) {
                sender.sendMessage(prefix + getMsg("check_not_in"));
                return true;
            }

            String result = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            CheckManager.ActiveCheck check = manager.endCheck(target.getUniqueId(), sender.getName(), result);
            
            if (target.isOnline() && target.getPlayer() != null) {
                Player tp = target.getPlayer();
                tp.sendMessage(prefix + getMsg("check_ended_target").replace("{result}", result));
                
                if (check.getTargetOriginalLoc() != null) {
                    Location loc = LocationManager.deserialize(check.getTargetOriginalLoc());
                    if (loc != null) tp.teleport(loc);
                }
            }

            if (sender instanceof Player && check.getStaffOriginalLoc() != null) {
                Location loc = LocationManager.deserialize(check.getStaffOriginalLoc());
                if (loc != null) ((Player) sender).teleport(loc);
            }

            sender.sendMessage(prefix + getMsg("check_ended").replace("{player}", target.getName() != null ? target.getName() : args[1]).replace("{result}", result));

        } else if (subCommand.equalsIgnoreCase(cmdAktifListe)) {
            CheckManager manager = plugin.getCheckManager();
            Map<UUID, CheckManager.ActiveCheck> activeChecks = manager.getActiveChecks();
            
            if (activeChecks.isEmpty()) {
                sender.sendMessage(prefix + getMsg("list_empty"));
                return true;
            }

            int page = 1;
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {}
            }

            List<Map.Entry<UUID, CheckManager.ActiveCheck>> list = new ArrayList<>(activeChecks.entrySet());
            int pageSize = 10;
            int maxPage = (int) Math.ceil((double) list.size() / pageSize);
            
            if (page < 1 || page > maxPage) {
                sender.sendMessage(prefix + getMsg("invalid_page"));
                return true;
            }

            sender.sendMessage(getMsg("list_header").replace("{page}", String.valueOf(page)).replace("{max_page}", String.valueOf(maxPage)));
            
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, list.size());
            
            for (int i = start; i < end; i++) {
                Map.Entry<UUID, CheckManager.ActiveCheck> entry = list.get(i);
                OfflinePlayer op = Bukkit.getOfflinePlayer(entry.getKey());
                String pName = op.getName() != null ? op.getName() : entry.getKey().toString();
                String area = entry.getValue().getAreaName() != null ? entry.getValue().getAreaName() : "Yok";
                sender.sendMessage(getMsg("list_format").replace("{player}", pName).replace("{staff}", entry.getValue().getStaffName()).replace("{area}", area));
            }

        } else if (subCommand.equalsIgnoreCase(cmdGecmis)) {
            if (args.length < 2) {
                sender.sendMessage(prefix + getMsg("usage_gecmis").replace("{command}", getMainCmd()).replace("{subcommand}", cmdGecmis));
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            CheckManager manager = plugin.getCheckManager();
            List<CheckManager.CheckRecord> history = manager.getHistory(target.getUniqueId());

            if (history.isEmpty()) {
                sender.sendMessage(prefix + getMsg("history_empty"));
                return true;
            }

            if (sender instanceof Player) {
                plugin.getGuiManager().openPlayerHistoryGui((Player) sender, target, 1);
            } else {
                sender.sendMessage(getMsg("history_header").replace("{player}", target.getName() != null ? target.getName() : args[1]));
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                for (CheckManager.CheckRecord record : history) {
                    String dateStr = sdf.format(new Date(record.getDate()));
                    sender.sendMessage(getMsg("history_format")
                            .replace("{date}", dateStr)
                            .replace("{staff}", record.getStaffName())
                            .replace("{result}", record.getResult()));
                }
            }

        } else {
            sender.sendMessage(prefix + getMsg("unknown_subcommand"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("kcheatcheck.admin")) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();
        
        String cmdAl = getSubCmd("al");
        String cmdKonus = getSubCmd("konus");
        String cmdBitir = getSubCmd("bitir");
        String cmdListe = getSubCmd("liste");
        String cmdAktifListe = getSubCmd("aktifliste");
        String cmdGecmis = getSubCmd("gecmis");
        String cmdReload = getSubCmd("reload");
        String cmdAlanEkle = getSubCmd("alanekle");
        String cmdAlanSil = getSubCmd("alansil");

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList(cmdAl, cmdKonus, cmdBitir, cmdListe, cmdAktifListe, cmdGecmis, cmdReload, cmdAlanEkle, cmdAlanSil);
            for (String sub : subCommands) {
                if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase(Locale.ROOT);
            if (subCommand.equalsIgnoreCase(cmdAl) || subCommand.equalsIgnoreCase(cmdKonus) || subCommand.equalsIgnoreCase(cmdBitir) || subCommand.equalsIgnoreCase(cmdGecmis)) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(p.getName());
                    }
                }
            } else if (subCommand.equalsIgnoreCase(cmdAlanSil)) {
                Set<String> areas = plugin.getLocationManager().getAreas();
                if (areas != null) {
                    for (String area : areas) {
                        if (area.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(area);
                        }
                    }
                }
            }
        }

        return completions;
    }
}
