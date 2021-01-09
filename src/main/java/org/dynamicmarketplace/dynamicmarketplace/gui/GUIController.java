package org.dynamicmarketplace.dynamicmarketplace.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dynamicmarketplace.dynamicmarketplace.EcoProcessor;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class GUIController {

    public final Map<UUID, String> USERS_SECTIONMAP = new HashMap<>();

    public final Map<String, ArrayList<Material>> SECTION_CONTENTS = new HashMap<>(8);
    public final Map<String, Material> SECTION_ICONS = new HashMap<>(8);
    public final ArrayList<ItemStack> SECTIONS = new ArrayList<>();
    public final ArrayList<String> SECTION_NAMES = new ArrayList<>();
    public final boolean DO_CONTENT_GLINT;

    private static final ItemStack NO_SECTION = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);

    public GUIController(YamlConfiguration sectionsConfig) {
        ConfigurationSection icons = sectionsConfig.getConfigurationSection("icons");
        ConfigurationSection contents = sectionsConfig.getConfigurationSection("contents");
        DO_CONTENT_GLINT = sectionsConfig.getBoolean("contentGlint", true);
        boolean sectionGlint = sectionsConfig.getBoolean("sectionGlint", true);
        if (sectionsConfig.getBoolean("backgroundGlint", true)) {
            ItemMeta itemMeta = NO_SECTION.getItemMeta();
            itemMeta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            NO_SECTION.setItemMeta(itemMeta);
        }
        for (String sectionName : icons.getKeys(false)) {
            Material mat = Material.matchMaterial(icons.getString(sectionName));
            if (mat != null) {
                SECTION_ICONS.put(sectionName, mat);
                ItemStack item = new ItemStack(mat);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(ChatColor.RESET + sectionName);
                if (sectionGlint) {
                    itemMeta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
                    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                item.setItemMeta(itemMeta);
                SECTIONS.add(item);
                SECTION_NAMES.add(sectionName);
                ArrayList<Material> materialContents = new ArrayList<>();
                for (String materialName : contents.getStringList(sectionName)) {
                    materialContents.add(Material.matchMaterial(materialName));
                }
                SECTION_CONTENTS.put(sectionName, materialContents);
            } else {
                System.err.println("Error while creating the DynamicMarket shop GUI: " 
                        + icons.getString(sectionName) + " is not a valid material!");
            }
        }
    }

    /**
     * Utility function to create the section selector within a given inventory.
     * The inventory is cleared during the process and should be 27 slots in size.
     * @param inv The inventory that should be used to create the menu in
     */
    public final void createSectionSelector(Inventory inv) {
        inv.clear();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, NO_SECTION);
        }
        for (int i = 0; i < 9; i++) {
            if (SECTIONS.size() == i) {
                return;
            }
            inv.setItem(i + 9, SECTIONS.get(i));
        }
    }

    /**
     * Utility function to generate a section within a given inventory.
     * The inventory is cleared by performing this action
     * @param sectionNum the section number to generate
     * @param inventory The inventory to generate it in
     * @param processor The EconomyProcessor to obtain the prices of the items from
     * @param the {@link Economy} to use, this is used for formatting the currency
     */
    protected final void genSection(int sectionNum, Inventory inventory, EcoProcessor processor, Economy eco) {
        inventory.clear();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, NO_SECTION);
        }
        ArrayList<Material> toGenerate = SECTION_CONTENTS.get(SECTION_NAMES.get(sectionNum));
        for (int i = 0; i < toGenerate.size(); i++) {
            ItemStack item = new ItemStack(toGenerate.get(i));
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setLore(List.of(ChatColor.GREEN + "Buy price:", 
                    ChatColor.DARK_GREEN.toString() + eco.format(processor.getItemBuyPrice(toGenerate.get(i), 1))
                    + ChatColor.GREEN + " per item"));
            if (DO_CONTENT_GLINT) {
                itemMeta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(itemMeta);
            inventory.setItem(i, item);
        }
    }
}
