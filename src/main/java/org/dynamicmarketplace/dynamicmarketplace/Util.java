package org.dynamicmarketplace.dynamicmarketplace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.dynamicmarketplace.dynamicmarketplace.savedata.Costs;

import net.milkbowl.vault.economy.Economy;

/**
 * Class holding static utility functions
 * @author Geolykt
 *
 */
public final class Util {

    private Util() {}

    /**
     * Load in a file as a List of lines
     * @param f The file that needs the be read
     * @return An ArrayList of the lines within the file.
     * @throws FileNotFoundException If the source is not found
     * @author eric-robertson
     */
    public static ArrayList<String> getLines (File f) throws FileNotFoundException {
        ArrayList<String> lines = new ArrayList<String> ();
        try (Scanner scanner = new Scanner(f)) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine().trim());
            }
        }
        return lines;
    }

    public static Material getItemInHand(Player p) {
        return p.getInventory().getItemInMainHand().getType();
    }

    /**
     * Gives the player a certain amount of a material kind and then returns how much actually was given to the player.
     *  Will return 0 if quantity &lt; 0 it will also return 0 for airs.
     * @param item The material to give the player
     * @param quantity The amount of the material that should be given to the player
     * @param player The player that should receive the items
     * @return The amount of items that were given to the player.
     */
    public static int getPlayerItems(Material item, int quantity, Player player) {
        if (quantity <= 0 || item.isAir()) {
            return 0;
        }
        int amtFullStacks = Math.floorDiv(quantity, item.getMaxStackSize());
        int leftover = quantity % item.getMaxStackSize();
        ItemStack stacks[] = new ItemStack[leftover == 0 ? amtFullStacks : amtFullStacks + 1];
        ItemStack stack = new ItemStack(item, item.getMaxStackSize());
        for (int i = 0; i < amtFullStacks; i++) {
            stacks[i] = stack;
        }
        if (leftover != 0) {
            stack.setAmount(leftover);
            stacks[amtFullStacks] = stack;
        }
        HashMap<Integer, ItemStack> leftOverItems = player.getInventory().addItem(stacks);
        for (Entry<Integer, ItemStack> leftOverItem : leftOverItems.entrySet()) {
            quantity -= leftOverItem.getValue().getAmount();
        }
        return quantity;
    }

    /**
     * Removes a given amount of an item type within the inventory of the player and returns the amount of the items that it returned.
     * This means that while usually quantity is returned when the player has the required items within the inventory, it may return 0 or other values
     * if the player does not have (enough) items within it&#39;s inventory. The returned value is only above the quantity if quantity is negative, else it is always
     * equal or below it.
     * @param item The type of the item that needs to be removed from the inventory
     * @param quantity The maximum amount that should be removed
     * @param player The player that is subject of the removal
     * @return The amount of items removed from the inventory of the player
     */
    public static int removePlayerItems(Material item, int quantity, Player player) {
        PlayerInventory inv = player.getInventory();
        int first = inv.first(item);
        int removalTarget = quantity;
        while (first != -1 && quantity > 0) {
            int amount = inv.getItem(first).getAmount();
            if (quantity < amount) {
                inv.getItem(first).setAmount(amount - quantity);
                return removalTarget;
            } else {
                quantity -= amount;
                inv.clear(first);
            }
            first = inv.first(item);
        }
        return removalTarget - quantity;
    }

    /**
     * Purchases an item for a player with the given informations.
     *  Also performs economy transactions, verification and 
     *  informing the player on spot.
     *  
     * @param item The type of item to buy
     * @param quantity The quantity to buy
     * @param player The player that buys the item
     * @param processor the economy processor to use
     * @param economy The economy to use
     * @param costs The costs instance to use
     * @return True if it succeeded, false otherwise
     * @since 1.0.0
     */
    public static boolean purchaseItem (Material item, int quantity, Player player, EcoProcessor processor,
            Economy economy, Costs costs) {
        if (!processor.canSell(item)) {
            return true;
        }
        Double cost = processor.getItemBuyPrice(item, quantity);
        Double balance = economy.getBalance(player);
        if (cost > balance) {
            Interactions.itemCostTooMuch(item.toString(), player, quantity, balance, cost );
            return true;
        }
        if (cost <= 0) {
            Interactions.itemsRunOut(item.toString(), player);
            return true;
        }
        int itemsGiven = Util.getPlayerItems(item, quantity, player);
        cost = processor.getItemBuyPrice(item, itemsGiven);
        if (itemsGiven == 0) {
            Interactions.noInventorySpace(item.toString(), player);
            return true;
        } else if (itemsGiven < quantity) {
            Interactions.inventorySpaceLimitBuy(item.toString(), itemsGiven, cost, player);
        } else  {
            Interactions.purchasedItems(item.toString(), quantity, cost, player);
        }
        processor.processDemandIncrease(item, itemsGiven);
        economy.withdrawPlayer(player, cost);
        try {
            costs.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
