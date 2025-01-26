package com.onedongua.plugin.Invincibility;

import com.onedongua.plugin.SIPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class InvincibilityListener implements Listener {

    private final SIPlugin plugin;

    public InvincibilityListener(SIPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        giveInvincibility(player);
    }

    private void giveInvincibility(Player player) {
        int duration = 120 * 20;
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
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage("你的无敌效果已结束，请小心行事！");
            }
        }.runTaskLater(plugin, duration);
    }
}
