package com.onedongua.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class PlayerEventListener implements Listener {

    private final ScoreManager scoreManager;
    private final JavaPlugin plugin;

    public PlayerEventListener(JavaPlugin plugin, ScoreManager scoreManager) {
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
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.ZOMBIE && entity instanceof LivingEntity) {
            if (event.getDamager() instanceof Player player) {
                // 清除僵尸的 Tag，防止重复计分
                List<String> tagsToRemove = entity.getScoreboardTags().stream()
                        .filter(tag -> tag.startsWith("LastAttacker:"))
                        .toList();

                tagsToRemove.forEach(entity::removeScoreboardTag);
                
                // 给僵尸添加 Tag，标记最后攻击者
                entity.addScoreboardTag("LastAttacker:" + player.getName());
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.ZOMBIE) {
            new FileManager(plugin).log("A zombie died.");
            LivingEntity zombie = event.getEntity();
            // 查找所有 Tags，找出最后攻击者
            zombie.getScoreboardTags().stream()
                    .filter(tag -> tag.startsWith("LastAttacker:"))
                    .findFirst()
                    .ifPresent(tag -> {
                        new FileManager(plugin).log(zombie.getScoreboardTags().toString());
                        // 提取玩家名字
                        String playerName = tag.substring("LastAttacker:".length());
                        Player player = Bukkit.getPlayer(playerName);
                        if (player != null && player.isOnline()) {
                            // 给玩家加分
                            scoreManager.addScore(player, 20);
                            scoreManager.updateAllScoresOnScoreboard();
                        }
                    });
        }
    }
}