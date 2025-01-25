package com.onedongua.plugin;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class FileManager {

    private final JavaPlugin plugin;
    private final File scoreFile;
    private final File logFile;

    public FileManager(JavaPlugin plugin) {
        this.plugin = plugin;

        plugin.getDataFolder().mkdirs();
        this.logFile = new File(plugin.getDataFolder(), "latest.log");
        this.scoreFile = new File(plugin.getDataFolder(), "player_scores.dat");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!scoreFile.exists()) {
            try {
                scoreFile.createNewFile();
            } catch (IOException e) {
                log(e);
            }
        }
    }

    public int loadPlayerScore(Player player) {
        Map<String, Integer> scores = loadAllScores();
        return scores.getOrDefault(player.getUniqueId().toString(), 0);
    }

    public void savePlayerScore(Player player, int score) {
        Map<String, Integer> scores = loadAllScores();
        scores.put(player.getUniqueId().toString(), score);
        saveAllScores(scores);
    }

    public Map<String, Integer> loadAllScores() {
        if (!scoreFile.exists() || scoreFile.length() == 0)
            return new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(scoreFile))) {
            Object object = ois.readObject();
            if (object instanceof Map) {
                return (Map<String, Integer>) object;
            } else {
                return new HashMap<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            log(e);
            return new HashMap<>();
        }
    }

    public void saveAllScores(Map<String, Integer> scores) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(scoreFile))) {
            oos.writeObject(scores);
        } catch (IOException e) {
            log(e);
        }
    }

    public void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = String.format("[%s] %s", timestamp, message);
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            plugin.getLogger().info(logMessage);
            writer.println(logMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(Exception exception) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        plugin.getLogger().warning("An error occurred");
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            writer.println("[" + timestamp + "] ");
            exception.printStackTrace(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
