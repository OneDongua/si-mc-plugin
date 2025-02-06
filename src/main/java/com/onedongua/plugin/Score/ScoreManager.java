package com.onedongua.plugin.Score;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;

public class ScoreManager {

    private final JavaPlugin plugin;
    private final ScoreFileManager fileManager;
    public final Map<String, String> players;

    private BukkitTask task;
    private final Map<String, Integer> playerScores = new HashMap<>();
    private long period = 100;
    private final Scoreboard scoreboard;
    private Objective objective;
    private int timerPoint = 1;

    public ScoreManager(JavaPlugin plugin, ScoreFileManager fileManager, Scoreboard scoreboard) {
        this.plugin = plugin;
        this.fileManager = fileManager;
        this.scoreboard = scoreboard;
        players = fileManager.players;

        // 初始化计分板
        setupScoreboard();
        loadScores();

        backup();
    }

    private void backup() {
        long currentTimeMillis = System.currentTimeMillis(); // 当前时间戳
        long nextHourMillis = ((currentTimeMillis / 3600000) + 1) * 3600000; // 计算下一个整点时间
        long delayMillis = nextHourMillis - currentTimeMillis; // 计算剩余时间
        long delayTicks = delayMillis / 50; // 转换为 Minecraft Tick（1秒 = 20 Tick）

        new BukkitRunnable() {
            @Override
            public void run() {
                saveAllScores();
                fileManager.savePlayers();
                backup(); // 继续安排下一个整点任务
            }
        }.runTaskLater(plugin, delayTicks);
    }

    private void setupScoreboard() {
        objective = scoreboard.registerNewObjective("score", "dummy", "积分榜");
        objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
    }

    public void startScoreTask() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.isDead() && !player.getScoreboardTags().contains("escaped")) {
                        addScore(player, timerPoint);
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

    public int getTimerPoint() {
        return timerPoint;
    }

    public void setTimerPoint(int timerPoint) {
        this.timerPoint = timerPoint;
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
