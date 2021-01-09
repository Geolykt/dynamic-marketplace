package org.dynamicmarketplace.dynamicmarketplace;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Interactions {

    private static String pluginName = "[DynaMark] ";
    private static String prefix = ChatColor.GREEN + pluginName + ChatColor.WHITE;

    // Utility functions
    private static String formatString ( String message ){
        message = formatClean( message );
        return prefix + message;
    }

    private static String formatClean ( String message ){
        message = message.replaceAll( "\\[([^]]*)\\]", ChatColor.RED + "$1" + ChatColor.WHITE );
        message = message.replaceAll( "\\(([^)]*)\\)", ChatColor.GREEN + "$1" + ChatColor.WHITE );
        message = message.replaceAll( "\\{([^}]*)}", ChatColor.YELLOW + "$1" + ChatColor.WHITE );
        return message;
    }

    // Client Interactions
    public static void intCastFailed ( String number, Player player ) {
        String rawMessage = String.format ("%s is not a valid number", number);
        player.sendMessage( formatString( rawMessage ) );
    }

    public static void intOutOfRange ( String number, Player player ) {
        String rawMessage = String.format ("%s is too big", number);
        player.sendMessage( formatString( rawMessage ) );
    }

    public static void itemLookupFailed ( String item, Player player ){
        String rawMessage = String.format( "Item '%s' not found, ask the server admin to add it to DynaMark ", item);
        player.sendMessage( formatString( rawMessage ) );
    }

    public static void itemInfo ( String item, Player player, double quantity, double[] costs ) {
        player.sendMessage( formatClean( String.format("{--- Dynamic Marketplace} (%s) {---}", item)));
        if ( quantity >= 0 )
            player.sendMessage( formatClean( String.format("Quantity: ~(%.2f) ", quantity)));
        else    
            player.sendMessage( formatClean( String.format("Crafted on purchase")));

        if ( costs[0] < 0 )
            player.sendMessage( formatClean( String.format("Buy  : Not enough left to buy" )));
        else if ( costs[1] < 0)
            player.sendMessage( formatClean( String.format("Buy  : $(%.2f) each", costs[0] )));
        else 
            player.sendMessage( formatClean( String.format("Buy  : $(%.2f) each, $(%.2f) for 64", costs[0], costs[1] )));

        player.sendMessage( formatClean( String.format("Sell  : $(%.2f) each, $(%.2f) for 64", costs[2],costs[3])));
        player.sendMessage( formatClean( String.format("{--------------------------------}")));
    }

    public static void costing ( String item, Player player, double quantity, double buy, double sell ) {
        player.sendMessage( formatClean( String.format("{--- Dynamic Marketplace} (%s) {---}", item)));
        if ( buy < 0 )
            player.sendMessage( formatClean( String.format("Buy  : Not enough left to buy (%d)", (int)buy )));
        else 
            player.sendMessage( formatClean( String.format("Buy  : (%d) for $(%.2f)", (int)quantity, buy )));
        player.sendMessage( formatClean( String.format("Sell  : (%d) for $(%.2f)", (int)quantity, sell )));
        player.sendMessage( formatClean( String.format("{--------------------------------}")));
    }


    // Selling
    
    public static void noItems ( String item, Player player ) {
        String rawMessage = String.format( "You dont have any %s to sell", item);
        player.sendMessage( formatString( rawMessage ) );
    }
    
    public static void itemNotSellable ( Player player ) {
        String rawMessage = String.format( "Cannot sell damaged or enchanted items... yet");
        player.sendMessage( formatString( rawMessage ) );
    }

    public static void saleShortItems ( String item, Player player, double quantity, double sale ) {
        String rawMessage = String.format( "You only had (%d) of (%s), sold them for $(%.2f) ", (int)quantity, item, sale);
        player.sendMessage( formatString( rawMessage ) );
    }
    public static void saleItems ( String item, Player player, double quantity, double sale ) {
        String rawMessage = String.format( "Sold (%d) of (%s) for $(%.2f) ", (int)quantity, item, sale);
        player.sendMessage( formatString( rawMessage ) );
    }
    public static void saleItemsCompact ( String item, Player player, double quantity, double sale ) {
        String rawMessage = String.format( "(%s) (x%d) for $(%.2f) ", item,(int)quantity, sale);
        player.sendMessage( formatClean( rawMessage ) );
    }
    public static void saleHeader ( Player player ) {
        player.sendMessage( formatClean( String.format("{--- Dyanmic Marketplace - Sell All ---}")));
    }
    public static void saleTotal ( Player player, double quantity, double sale ) {
        String rawMessage = String.format( "Total: (%d) items for $(%.2f)", (int)quantity, sale);
        player.sendMessage( formatString( rawMessage ) );
    }
    

    // Buying
    public static void inventorySpaceLimitBuy ( String item, int amount, double price, Player player ) {
        String rawMessage = String.format( "Could only buy (%d) (%s) for $(%.2f) because of limited inventory space", amount, item, price);
        player.sendMessage( formatString( rawMessage ) );
    }
    public static void purchasedItems ( String item, int amount, double price, Player player ) {
        String rawMessage = String.format( "You bought (%d) (%s) for $(%.2f)", amount, item, price);
        player.sendMessage( formatString( rawMessage ) );
    }    public static void itemCostTooMuch ( String item, Player player, double quantity, double bal, double cost ) {
        String rawMessage = String.format( "(%.0f) of (%s) costs $(%.2f) but you only have $(%.2f)", quantity, item, cost, bal);
        player.sendMessage( formatString( rawMessage ) );
    }
    public static void itemsRunOut ( String item, Player player ) {
        String rawMessage = String.format( "there is not enough of (%s) or its raw materials in the store to complete that order", item);
        player.sendMessage( formatString( rawMessage ) );
    }
    public static void noInventorySpace ( String item, Player player ) {
        String rawMessage = String.format( "You need an empty spot in your inventory to buy (%s)", item);
        player.sendMessage( formatString( rawMessage ) );
    }
    
}
