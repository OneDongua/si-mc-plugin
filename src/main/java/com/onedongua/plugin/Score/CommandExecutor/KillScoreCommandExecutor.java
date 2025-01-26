package com.onedongua.plugin.Score.CommandExecutor;

import com.onedongua.plugin.Score.KillScoreManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KillScoreCommandExecutor implements CommandExecutor {
    private final KillScoreManager scoreManager;

    public KillScoreCommandExecutor(KillScoreManager scoreManager) {
        this.scoreManager = scoreManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String[] args) {

        if (sender instanceof Player player) {
            // 检查玩家是否拥有权限
            if (!player.hasPermission("siplugin.sikillscore")) {
                player.sendMessage("你没有权限打开击杀分商店！");
                return true;
            }
        }

        if (args.length < 1) {
            return false;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "add", "set":
                if (args.length != 3) return false;

                String playerName = args[1];
                Player player = Bukkit.getPlayer(playerName);

                if (player == null) {
                    sender.sendMessage("§4玩家 " + playerName + " 不在线!");
                    return true;
                }

                int score;
                try {
                    score = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§4请输入有效的分数!");
                    return true;
                }

                switch (subCommand) {
                    case "add":
                        scoreManager.addScore(player, score);
                        scoreManager.updateAllScoresOnScoreboard();
                        sender.sendMessage("§2已为玩家 " + playerName + " 增加 " + score + " 分!");
                        break;

                    case "set":
                        scoreManager.setScore(player, score);
                        scoreManager.updateAllScoresOnScoreboard();
                        sender.sendMessage("§2已将玩家 " + playerName + " 的分数设置为 " + score + "!");
                        break;
                }
                return true;

            case "each":
                if (args.length != 2) return false;

                int point;
                try {
                    point = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§4请输入有效的分数!");
                    return true;
                }

                scoreManager.setEachPoint(point);
                sender.sendMessage("§2已设置击杀分每次增加 " + point + " 分!");
                return true;

            default:
                sender.sendMessage("§4未知的子命令: " + subCommand);
                return false;
        }
    }
}