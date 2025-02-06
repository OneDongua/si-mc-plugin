package com.onedongua.plugin.Score;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.onedongua.plugin.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ScoreFileManager {

    private final JavaPlugin plugin;
    private final Logger logger;
    private final File scoreFile;
    private final File killScoreFile;
    private final File playersFile;
    private final File historyFolder;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public final Map<String, String> players = new HashMap<>();
    private long lastSaveBackupTime = 0;
    private long lastSaveKillBackupTime = 0;

    public ScoreFileManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = new Logger(plugin);

        plugin.getDataFolder().mkdirs();
        this.scoreFile = new File(plugin.getDataFolder(), "player_scores.dat");
        this.killScoreFile = new File(plugin.getDataFolder(), "player_kill_scores.dat");
        this.historyFolder = new File(plugin.getDataFolder(), "history");
        this.playersFile = new File(plugin.getDataFolder(), "players.json");

        if (!scoreFile.exists()) {
            try {
                scoreFile.createNewFile();
            } catch (IOException e) {
                logger.log(e);
            }
        }
        if (!killScoreFile.exists()) {
            try {
                killScoreFile.createNewFile();
            } catch (IOException e) {
                logger.log(e);
            }
        }
        if (!historyFolder.exists()) {
            historyFolder.mkdirs();
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
            logger.log(e);
            return new HashMap<>();
        }
    }

    public void saveAllScores(Map<String, Integer> scores) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(scoreFile))) {
            oos.writeObject(scores);
        } catch (IOException e) {
            logger.log(e);
        }
        long now = System.currentTimeMillis();
        if (now - lastSaveBackupTime > 3600000) {
            saveJsonFile(scores, new File(historyFolder, "scores_" + System.currentTimeMillis() + ".json"));
            lastSaveBackupTime = now;
        }
    }

    public int loadPlayerKillScore(Player player) {
        Map<String, Integer> scores = loadAllKillScores();
        return scores.getOrDefault(player.getUniqueId().toString(), 0);
    }

    public void savePlayerKillScore(Player player, int score) {
        Map<String, Integer> scores = loadAllKillScores();
        scores.put(player.getUniqueId().toString(), score);
        saveAllKillScores(scores);
    }

    public Map<String, Integer> loadAllKillScores() {
        if (!killScoreFile.exists() || killScoreFile.length() == 0)
            return new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(killScoreFile))) {
            Object object = ois.readObject();
            if (object instanceof Map) {
                return (Map<String, Integer>) object;
            } else {
                return new HashMap<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.log(e);
            return new HashMap<>();
        }
    }

    public void saveAllKillScores(Map<String, Integer> scores) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(killScoreFile))) {
            oos.writeObject(scores);
        } catch (IOException e) {
            logger.log(e);
        }
        long now = System.currentTimeMillis();
        if (now - lastSaveKillBackupTime > 3600000) {
            saveJsonFile(scores, new File(historyFolder, "kill_scores_" + System.currentTimeMillis() + ".json"));
            lastSaveKillBackupTime = now;
        }
    }

    private void saveJsonFile(Map<String, Integer> scores, File file) {
        Map<String, Integer> playerScores = new HashMap<>();
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            String name = entry.getKey();
            if (players.containsKey(name))
                name = players.get(name);
            playerScores.put(name,
                    entry.getValue());
        }
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(playerScores, writer);
        } catch (IOException e) {
            logger.log(e);
        }
    }

    public void loadPlayers() {
        if (!playersFile.exists()) {
            try {
                playersFile.createNewFile();
            } catch (IOException e) {
                logger.log(e);
            }
        }
        if (playersFile.length() == 0) return;
        try (FileReader reader = new FileReader(playersFile)) {
            players.putAll(gson.fromJson(reader,
                    new TypeToken<Map<String, String>>() {
                    }.getType()));
            logger.logf(players.toString());
        } catch (IOException e) {
            logger.log(e);
        }
    }

    public void savePlayers() {
        try (FileWriter writer = new FileWriter(playersFile)) {
            gson.toJson(players, writer);
        } catch (IOException e) {
            logger.log(e);
        }
    }
}
