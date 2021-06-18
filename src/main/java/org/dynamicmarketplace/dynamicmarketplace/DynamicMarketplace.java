package org.dynamicmarketplace.dynamicmarketplace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynamicmarketplace.dynamicmarketplace.commands.Shop;
import org.dynamicmarketplace.dynamicmarketplace.commands.Worth;
import org.dynamicmarketplace.dynamicmarketplace.savedata.Config;
import org.dynamicmarketplace.dynamicmarketplace.savedata.Costs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.geolykt.starloader.api.NullUtils;
import net.milkbowl.vault.economy.Economy;

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
        Worth worthCmd = new Worth(getEconomyProcessor(), economy, costs.getItemNames());
        Shop shopCMD = new Shop(this, costs, getEconomyProcessor(), economy);

        PluginCommand worth = getCommand("worth");
        if (worth != null) {
            worth.setExecutor(worthCmd);
            worth.setTabCompleter(worthCmd);
        }
        PluginCommand shop = getCommand("shop");
        if (shop != null) {
            shop.setExecutor(shopCMD);
            shop.setTabCompleter(shopCMD);
        }
    }

    @SuppressWarnings("null")
    private @NotNull EcoProcessor getEconomyProcessor() {
        if (processor == null) {
            throw new IllegalStateException("Eco processor not yet defined!");
        }
        return processor;
    }

    private void setupEconomy () {
        @Nullable RegisteredServiceProvider<@NotNull Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        economy = (economyProvider == null) ? null : economyProvider.getProvider();
        if (economy == null) {
            getServer().getPluginManager().disablePlugin(this);
            throw new IllegalStateException("No economy was loaded.");
        }
    }

    // Commands
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        int count = 0;

        switch (command.getName().toLowerCase()){
            case "iteminfo":
                if (args.length == 0)
                    return getInfoOnHand(player);
                Material material2 = Material.getMaterial(args[0]);
                if (material2 == null) {
                    sender.sendMessage("Argument 0 is not valid.");
                    return true;
                }
                return getInfoOnItem(player, material2);
            case "cost":
                if (args.length == 0)
                    return doCostings(player, player.getInventory().getItemInMainHand().getType(), 1);
                if (args.length == 1) {
                    Material mat = Material.getMaterial(args[0]);
                    if (mat == null) {
                        sender.sendMessage("Argument 0 is not valid.");
                        return true;
                    }
                    return doCostings(player, mat, 1);
                }
                count = inputParser.castInt(args[1], player);
                Material mat1 = Material.getMaterial(args[0]);
                if (mat1 == null) {
                    sender.sendMessage("Argument 0 is not valid.");
                    return true;
                }
                return doCostings(player, mat1, count);
            // Purchasing Items
            case "buy":
                if (args.length == 0)
                    return false;
                if (args.length == 1) {
                    Material mat = Material.getMaterial(args[0]);
                    if (mat == null) {
                        sender.sendMessage("Argument 0 is not valid.");
                        return true;
                    }
                    return Util.purchaseItem(mat, 1, player, getEconomyProcessor(), NullUtils.requireNotNull(economy), NullUtils.requireNotNull(costs));
                }
                count = inputParser.castInt(args[1], player);
                Material material = Material.getMaterial(args[0]);
                if (material == null) {
                    sender.sendMessage("Argument 0 is not valid.");
                    return true;
                }
                return count < 1 ? true : Util.purchaseItem(material, count, player, getEconomyProcessor(), NullUtils.requireNotNull(economy), NullUtils.requireNotNull(costs));
            case "sell":
                if (args.length == 0 || args[0].equalsIgnoreCase("hand")) {
                    if (args.length < 1) {
                        return sellHand(player.getInventory().getItemInMainHand().getAmount(), player);
                    } else {
                        count = inputParser.castInt(args[0], player);
                        return count < 1 ? true : sellHand(count, player);
                    }
                } else {
                    Material mat = Material.getMaterial(args[0]);
                    if (mat == null) {
                        sender.sendMessage("Argument 0 is not valid.");
                        return true;
                    }
                    if (args.length == 1) {
                        return sellItem(mat, 1, player);
                    } else {
                        count = inputParser.castInt(args[1], player);
                        return count < 1 ? true : sellItem(mat, count, player);
                    }
                }
            case "sellall":
                return sellAll(player);
        }
        return true;
    }

    private boolean getInfoOnHand (@NotNull Player player) {
        return getInfoOnItem(player, Util.getItemInHand(player));
    }

    private boolean getInfoOnItem (@NotNull Player player, @NotNull Material item) {
        if (!processor.canSell(item)) {
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

    private boolean doCostings (@NotNull Player player, @NotNull Material item, int amount){
        if (!processor.canSell(item)) {
            return true;
        }
        Double buy = processor.getItemBuyPrice(item, amount);
        Double sell = processor.getItemSellPrice(item, amount);
        Interactions.costing(item.toString(), player, amount, buy, sell);
        return true;
    }

    private boolean sellHand (int quantity, @NotNull Player player) {
        return sellItem(Util.getItemInHand(player), quantity, player);
    }

    private boolean sellItem (@NotNull Material item, int quantity, @NotNull Player player) {
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

    private boolean sellAll (@NotNull Player player) {
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
