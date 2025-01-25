package com.onedongua.plugin;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
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
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.ZOMBIE && entity instanceof LivingEntity) {
            if (event.getDamager() instanceof Player player) {
                // 给僵尸添加Metadata，标记最后攻击者
                entity.setMetadata("LastAttacker",
                        new FixedMetadataValue(plugin, player.getName()));
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.ZOMBIE) {
            LivingEntity zombie = event.getEntity();

            // 如果已经处理过，则返回
            if (zombie.hasMetadata("hasHandledDeath")) return;

            // 标记已处理
            zombie.setMetadata("hasHandledDeath", new FixedMetadataValue(plugin, true));

            // 查找是否有 LastAttacker Metadata
            if (zombie.hasMetadata("LastAttacker")) {
                String playerName = zombie.getMetadata("LastAttacker").get(0).asString();
                Player player = Bukkit.getPlayer(playerName);
                if (player != null && player.isOnline()) {
                    // 给玩家加分
                    scoreManager.addScore(player, 20);
                    scoreManager.updateAllScoresOnScoreboard();
                    player.sendActionBar("§a+20");
                }
            }
        }
    }
}