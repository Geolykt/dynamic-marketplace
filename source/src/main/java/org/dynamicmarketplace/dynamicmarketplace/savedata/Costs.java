package org.dynamicmarketplace.dynamicmarketplace.savedata;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class Costs{

    private EnumMap<Material, Double> sellValues = new EnumMap<>(Material.class);
    private EnumMap<Material, Double> demandMod = new EnumMap<>(Material.class);
    private EnumMap<Material, Double> supplyMod = new EnumMap<>(Material.class);
    private EnumMap<Material, String> sectionNames = new EnumMap<>(Material.class);
    private Set<String> itemNames;
    private final ConfigurationSection section;

    // Initialisation
    public Costs (File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        section = config.getConfigurationSection("materials");
        itemNames = section.getKeys(false);
        for (String materialName : itemNames) {
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                continue;
            }
            sellValues.put(material, section.getDouble(materialName + ".cost"));
            demandMod.put(material, section.getDouble(materialName + ".demandprice"));
            supplyMod.put(material, section.getDouble(materialName + ".supplyprice"));
            sectionNames.put(material, materialName);
        }
    }

    // Get Data
    public double getCost (Material item){
        return sellValues.getOrDefault(item, -1.0);
    }

    public void updateCost (Material item, double cost) {
        sellValues.put(item, cost);
        section.set(sectionNames.get(item) + ".cost", cost);
    }

    public void save () throws IOException {
        // FileConfigurations save automatically - however this will be needed once we go into binary storage means.
    }

    // get all item names, for tab completion
    public Set<String> getItemNames() {
        return itemNames;
    }

    public boolean exists(Material material) {
        return sellValues.getOrDefault(material, 0.0) != 0.0;
    }

    public void processDemand(Material item, int amount) {
        updateCost(item, getCost(item) + (amount * demandMod.getOrDefault(item, 0.0)));
    }

    public void processSupply(Material item, int amount) {
        updateCost(item, getCost(item) - (amount * supplyMod.getOrDefault(item, 0.0)));
    }
}