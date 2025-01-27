package com.onedongua.plugin.Shop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.onedongua.plugin.Logger;
import com.onedongua.plugin.Score.KillScoreManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class KillScoreShop {

    private final KillScoreManager killScoreManager;
    private Inventory shopInventory;
    private int killScore;
    private final HashMap<Player, Inventory> playerInventories = new HashMap<>();
    private final JavaPlugin plugin;
    private final Logger logger;
    private ShopConfig shopConfig;

    public KillScoreShop(JavaPlugin plugin, KillScoreManager killScoreManager) {
        this.killScoreManager = killScoreManager;
        this.plugin = plugin;
        logger = new Logger(plugin);
        killScoreManager.addShop(this);

        loadConfig();
    }

    public void openShop(Player player) {
        // 创建箱子界面
        shopInventory = Bukkit.createInventory(null, 54, "§6击杀分兑换商店　");

        // 获取玩家的击杀分
        killScore = killScoreManager.getScore(player);

        // 显示玩家当前的击杀分
        ItemStack killScoreItem = new ItemStack(Material.PAPER);
        updateKillScoreItem(killScoreItem);
        shopInventory.setItem(49, killScoreItem); // 中间位置显示玩家的击杀分

        // 添加商店物品
        if (shopConfig != null && shopConfig.getItems() != null) {
            for (int i = 0; i < Math.min(shopConfig.getItems().size(), 45); i++) {
                ShopConfig.ShopItem shopItem = shopConfig.getItems().get(i);
                Material material = Material.getMaterial(shopItem.getMaterial());
                if (material != null) {
                    ItemStack itemStack = new ItemStack(material, shopItem.getAmount());
                    updateShopItem(shopItem, itemStack);
                    shopInventory.setItem(i, itemStack);
                } else {
                    ItemStack errorItemStack = new ItemStack(Material.BARRIER);
                    ItemMeta meta = errorItemStack.getItemMeta();
                    meta.setDisplayName("§4加载错误");
                    meta.setLore(List.of("§6请联系管理员进行修复"));
                    errorItemStack.setItemMeta(meta);
                    shopInventory.setItem(i, errorItemStack);
                    logger.log("§4警告：未找到物品\"" + shopItem.getMaterial() + "\"");
                    logger.log("§4请检查配置文件 shop.json");
                }
            }
        }

        // 打开商店界面
        player.openInventory(shopInventory);
        playerInventories.put(player, shopInventory); // 保存玩家的商店界面
    }

    public void updateShop(Player player) {
        Inventory shopInventory = playerInventories.get(player);
        if (shopInventory != null) {
            killScore = killScoreManager.getScore(player);

            // 更新击杀分显示
            ItemStack killScoreItem = shopInventory.getItem(49);
            updateKillScoreItem(killScoreItem);

            // 更新商店物品
            if (shopConfig != null && shopConfig.getItems() != null) {
                for (int i = 0; i < Math.min(shopConfig.getItems().size(), 45); i++) {
                    ShopConfig.ShopItem shopItem = shopConfig.getItems().get(i);
                    ItemStack itemStack = shopInventory.getItem(i);
                    updateShopItem(shopItem, itemStack);
                    shopInventory.setItem(i, itemStack);
                }
            }
        }
    }

    private void updateShopItem(ShopConfig.ShopItem shopItem, ItemStack itemStack) {
        if (itemStack != null) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§r" + shopItem.getDisplayName());
                ArrayList<String> loreList = new ArrayList<>();
                loreList.addAll(Arrays.asList(
                        shopItem.isCommand() ? "§5技能" : "§6物品",
                        killScore >= shopItem.getCost() ? "§a可购买" : "§4击杀分不足",
                        "§e需要 " + shopItem.getCost() + " 击杀分"));
                loreList.addAll(shopItem.getLore());
                meta.setLore(loreList);
                itemStack.setItemMeta(meta);
            }
        }
    }

    private void updateKillScoreItem(ItemStack killScoreItem) {
        if (killScoreItem != null) {
            ItemMeta meta = killScoreItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§r当前击杀分: " + killScore);  // 在 ShopClickListener 中有使用该字符串
                killScoreItem.setItemMeta(meta);
            }

        }
    }

    public void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "shop.json");
        if (!configFile.exists()) {
            saveDefaultConfig(configFile);
        }

        try (FileReader reader = new FileReader(configFile)) {
            Gson gson = new Gson();
            Type type = new TypeToken<ShopConfig>() {
            }.getType();
            shopConfig = gson.fromJson(reader, type);
        } catch (IOException e) {
            logger.log(e);
        }
    }

    private void saveDefaultConfig(File configFile) {
        ShopConfig defaultConfig = new ShopConfig();
        defaultConfig.setItems(new ArrayList<>());

        ShopConfig.ShopItem gunPowderItem = new ShopConfig.ShopItem();
        gunPowderItem.setIsCommand(false);
        gunPowderItem.setMaterial("GUNPOWDER");
        gunPowderItem.setAmount(16);
        gunPowderItem.setCost(100);
        gunPowderItem.setDisplayName("火药 x16");
        gunPowderItem.setLore(List.of(""));

        defaultConfig.getItems().add(gunPowderItem);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(defaultConfig, writer);
        } catch (IOException e) {
            logger.log(e);
        }
    }

    public ShopConfig getShopConfig() {
        return shopConfig;
    }
}
