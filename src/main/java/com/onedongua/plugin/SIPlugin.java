package com.onedongua.plugin;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class SIPlugin extends JavaPlugin {

    public ScoreManager scoreManager;
    public KillScoreManager killScoreManager;
    public FileManager fileManager;

    @Override
    public void onEnable() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();

        fileManager = new FileManager(this);
        scoreManager = new ScoreManager(this, fileManager, scoreboard);
        killScoreManager = new KillScoreManager(this, fileManager, scoreboard);

        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(new ScoreListener(this, scoreManager), this);
        Bukkit.getPluginManager().registerEvents(new KillScoreListener(this, killScoreManager), this);
        Bukkit.getPluginManager().registerEvents(new InvincibilityListener(this), this);

        // 启动分数定时任务
        scoreManager.startScoreTask();

        // 注册命令
        registerCommand();

        fileManager.log("SIPlugin 已启用！");
    }

    @Override
    public void onDisable() {
        // 保存玩家分数
        scoreManager.saveAllScores();
        fileManager.log("SIPlugin 已禁用！");
    }

    private void registerCommand() {
        // 注册 si-score 命令
        PluginCommand siScoreCommand = getCommand("si-score");
        if (siScoreCommand != null) {
            siScoreCommand.setExecutor(
                    new ScoreCommandExecutor(scoreManager));
        } else {
            fileManager.log("si-score 命令未找到！");
        }

        // 注册 si-killscore 命令
        PluginCommand siKillScoreCommand = getCommand("si-killscore");
        if (siKillScoreCommand != null) {
            siKillScoreCommand.setExecutor(
                    new KillScoreCommandExecutor(killScoreManager));
        } else {
            fileManager.log("si-killscore 命令未找到！");
        }
    }
}
