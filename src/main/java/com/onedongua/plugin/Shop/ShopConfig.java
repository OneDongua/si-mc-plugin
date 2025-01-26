package com.onedongua.plugin.Shop;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ShopConfig {
    @SerializedName("items")
    private List<ShopItem> items;

    public List<ShopItem> getItems() {
        return items;
    }

    public void setItems(List<ShopItem> items) {
        this.items = items;
    }

    public static class ShopItem {
        @SerializedName("material")
        private String material;

        @SerializedName("amount")
        private int amount;

        @SerializedName("cost")
        private int cost;

        @SerializedName("displayName")
        private String displayName;

        @SerializedName("lore")
        private List<String> lore;

        @SerializedName("isCommand")
        private boolean isCommand;

        @SerializedName("command")
        private String command;

        @SerializedName("hint")
        private String hint;

        public String getMaterial() {
            return material;
        }

        public void setMaterial(String material) {
            this.material = material;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public int getCost() {
            return cost;
        }

        public void setCost(int cost) {
            this.cost = cost;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public List<String> getLore() {
            if (lore.size() == 1 && lore.get(0).isEmpty())
                return new ArrayList<>();
            return lore;
        }

        public void setLore(List<String> lore) {
            if (lore == null)
                lore = new ArrayList<>();
            this.lore = lore;
        }

        public boolean isCommand() {
            return isCommand;
        }

        public void setIsCommand(boolean isCommand) {
            this.isCommand = isCommand;
            if (!isCommand) {
                command = "";
                hint = "";
            }
        }

        public String getCommand() {
            if (command == null) return "";
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public String getHint() {
            if (hint == null) return "";
            return hint;
        }

        public void setHint(String hint) {
            this.hint = hint;
        }
    }
}
