package com.onedongua.plugin;

import org.bukkit.Bukkit;
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
    private final Map<Player, Integer> playerScores = new HashMap<>();
    private long period = 100;

    public ScoreManager(JavaPlugin plugin, FileManager fileManager) {
        this.plugin = plugin;
        this.fileManager = fileManager;
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
                        increaseScore(player);
                    }
                }
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

    public void loadScore(Player player) {
        int score = fileManager.loadPlayerScore(player);
        playerScores.put(player, score);
    }

    public void saveScore(Player player) {
        fileManager.savePlayerScore(player, getScore(player));
    }

    public void saveAllScores() {
        for (Map.Entry<Player, Integer> entry : playerScores.entrySet()) {
            fileManager.savePlayerScore(entry.getKey(), entry.getValue());
        }
    }

    public void increaseScore(Player player) {
        int score = getScore(player) + 1;
        playerScores.put(player, score);
        updateScoreboard(player);
    }

    public void increaseScore(Player player, int amount) {
        int score = getScore(player) + amount;
        playerScores.put(player, score);
        updateScoreboard(player);
    }

    public void resetScore(Player player) {
        playerScores.put(player, 0);
        updateScoreboard(player);
    }

    public void setScore(Player player, int score) {
        playerScores.put(player, score);
        updateScoreboard(player);
    }

    public int getScore(Player player) {
        return playerScores.getOrDefault(player, 0);
    }

    public void setScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("score", "dummy", "积分榜");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(scoreboard);
    }

    private void updateScoreboard(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective("score");
        if (objective != null) {
            objective.getScore(player.getName()).setScore(getScore(player));
        }
    }

}
