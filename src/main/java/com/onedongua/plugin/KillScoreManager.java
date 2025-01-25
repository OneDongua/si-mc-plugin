package com.onedongua.plugin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.Map;

public class KillScoreManager {

    private final JavaPlugin plugin;
    private final FileManager fileManager;

    private final Map<String, Integer> playerScores = new HashMap<>();
    private Scoreboard scoreboard;
    private Objective objective;

    public KillScoreManager(JavaPlugin plugin, FileManager fileManager, Scoreboard scoreboard) {
        this.plugin = plugin;
        this.fileManager = fileManager;
        this.scoreboard = scoreboard;

        // 初始化计分板
        setupScoreboard();
        loadScores();
    }

    private void setupScoreboard() {
        objective = scoreboard.registerNewObjective("kill_score", "dummy", "击杀分数榜");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void loadScores() {
        Map<String, Integer> storedScores = fileManager.loadAllKillScores();
        playerScores.putAll(storedScores);
        updateAllScoresOnScoreboard();
    }

    public void saveAllScores() {
        fileManager.saveAllKillScores(playerScores);
    }

    public void addScore(Player player, int i) {
        String uuid = player.getUniqueId().toString();
        int score = playerScores.getOrDefault(uuid, 0) + i;
        playerScores.put(uuid, score);
    }

    public void timeScore(Player player, double times) {
        String uuid = player.getUniqueId().toString();
        int score = (int) (playerScores.getOrDefault(uuid, 0) * times);
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

    public void assignScoreboardToPlayer(Player player) {
        player.setScoreboard(scoreboard);
    }

    public void updateAllScoresOnScoreboard() {
        // 清空计分板
        /*for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }*/

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
