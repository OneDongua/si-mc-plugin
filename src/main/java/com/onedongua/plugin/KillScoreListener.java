package com.onedongua.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class KillScoreListener implements Listener {

    private final KillScoreManager scoreManager;
    private final JavaPlugin plugin;

    public KillScoreListener(JavaPlugin plugin, KillScoreManager scoreManager) {
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
        scoreManager.timeScore(player, 0.5);
        scoreManager.updateAllScoresOnScoreboard();
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.ZOMBIE && entity instanceof LivingEntity) {
            if (event.getDamager() instanceof Player player) {
                // 给僵尸添加 Metadata，标记最后攻击者
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

            // 检测 1
            Player player = zombie.getKiller();

            // 检测 2
            if (player == null && zombie.hasMetadata("LastAttacker")) {
                String playerName = zombie.getMetadata("LastAttacker").get(0).asString();
                new FileManager(plugin).log(playerName);
                player = Bukkit.getPlayer(playerName);
            }

            if (player != null && player.isOnline()) {
                // 给玩家加分
                scoreManager.addScore(player, 20);
                scoreManager.updateAllScoresOnScoreboard();
                player.sendActionBar("§a+20");
            }
        }
    }
}