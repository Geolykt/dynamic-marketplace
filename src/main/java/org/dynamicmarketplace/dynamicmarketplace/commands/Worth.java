package org.dynamicmarketplace.dynamicmarketplace.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.dynamicmarketplace.dynamicmarketplace.EcoProcessor;
import org.jetbrains.annotations.NotNull;

import net.milkbowl.vault.economy.Economy;

public class Worth implements CommandExecutor, TabCompleter {

    // stores valid item names
    private final Set<String> itemNames;

    private final EcoProcessor processor;
    private final Economy eco;

    // construct worth command object with a list of valid item names to tab complete with
    public Worth(@NotNull EcoProcessor process, @NotNull Economy economy, Set<String> itemNames) {
        this.itemNames = itemNames;
        this.processor = process;
        this.eco = economy;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Material material;
        int amount = -1;
        if(args.length == 0 || args[0].equalsIgnoreCase("hand")) {
            if (sender instanceof Player) {
                material = ((Player) sender).getInventory().getItemInMainHand().getType();
                amount = ((Player) sender).getInventory().getItemInMainHand().getAmount();
            } else {
                sender.sendMessage(ChatColor.RED + "You must be a player to do this action.");
                return true;
            }
        } else {
            material = Material.matchMaterial(args[0]);
        }
        if (material == null || material.isAir() || amount == 0) {
            sender.sendMessage(ChatColor.RED + "Cannot calcuate the price of air.");
            return true;
        }
        double base = processor.getItemSellPrice(material);
        if (amount != -1) {
            double all = processor.getItemSellPrice(material, amount);
            sender.sendMessage(ChatColor.DARK_GREEN + material.toString() + " " + ChatColor.GREEN + "would be worth " 
                    + ChatColor.DARK_GREEN + eco.format(base) + ChatColor.GREEN + " per item right now. In total your "
                    + ChatColor.DARK_GREEN + amount + ChatColor.GREEN + " of " 
                    + ChatColor.DARK_GREEN + material.toString() + ChatColor.GREEN + " would be worth "
                    + ChatColor.DARK_GREEN + eco.format(all) + ChatColor.GREEN + ".");
        } else {
            sender.sendMessage(ChatColor.DARK_GREEN + material.toString() + " " + ChatColor.GREEN + "would be worth " 
                    + ChatColor.DARK_GREEN + eco.format(base) + ChatColor.GREEN + " per item right now.");
        }
        return true;
    }

    @SuppressWarnings("null")
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
            @NotNull String[] args) {
        if(args.length > 1) { // check that args[1] actually exists
            // copy the item names list, and only return the ones that start with the argument
            return new ArrayList<>(itemNames).stream().filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
        } else if (args.length == 1) {
            return new ArrayList<>(itemNames);
        }
        return null;
    }
    
}