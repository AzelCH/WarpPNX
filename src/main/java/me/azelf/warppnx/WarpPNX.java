package me.azelf.warppnx;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;

import java.io.File;

public class WarpPNX extends PluginBase {

    private Config warps;
    private Config config;

    @Override
    public void onEnable() {
        this.saveResource("warps.yml");
        this.saveDefaultConfig();
        this.warps = new Config(new File(getDataFolder(), "warps.yml"), Config.YAML);
        this.config = new Config(new File(getDataFolder(), "config.yml"), Config.YAML);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("warp")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cPlease use this command in game!");
                return true;
            }
            if (args.length == 0) {
                this.warpHelp(sender);
                return true;
            }

            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "create":
                case "add":
                    if (!sender.hasPermission("warppnx.create")) {
                        sender.sendMessage(config.getString("no-permission"));
                        return true;
                    }
                    if (args.length < 2) {
                        sender.sendMessage("Usage: /warp create <warpName>");
                        return true;
                    }
                    String warpName = args[1];
                    if (warps.exists(warpName)) {
                        sender.sendMessage("§cSorry warp with name " + warpName + " already exists.");
                        return true;
                    }
                    Position posCreate = ((Player) sender).getPosition();
                    String worldNameCreate = posCreate.getLevel().getName();
                    warps.set(warpName + ".name", warpName);
                    warps.set(warpName + ".world", worldNameCreate);
                    warps.set(warpName + ".x", posCreate.x);
                    warps.set(warpName + ".y", posCreate.y);
                    warps.set(warpName + ".z", posCreate.z);
                    warps.save();
                    sender.sendMessage(config.getString("warp-create-success").replace("%warp%", warpName));
                    break;

                case "remove":
                case "delete":
                    if (!sender.hasPermission("warppnx.delete")) {
                        sender.sendMessage(config.getString("no-permission"));
                        return true;
                    }
                    if (args.length < 2) {
                        sender.sendMessage("Usage: /warp remove <warpName>");
                        return true;
                    }
                    String warpToRemove = args[1];
                    if (!warps.exists(warpToRemove)) {
                        sender.sendMessage("§cWarp " + warpToRemove + " not found.");
                        return true;
                    }
                    warps.remove(warpToRemove);
                    warps.save();
                    sender.sendMessage(config.getString("warp-remove-success").replace("%warp%", warpToRemove));
                    break;
                    
                case "teleport":
                case "tp":
                    if (args.length < 2) {
                        sender.sendMessage("Usage: /warp teleport <warpName>");
                        return true;
                    }
                    String warpToTeleport = args[1];
                    if (!warps.exists(warpToTeleport)) {
                        sender.sendMessage("§cWarp " + warpToTeleport + " not found.");
                        return true;
                    }
                    double x = warps.getDouble(warpToTeleport + ".x");
                    double y = warps.getDouble(warpToTeleport + ".y");
                    double z = warps.getDouble(warpToTeleport + ".z");
                    String worldName = warps.getString(warpToTeleport + ".world");
                    Level world = this.getServer().getLevelByName(worldName);
                    if (world == null) {
                        sender.sendMessage("§cError: World '" + worldName + "' not found.");
                        return true;
                    }
                    Position posTeleport = new Position(x, y, z, world);
                    ((Player) sender).teleport(posTeleport);
                    if (config.getBoolean("enable-title")) {
                        String title = config.getString("title").replace("%warp%", warpToTeleport);
                        ((Player) sender).sendTitle(title);
                    }
                    break;
                
                default:
                    sender.sendMessage("Usage: /warp <create|remove|teleport> <warpName>");
                    return true;
            }
        }
        return true;
    }

    private void warpHelp(CommandSender sender) {
        sender.sendMessage("§6=== Warp Menu ===");
        sender.sendMessage("§e/warp create <warpName> - Create a new warp");
        sender.sendMessage("§e/warp remove <warpName> - Remove an existing warp");
        sender.sendMessage("§e/warp teleport <warpName> - Teleport to a warp");
        sender.sendMessage("§e/warp list - Show warp list");
    }
}