package org.dynamicmarketplace.dynamicmarketplace;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.dynamicmarketplace.dynamicmarketplace.savedata.*;

public class Processor {
    
    private Recipies recipies;
    private Costs costs;
    private Config config;

    public Processor ( Recipies recipies, Costs costs, Config config ){
        this.recipies = recipies;
        this.costs = costs;
        this.config = config;
    }

    // Utils

    public String getHeldItem ( Player player ) {
        return player.getInventory().getItemInMainHand().getType().toString().toLowerCase() ;
    }
    
    public boolean canSell ( ItemStack item, boolean silent ){
        boolean damaged = item.getItemMeta() instanceof Damageable && ((Damageable) item.getItemMeta()).hasDamage();
        return item.getEnchantments().isEmpty() && !damaged;
    }
    public int getHandQuantity ( Player player ){
        return player.getInventory().getItemInMainHand().getAmount();
    }

    public boolean isValidItem ( Player player, String item ){
        return isValidItem(player, item, false);
    }
    public boolean isValidItem ( Player player, String item, boolean silent  ) {
        double cost = costs.getCost( item );
        Recipie recipie = recipies.getRecipie( item );
        if ( cost != -1 || recipie != null )
            return true;
        if ( ! silent )
            Interactions.itemLookupFailed(item, player);
        return false;
    }

    // Computations

    public double getShopQuantity ( String item ) {
        double cost = costs.getCost(item);
        if ( cost != -1 )
            return getQuantityFromPrice(cost);
        return -1;
    }
    public double getQuantityFromPrice ( double price ) {
        if ( price == 2 ) return 0;
        return Math.round( 1 / price );
    }
    public double getPriceFromQuantity ( double quantity ) {
        if ( quantity == 0 ) return 2;
        return 1 / quantity;
    }
    public double getBuyPriceFromQuantity ( double base, double purchaseNumber ) {
        if ( purchaseNumber > base ) return -1;
        if ( purchaseNumber == base )
            return Math.log(base) + 1 ;
        return Math.log(base) - Math.log(base - purchaseNumber) ;
    }
    public double getSalePriceFromQuantity ( double base, double saleNumber ) {
        if ( base == 0 ) 
            return Math.log(saleNumber) + 1;
        return Math.log(base + saleNumber) - Math.log(base);
    }

    // Data Lookup

    public double getBuyPrice ( String validItem, double buyAmount ) {
        double price = getBaseBuyPrice(validItem, buyAmount);
        if ( price == 0) Interactions.cannotLoadItemWhenShould(validItem);
        return price * config.tax * config.scalar;
    }
    public double getBaseBuyPrice ( String validItem, double amount ) {

        // Item is a basic item
        double cost = costs.getCost( validItem );
        if ( cost != -1 ){
            double quantity = getQuantityFromPrice( cost );
            return getBuyPriceFromQuantity( quantity, amount); 
        }

        // Item is crafted, add in multiplier data
        Recipie recipie = recipies.getRecipie( validItem );
        if ( recipie != null ){
            double totalCost = 0;
            double multiplier = config.multipliers.get("crafting");
            for( int i = 0 ; i < recipie.items.length; i++ ) {
                if ( config.multipliers.containsKey(recipie.items[i]))
                    multiplier *= config.multipliers.get(recipie.items[i]);
                else{
                    cost = getBaseBuyPrice ( recipie.items[i], recipie.amounts[i] * amount );
                    totalCost += cost;
                    if ( cost == -1 ) return -1;
                }
            }
            return totalCost * multiplier;
        }

        return 0;

    }

