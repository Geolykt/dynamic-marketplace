package org.dynamicmarketplace.dynamicmarketplace;

import net.milkbowl.vault.economy.Economy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynamicmarketplace.dynamicmarketplace.commands.Shop;
import org.dynamicmarketplace.dynamicmarketplace.commands.Worth;
import org.dynamicmarketplace.dynamicmarketplace.savedata.*;

public final class DynamicMarketplace extends JavaPlugin {

    private Economy economy;

    private InputParser inputParser;
    private EcoProcessor processor;

    private Config config;
    private Costs costs;

    // Init
    @Override
    public void onEnable() {

        // Economy
        setupEconomy();

        //Setup resources
        File configFile = new File(getDataFolder(), "CONFIG.txt");
        if (!configFile.exists()) {
            saveResource("CONFIG.txt", false);
            saveResource("costs.yml", false);
        }

        // Save Data
        try {
            config = new Config(getDataFolder(), configFile);
            costs = new Costs(new File(getDataFolder(), "costs.yml"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Commands
        inputParser = new InputParser();
        processor = new EcoProcessor(costs, (1/config.tax), config.tax); // TODO todo

        // setup worth command
        Worth worthCmd = new Worth(processor, economy, costs.getItemNames());
        Shop shopCMD = new Shop(this, costs, processor, economy);

        getCommand("worth").setExecutor(worthCmd);
        getCommand("worth").setTabCompleter(worthCmd);
        getCommand("shop").setExecutor(shopCMD);
        getCommand("shop").setTabCompleter(shopCMD);
    }

    private void setupEconomy () {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        economy = (economyProvider == null) ? null : economyProvider.getProvider();
        if (economy == null) {
            getServer().getPluginManager().disablePlugin(this);
            throw new IllegalStateException("No economy was loaded.");
        }
    }

    // Commands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        int count = 0;

        switch (command.getName().toLowerCase()){
            case "iteminfo":
                if (args.length == 0)
                    return getInfoOnHand(player);
                return getInfoOnItem(player, Material.getMaterial(args[0]));
            case "cost":
                if (args.length == 0)
                    return doCostings(player, player.getInventory().getItemInMainHand().getType(), 1);
                if (args.length == 1)
                    return doCostings(player, Material.getMaterial(args[0]), 1);
                count = inputParser.castInt(args[1], player);
                return doCostings(player, Material.getMaterial(args[0]), count);
            // Purchasing Items
            case "buy":
                if (args.length == 0)
                    return false;
                if (args.length == 1)
                    return Util.purchaseItem(Material.getMaterial(args[0]), 1, player, processor, economy, costs);
                count = inputParser.castInt(args[1], player);
                return count < 1 ? true : Util.purchaseItem(Material.getMaterial(args[0]), count, player, processor, economy, costs);
            case "sell":
                if (args.length == 0 || args[0].equalsIgnoreCase("hand")) {
                    if (args.length < 1) {
                        return sellHand(player.getInventory().getItemInMainHand().getAmount(), player);
                    } else {
                        count = inputParser.castInt(args[0], player);
                        return count < 1 ? true : sellHand(count, player);
                    }
                } else {
                    if (args.length == 1) {
                        return sellItem(Material.getMaterial(args[0]), 1, player);
                    } else {
                        count = inputParser.castInt(args[1], player);
                        return count < 1 ? true : sellItem(Material.getMaterial(args[0]), count, player);
                    }
                }
            case "sellall":
                return sellAll(player);
        }
        return true;
    }

    private boolean getInfoOnHand (Player player) {
        return getInfoOnItem(player, Util.getItemInHand(player));
    }

    private boolean getInfoOnItem (Player player, Material item) {
        if (item == null || !processor.canSell(item)) {
            return true;
        }
//        Double quantity = processor.getShopQuantity(item);
        Double buySingle = processor.getItemBuyPrice(item, 1);
        Double buyStack = processor.getItemBuyPrice(item, 64);
        Double sellSingle = processor.getItemSellPrice(item, 1);
        Double sellStack = processor.getItemSellPrice(item, 64);
        Interactions.itemInfo(item.toString(), player, -1, new double[]{buySingle, buyStack, sellSingle, sellStack});
        return true;
    }

    private boolean doCostings (Player player, Material item, int amount){
        if (!processor.canSell(item)) {
            return true;
        }
        Double buy = processor.getItemBuyPrice(item, amount);
        Double sell = processor.getItemSellPrice(item, amount);
        Interactions.costing(item.toString(), player, amount, buy, sell);
        return true;
    }

    private boolean sellHand (int quantity, Player player) {
        return sellItem(Util.getItemInHand(player), quantity, player);
    }

    private boolean sellItem (Material item, int quantity, Player player) {
        if (!processor.canSell(item)) {
            return true;
        }
        int soldAmount = Util.removePlayerItems(item, quantity, player);
        if (soldAmount == 0){
            Interactions.noItems(item.toString(), player);
            return true;
        }
        double saleprice = processor.getItemSellPrice(item, soldAmount);
        if (soldAmount < quantity) {
            Interactions.saleShortItems(item.toString(), player, soldAmount, saleprice);
        } else {
            Interactions.saleItems(item.toString(), player, soldAmount, saleprice);
        }
        processor.processSupplyIncrease(item, soldAmount);
        economy.depositPlayer(player, saleprice);
        try {
            costs.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean sellAll (Player player) {
        Map<Material,Integer> soldItems = processor.removeAllSellables(player);
        double total = 0;
        int totalCount = 0;
        Interactions.saleHeader(player);
        for (Entry<Material, Integer> entry : soldItems.entrySet()) {
            double salePrice = processor.getItemSellPrice(entry.getKey(), entry.getValue());
            total += salePrice;
            totalCount += entry.getValue();
            Interactions.saleItemsCompact(entry.getKey().toString(), player, entry.getValue(), salePrice);
            processor.processSupplyIncrease(entry.getKey(), entry.getValue());
        }
        Interactions.saleTotal(player, totalCount, total);
        economy.depositPlayer(player, total);
        try {
            costs.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}