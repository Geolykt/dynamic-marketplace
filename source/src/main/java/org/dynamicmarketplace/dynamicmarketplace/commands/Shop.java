package org.dynamicmarketplace.dynamicmarketplace.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.dynamicmarketplace.dynamicmarketplace.DynamicMarketplace;
import org.dynamicmarketplace.dynamicmarketplace.EcoProcessor;
import org.dynamicmarketplace.dynamicmarketplace.gui.GUIController;
import org.dynamicmarketplace.dynamicmarketplace.gui.GUIEventListener;
import org.dynamicmarketplace.dynamicmarketplace.savedata.Costs;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class Shop implements CommandExecutor, TabCompleter {

    private final GUIController controller;
    private final GUIEventListener listener;

    public Shop(DynamicMarketplace plugin, Costs costs, EcoProcessor ecoProcessor, Economy eco) {
        File file = new File(plugin.getDataFolder(), "guiConfig.yml");
        if (!file.exists()) {
            plugin.saveResource("guiConfig.yml", false);
        }
        controller = new GUIController(YamlConfiguration.loadConfiguration(file));
        listener = new GUIEventListener(controller, costs, ecoProcessor, eco);
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>(0);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player in order to use this command!");
            return true;
        }
        controller.USERS_SECTIONMAP.put(((Player) sender).getUniqueId(), "internal_main_section");
        Inventory inv = Bukkit.createInventory((Player) sender, 27);
        controller.createSectionSelector(inv);
        return true;
    }

}
