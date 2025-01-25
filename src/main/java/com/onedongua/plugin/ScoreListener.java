package com.onedongua.plugin;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ScoreListener implements Listener {

    private final ScoreManager scoreManager;
    private final JavaPlugin plugin;

    public ScoreListener(JavaPlugin plugin, ScoreManager scoreManager) {
        this.scoreManager = scoreManager;
        this.plugin = plugin;

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        scoreManager.assignScoreboardToPlayer(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 保存玩家分数
        scoreManager.saveAllScores();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        // 玩家死亡时清零分数
        scoreManager.resetScore(player);
        scoreManager.updateAllScoresOnScoreboard();
    }

}