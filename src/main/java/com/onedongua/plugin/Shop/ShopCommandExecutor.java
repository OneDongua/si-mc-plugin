package com.onedongua.plugin.Shop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShopCommandExecutor implements CommandExecutor {

    private final KillScoreShop killScoreShop;
    private final ShopClickListener shopClickListener;

    public ShopCommandExecutor(KillScoreShop killScoreShop, ShopClickListener shopClickListener) {
        this.killScoreShop = killScoreShop;
        this.shopClickListener = shopClickListener;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            if (args.length < 1) {
                // 确保命令执行者是玩家
                killScoreShop.openShop(player);  // 执行打开商店的操作
                return true;
            }

            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("reload")
                    && player.hasPermission("siplugin.sishopmanage")) {
               killScoreShop.loadConfig();
               shopClickListener.reloadConfig();
               player.sendMessage("§2商店配置文件已重载!");
               return true;
            }
        }

        return false;
    }
}
