package com.onedongua.plugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEventListener implements Listener {

    private final ScoreManager scoreManager;

    public PlayerEventListener(ScoreManager scoreManager) {
        this.scoreManager = scoreManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 加载玩家分数
        scoreManager.loadScore(player);

        // 设置计分板
        scoreManager.setScoreboard(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // 保存玩家分数
        scoreManager.saveScore(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // 玩家死亡时清零分数
        scoreManager.resetScore(player);
    }
}