    public double getSalePrice ( String validItem, double saleAmount ) {
        double price = getBaseSalePrice(validItem, saleAmount);
        if ( price == 0) Interactions.cannotLoadItemWhenShould(validItem);
        return price * (1/config.tax) * config.scalar;
    }
    public double getBaseSalePrice ( String validItem, double amount ) {

        // Item is a basic item
        double cost = costs.getCost( validItem );
        if ( cost != -1 ){
            double quantity = getQuantityFromPrice( cost );
            return getSalePriceFromQuantity( quantity, amount); 
        }

        // Item is crafted, dont add in multipliers on selling
        Recipie recipie = recipies.getRecipie( validItem );
        if ( recipie != null ){
            double totalCost = 0;
            for( int i = 0 ; i < recipie.items.length; i++ ) {
                if ( !config.multipliers.containsKey(recipie.items[i])) {
                    cost = getBaseSalePrice ( recipie.items[i], recipie.amounts[i] * amount );;
                    totalCost += cost;
                }
            }
            return totalCost;
        }
        return 0;
    }

    // Inventory Lookup

    public Material getItemMaterial ( String item ) {
        String converted = item.replaceAll("([A-Z])", "_$1").toUpperCase();
        return Material.getMaterial( converted );
    }

    public int givePlayerItem ( String item, int amount, Player player ) {
        Material itemMaterial = getItemMaterial(item);
        int stack = itemMaterial.getMaxStackSize();
        int given = 0;
        while ( amount > 0 ){
            if ( player.getInventory().firstEmpty() == -1 ) 
                break;

            int itemsInStack = amount >= stack ? stack : amount;
            ItemStack items = new ItemStack( itemMaterial );
            items.setAmount( itemsInStack);
            player.getInventory().addItem(items);

            given += itemsInStack;
            amount -= itemsInStack;
        }
        return given;
    }

    public int takeItemFromPlayer ( String item, int amount, Player player ) {
            
        Material itemMaterial = getItemMaterial(item);
        int taken = 0;
        for (ItemStack i : player.getInventory()) {
            if ( i == null ) continue;
            if ( ! canSell( i, false )) continue;

            if ( i.getType() == itemMaterial && amount > 0 ) {

                if ( i.getAmount() <= amount ) {
                    taken += i.getAmount();
                    amount -= i.getAmount();
                    player.getInventory().removeItem( i );
                }
                else {
                    i.setAmount( i.getAmount() - amount );
                    return taken + amount;
                }
            }
        }
        return taken;
        
    }

    public HashMap<String,Integer> removeAllValidFromInventory ( Player player ){

        HashMap<String,Integer> itemNumbers = new HashMap<String,Integer>();
        for (ItemStack i : player.getInventory()) {
            if ( i == null ) continue;
            if ( ! canSell(i, true)) continue;
            String itemName = i.getType().name().toLowerCase();
            if ( isValidItem( player, itemName, true )){
                if ( itemNumbers.containsKey(itemName))
                    itemNumbers.put(itemName, itemNumbers.get(itemName) + i.getAmount());
                else
                    itemNumbers.put(itemName, i.getAmount());
                i.setAmount(0);
            }
        }

        return itemNumbers;
    }

    // Quantity Changes

    public void removeItemsFromShop ( String item, double amount ){

        // Item is a basic item
        double cost = costs.getCost( item );
        if ( cost != -1 ){
            double quantity = getQuantityFromPrice( cost );
            double newPrice = getPriceFromQuantity( quantity - amount );
            costs.updateCost(item, newPrice);
        }

        // Item is crafted
        Recipie recipie = recipies.getRecipie( item );
        if ( recipie != null )
            for( int i = 0 ; i < recipie.items.length; i++ ) 
                if ( !config.multipliers.containsKey(recipie.items[i])) {
                    removeItemsFromShop( recipie.items[i], recipie.amounts[i] * amount );
                }
    }

    public void insertItemIntoShop ( String item, double amount ){

        // Item is a basic item
        double cost = costs.getCost( item );
        if ( cost != -1 ){
            double quantity = getQuantityFromPrice( cost );
            double newPrice = getPriceFromQuantity( quantity + amount );
            costs.updateCost(item, newPrice);
        }

        // Item is crafted
        Recipie recipie = recipies.getRecipie( item );
        if ( recipie != null )
            for( int i = 0 ; i < recipie.items.length; i++ ) 
                if ( !config.multipliers.containsKey(recipie.items[i])) {
                    insertItemIntoShop( recipie.items[i], recipie.amounts[i] * amount );
                }
            
        
    }

}