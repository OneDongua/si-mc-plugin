package com.onedongua.plugin.Score.Listener;

import com.onedongua.plugin.Logger;
import com.onedongua.plugin.Score.KillScoreManager;
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
import org.bukkit.scheduler.BukkitRunnable;

public class KillScoreListener implements Listener {

    private final KillScoreManager scoreManager;
    private final JavaPlugin plugin;
    private final Logger logger;

    public KillScoreListener(JavaPlugin plugin, KillScoreManager scoreManager) {
        this.scoreManager = scoreManager;
        this.plugin = plugin;
        this.logger = new Logger(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        scoreManager.assignScoreboardToPlayer(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage("§2[通知] 击杀分数死亡减半！");
                player.sendMessage("§2[通知] 可通过 /si-shop 或 /sishop 进入击杀分商店");
            }
        }.runTaskLater(plugin, 200);
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
        if ((entity.getType() == EntityType.ZOMBIE ||
                entity.getType() == EntityType.ZOMBIE_VILLAGER ||
                entity.getType() == EntityType.DROWNED)
                && entity instanceof LivingEntity) {
            if (event.getDamager() instanceof Player player) {
                // 给僵尸添加 Metadata，标记最后攻击者
                entity.setMetadata("LastAttacker",
                        new FixedMetadataValue(plugin, player.getName()));
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.ZOMBIE ||
                event.getEntityType() == EntityType.ZOMBIE_VILLAGER ||
                event.getEntityType() == EntityType.DROWNED) {
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
                logger.logf(playerName + " 进入检测2");
                player = Bukkit.getPlayer(playerName);
            }

            if (player != null && player.isOnline()) {
                // 给玩家加分
                int point = scoreManager.getEachPoint();
                scoreManager.addScore(player, point);
                scoreManager.updateAllScoresOnScoreboard();
                player.sendActionBar("§a+" + point);
            }
        }
    }
}