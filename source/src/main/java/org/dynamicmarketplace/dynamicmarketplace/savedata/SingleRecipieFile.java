package org.dynamicmarketplace.dynamicmarketplace.savedata;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class SingleRecipieFile {

    private File file;
    public HashMap<String, Recipie> recipies;

    // Init

    public SingleRecipieFile(String filePath) {
        file = Processor.verifyFile(filePath);
        recipies = new HashMap<String, Recipie>();
        System.out.println("[DynaMark] Loading recipie file " + filePath);
        load();
    }

    // Load data

    public void load() {
        ArrayList<String> lines = Processor.loadFile(file);
        for (String line : lines) {

            if (line.length() == 0)
                continue;
            if (line.charAt(0) == '#')
                continue;

            try {

                String[] _line = line.split("\\s*:\\s*");
                String[] components = _line[1].split("\\s*,\\s*");
                Recipie recipie = new Recipie(components.length);

                for (int i = 0; i < components.length; i++) {
                    String[] costs = components[i].split("\\s+");
                    recipie.pushItem(costs[0], Double.parseDouble(costs[1]));
                }

                recipies.put(_line[0], recipie);

            } catch (Exception e) {
                Processor.loadError(line);
            }

        }
    }

    // get all valid item names (used for tab completion)

    public Set<String> getItemNames() {
        return recipies.keySet();
    }
}