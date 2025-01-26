package com.onedongua.plugin;

import com.onedongua.plugin.Invincibility.InvincibilityListener;
import com.onedongua.plugin.Score.*;
import com.onedongua.plugin.Score.CommandExecutor.KillScoreCommandExecutor;
import com.onedongua.plugin.Score.CommandExecutor.ScoreCommandExecutor;
import com.onedongua.plugin.Score.Listener.KillScoreListener;
import com.onedongua.plugin.Score.Listener.ScoreListener;
import com.onedongua.plugin.Shop.KillScoreShop;
import com.onedongua.plugin.Shop.ShopClickListener;
import com.onedongua.plugin.Shop.ShopCommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class SIPlugin extends JavaPlugin {

    private Logger logger;
    public ScoreManager scoreManager;
    public KillScoreManager killScoreManager;
    public ScoreFileManager fileManager;
    public KillScoreShop killScoreShop;
    public ShopClickListener shopClickListener;

    @Override
    public void onEnable() {
        logger = new Logger(this);

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();

        fileManager = new ScoreFileManager(this);
        scoreManager = new ScoreManager(this, fileManager, scoreboard);
        killScoreManager = new KillScoreManager(this, fileManager, scoreboard);

        killScoreShop = new KillScoreShop(this, killScoreManager);
        shopClickListener = new ShopClickListener(this, killScoreManager, killScoreShop);

        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(new ScoreListener(this, scoreManager), this);
        Bukkit.getPluginManager().registerEvents(new KillScoreListener(this, killScoreManager), this);
        Bukkit.getPluginManager().registerEvents(new InvincibilityListener(this), this);
        Bukkit.getPluginManager().registerEvents(shopClickListener, this);

        // 启动分数定时任务
        scoreManager.startScoreTask();

        // 注册命令
        registerCommand();

        logger.log("SIPlugin 已启用！");
    }

    @Override
    public void onDisable() {
        // 保存玩家分数
        scoreManager.saveAllScores();
        killScoreManager.saveAllScores();
        logger.log("SIPlugin 已禁用！");
    }

    private void registerCommand() {
        // 注册 si-score 命令
        PluginCommand siScoreCommand = getCommand("si-score");
        if (siScoreCommand != null) {
            siScoreCommand.setExecutor(
                    new ScoreCommandExecutor(scoreManager));
        } else {
            logger.log("si-score 命令未找到！");
        }

        // 注册 si-killscore 命令
        PluginCommand siKillScoreCommand = getCommand("si-killscore");
        if (siKillScoreCommand != null) {
            siKillScoreCommand.setExecutor(
                    new KillScoreCommandExecutor(killScoreManager));
        } else {
            logger.log("si-killscore 命令未找到！");
        }

        // 注册 si-killscore 命令
        PluginCommand siShopCommand = getCommand("si-shop");
        if (siShopCommand != null) {
            siShopCommand.setExecutor(
                    new ShopCommandExecutor(killScoreShop, shopClickListener));
        } else {
            logger.log("si-shop 命令未找到！");
        }
    }
}
