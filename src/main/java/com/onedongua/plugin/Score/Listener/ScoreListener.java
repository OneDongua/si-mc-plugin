package com.onedongua.plugin.Score.Listener;

import com.onedongua.plugin.Score.ScoreManager;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

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
        scoreManager.players.put(player.getUniqueId().toString(), player.getName());
        scoreManager.assignScoreboardToPlayer(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage("§2[通知] 生存积分已迁移至 Tab 列表！");
            }
        }.runTaskLater(plugin, 200);

        // 校准
        int ticksSinceDeath = player.getStatistic(Statistic.TIME_SINCE_DEATH);
        int score = (int) (ticksSinceDeath / 20.0 / 5.0);
        if (Math.abs(score - scoreManager.getScore(player)) > 60) {
            scoreManager.setScore(player, score);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 保存玩家分数
        scoreManager.saveAllScores();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!player.getScoreboardTags().contains("escaped")) {
            // 玩家死亡时清零分数
            scoreManager.resetScore(player);
            scoreManager.updateAllScoresOnScoreboard();
        }
    }
}