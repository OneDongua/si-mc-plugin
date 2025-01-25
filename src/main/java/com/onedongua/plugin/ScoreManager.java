package com.onedongua.plugin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;

public class ScoreManager {

    private final JavaPlugin plugin;
    private final FileManager fileManager;

    private BukkitTask task;
    private final Map<String, Integer> playerScores = new HashMap<>();
    private long period = 100;
    private Scoreboard scoreboard;
    private Objective objective;

    public ScoreManager(JavaPlugin plugin, FileManager fileManager) {
        this.plugin = plugin;
        this.fileManager = fileManager;

        // 初始化计分板
        setupScoreboard();
        loadScores();
    }

    public void startScoreTask() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.isDead()) {
                        addScore(player, 1);
                    }
                }
                updateAllScoresOnScoreboard();
            }
        }.runTaskTimer(plugin, period, period);
    }

    public void stopScoreTask() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    public void setPeriod(long period) {
        this.period = period * 20;
        stopScoreTask();
        startScoreTask();
    }

    public void loadScores() {
        Map<String, Integer> storedScores = fileManager.loadAllScores();
        playerScores.putAll(storedScores);
        updateAllScoresOnScoreboard();
    }

    public void saveAllScores() {
        fileManager.saveAllScores(playerScores);
    }

    public void addScore(Player player, int i) {
        String uuid = player.getUniqueId().toString();
        int score = playerScores.getOrDefault(uuid, 0) + i;
        playerScores.put(uuid, score);
    }

    public void resetScore(Player player) {
        String uuid = player.getUniqueId().toString();
        playerScores.put(uuid, 0);
    }

    public void setScore(Player player, int score) {
        String uuid = player.getUniqueId().toString();
        playerScores.put(uuid, score);
    }

    public int getScore(Player player) {
        return playerScores.getOrDefault(player.getUniqueId().toString(), 0);
    }

    private void setupScoreboard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        scoreboard = manager.getNewScoreboard();
        objective = scoreboard.registerNewObjective("score", "dummy", "积分榜");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void assignScoreboardToPlayer(Player player) {
        player.setScoreboard(scoreboard);
    }

    public void updateAllScoresOnScoreboard() {
        // 清空计分板
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        // 更新所有玩家分数（包括离线玩家）
        for (Map.Entry<String, Integer> entry : playerScores.entrySet()) {
            String playerName = getPlayerName(entry.getKey());
            if (playerName != null) {
                objective.getScore(playerName).setScore(entry.getValue());
            }
        }
    }

    private String getPlayerName(String uuid) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(java.util.UUID.fromString(uuid));
        return offlinePlayer.getName();
    }
}
