package com.onedongua.plugin.Invincibility;

import com.onedongua.plugin.Logger;
import com.onedongua.plugin.SIPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;

public class InvincibilityListener implements Listener {

    private final SIPlugin plugin;
    private final Logger logger;
    private final HashMap<String, Long> players = new HashMap<>();
    private final HashSet<String> exceptionalPlayers = new HashSet<>();

    public InvincibilityListener(SIPlugin plugin) {
        this.plugin = plugin;
        this.logger = new Logger(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (players.containsKey(player.getUniqueId().toString())) {
            long lastJoinTime = players.get(player.getUniqueId().toString());
            long currentTime = System.currentTimeMillis();
            long timeDiff = currentTime - lastJoinTime;
            if (timeDiff < 60 * 1000) {
                logger.logf("异常玩家 " + player.getName() + " 重新登录时间小于 60 秒");
                exceptionalPlayers.add(player.getUniqueId().toString());
            }
        }

        giveInvincibility(player, 120);
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (exceptionalPlayers.contains(player.getUniqueId().toString())) {
            String command = event.getMessage();
            if (command.toLowerCase().startsWith("/login ")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        clearInvincibility(player);
                        exceptionalPlayers.remove(player.getUniqueId().toString());
                        player.sendMessage("§e[提示] 欢迎回来，你的无敌效果已结束");
                        logger.log("已清除 " + player.getName() + " 的无敌效果");
                    }
                }.runTaskLater(plugin, 100);
            }
        }

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        players.put(player.getUniqueId().toString(), System.currentTimeMillis());
    }

    private void giveInvincibility(Player player, int duration) {
        duration *= 20;
        // 创建无敌效果
        PotionEffect invincibilityEffect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,
                duration, 4, false, false, true);
        player.addPotionEffect(invincibilityEffect);

        // 发送提示信息
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage("你已获得 120 秒无敌效果！");
            }
        }.runTaskLater(plugin, 10);

        // 效果消失提示
        if (!exceptionalPlayers.contains(player.getUniqueId().toString())) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.sendMessage("你的无敌效果已结束，请小心行事！");
                }
            }.runTaskLater(plugin, duration + 10);
        }
    }

    private void clearInvincibility(Player player) {
        player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
    }
}
