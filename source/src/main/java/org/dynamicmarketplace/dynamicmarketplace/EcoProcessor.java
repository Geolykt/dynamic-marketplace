package org.dynamicmarketplace.dynamicmarketplace;

import java.util.EnumMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.dynamicmarketplace.dynamicmarketplace.savedata.Costs;
import org.dynamicmarketplace.dynamicmarketplace.savedata.Recipies;

public class EcoProcessor {

    private final Costs costs;
    private final Recipies recipies;
    private final double buyModifier;
    private final double sellModifier;

    public EcoProcessor(Costs costs, Recipies recipies, double sellMod, double buyMod) {
        this.costs = costs;
        this.recipies = recipies;
        this.buyModifier = buyMod;
        this.sellModifier = sellMod;
    }

    public double getItemSellPrice(Material material) {
        return costs.getCost(material) * sellModifier;
    }

    public double getItemSellPrice(Material material, int amount) {
        return costs.getCost(material) * amount * sellModifier; // FIXME fix
    }

    public double getItemBuyPrice(Material material) {
        return costs.getCost(material) * buyModifier;
    }

    public double getItemBuyPrice(Material material, int amount) {
        return costs.getCost(material) * amount * buyModifier; // FIXME fix
    }

    public boolean canSell(Material material) {
        return costs.exists(material);
    }

    /**
     * Processes the increase of demand within for a certain item. This is commonly used to tell the plugin to adjust prices after a player has bought an item.
     * @param item The item whose demand increased
     * @param amount The amount of item added to the demand pool
     */
    public void processDemandIncrease(Material item, int amount) {
        costs.processDemand(item, amount);
    }

    /**
     * Processes the increase of supply within for a certain item. This is commonly used to tell the plugin to adjust prices after a player has sold an item.
     * @param item The item whose supply increased
     * @param amount The amount of item added to the supply pool
     */
    public void processSupplyIncrease(Material item, int amount) {
        costs.processSupply(item, amount);
    }

    /**
     * Removes all sellable Itemstacks within the inventory of a player.
     * @param player The player that is victim of the removal
     * @return A Map that notes how many and which items were removed
     */
    public EnumMap<Material, Integer> removeAllSellables(Player player) {
        EnumMap<Material, Integer> map = new EnumMap<>(Material.class);
        PlayerInventory inv = player.getInventory();
        ItemStack[] stacks = inv.getContents();
        for (int i = 0; i < stacks.length; i++) {
            ItemStack stack = stacks[i];
            if (stack == null || !canSell(stack.getType())) {
                continue;
            }
            map.put(stack.getType(), stack.getAmount());
            inv.clear(i);
        }
        return map;
    }
}
