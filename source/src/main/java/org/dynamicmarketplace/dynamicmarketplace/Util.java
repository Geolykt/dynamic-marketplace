package org.dynamicmarketplace.dynamicmarketplace;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Class holding static utility functions
 * @author Geolykt
 *
 */
public class Util {

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
}
