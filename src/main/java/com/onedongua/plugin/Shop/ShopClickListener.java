package com.onedongua.plugin.Shop;

import com.onedongua.plugin.Logger;
import com.onedongua.plugin.Score.KillScoreManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;

public class ShopClickListener implements Listener {

    private final Logger logger;
    private final KillScoreManager killScoreManager;
    private final HashMap<Player, ItemStack> offHandItems;
    private KillScoreShop killScoreShop;
    private ShopConfig shopConfig;

    public ShopClickListener(JavaPlugin plugin,
                             KillScoreManager killScoreManager,
                             KillScoreShop killScoreShop) {
        logger = new Logger(plugin);
        this.killScoreManager = killScoreManager;
        this.killScoreShop = killScoreShop;
        this.shopConfig = killScoreShop.getShopConfig();
        this.offHandItems = new HashMap<>();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // 确保是玩家点击
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // 确保是商店界面
        if (!event.getView().getTitle().equals("§6击杀分兑换商店　")) return;

        // 防止玩家拿取物品
        event.setCancelled(true);

        // 切换副手处理
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        offHandItems.put(player, offHandItem);

        if (event.getClick() == ClickType.SWAP_OFFHAND) {
            ItemStack originalOffHandItem = offHandItems.get(player);
            if (originalOffHandItem != null) {
                player.getInventory().setItemInOffHand(originalOffHandItem);
                offHandItems.remove(player);
            }
            logger.log(player.getName() + " 试图零元购，但他失败了");
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.BARRIER)
            return;

        int killScore = killScoreManager.getScore(player);

        // 兑换物品逻辑
        if (shopConfig != null && shopConfig.getItems() != null) {
            for (ShopConfig.ShopItem shopItem : shopConfig.getItems()) {
                if (clickedItem.getType() == Material.getMaterial(shopItem.getMaterial())) {
                    if (killScore >= shopItem.getCost()) {
                        // 扣分并更新击杀分
                        killScoreManager.addScore(player, -shopItem.getCost());
                        killScoreManager.updateAllScoresOnScoreboard();
                        killScoreManager.notifyShops(player);

                        // 执行兑换
                        if (shopItem.isCommand()) {
                            List<String> commands = shopItem.getCommand();
                            for (String command : commands) {
                                command = replacePlaceholder(command, player.getName());
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                            }
                            player.sendMessage(shopItem.getHint());
                            player.closeInventory();
                        } else {
                            ItemStack itemStack = new ItemStack(Material.valueOf(shopItem.getMaterial()), shopItem.getAmount());
                            player.getInventory().addItem(itemStack);

                            player.sendMessage("§e成功兑换 " + shopItem.getDisplayName() + "！");
                        }

                    } else {
                        player.sendMessage("§4击杀分不足");
                    }
                    return;
                }
            }
        }

        // 如果没有匹配的物品，记录日志
        logger.log(player.getName() + " 尝试兑换未配置的物品: " + clickedItem.getType());
    }

    private String replacePlaceholder(String string, String playerName) {
        return string.replace("%player%", playerName);
    }

    public void reloadConfig() {
        this.shopConfig = killScoreShop.getShopConfig();
    }
}
