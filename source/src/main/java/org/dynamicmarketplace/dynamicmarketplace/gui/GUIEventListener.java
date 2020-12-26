package org.dynamicmarketplace.dynamicmarketplace.gui;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.dynamicmarketplace.dynamicmarketplace.EcoProcessor;
import org.dynamicmarketplace.dynamicmarketplace.Util;
import org.dynamicmarketplace.dynamicmarketplace.savedata.Costs;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class GUIEventListener implements Listener {

    private final GUIController guiControl;
    private final Costs costs;
    private final Economy economy;
    private final EcoProcessor economyProcessor;

    private final HashMap<UUID, Material> BUY_OPTIONS = new HashMap<>();

    public GUIEventListener(GUIController controller, Costs costProcessor, EcoProcessor ecoProcessor, Economy eco) {
        guiControl = controller;
        costs = costProcessor;
        economy = eco;
        economyProcessor = ecoProcessor;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        guiControl.USERS_SECTIONMAP.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        guiControl.USERS_SECTIONMAP.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryInteractEvent e) {
        if (guiControl.USERS_SECTIONMAP.containsKey(e.getWhoClicked().getUniqueId())) {
            e.setResult(Result.DENY);
            if (e instanceof InventoryClickEvent &&
                    (e.getView().convertSlot(((InventoryClickEvent) e).getRawSlot()) == ((InventoryClickEvent) e).getRawSlot())) {
                String section = guiControl.USERS_SECTIONMAP.remove(e.getWhoClicked().getUniqueId());
                if (section.equals("internal_main_section")) {
                    int sectionNum = ((InventoryClickEvent) e).getRawSlot() % 9;
                    guiControl.genSection(sectionNum, e.getView().getTopInventory(), economyProcessor);
                } else {
                    Material m = guiControl.SECTION_CONTENTS.get(section).get(((InventoryClickEvent) e).getRawSlot());
                    e.getWhoClicked().closeInventory();
                    if (!costs.exists(m)) {
                        Bukkit.getLogger().severe("(DynamicMarket GUI)" + m.name() + " is for sale but does not have a price attached!");
                        return;
                    } else {
                        e.getWhoClicked().sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + 
                                "How much of " + m.toString() + " do you want to buy (type in chat)?");
                        BUY_OPTIONS.put(e.getWhoClicked().getUniqueId(), m);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Material m = BUY_OPTIONS.remove(e.getPlayer().getUniqueId());
        if (m == null) {
            return;
        }
        e.setCancelled(true);
        if (e.getMessage().equalsIgnoreCase("cancel")) {
            return;
        }
        try {
            int num = Integer.valueOf(e.getMessage());
            Util.purchaseItem(m, num, e.getPlayer(), economyProcessor, economy, costs);
        } catch (NumberFormatException expected) {
            e.getPlayer().sendMessage(ChatColor.RED + "This is not a known number! Cancelling...");
        }
    }
}
