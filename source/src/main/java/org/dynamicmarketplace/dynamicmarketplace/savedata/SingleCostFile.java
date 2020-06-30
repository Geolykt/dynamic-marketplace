package org.dynamicmarketplace.dynamicmarketplace.savedata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.text.DecimalFormat;

public class SingleCostFile {

    private File file;
    public HashMap<String, Double> costs;

    // Init

    public SingleCostFile(String filePath) {
        file = Processor.verifyFile(filePath);
        costs = new HashMap<String, Double>();
        System.out.println("[DynaMark] Loading cost file " + filePath);
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
                double cost = Double.parseDouble(_line[1]);

                costs.put(_line[0], cost/100000);

            } catch (Exception e) {
                Processor.loadError(line);
            }

        }
    }

    // Update

    public void updateCost(String item, double cost) {
        costs.put(item, cost);
    }

    // Write out

    public void save() {
        try {        
            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(8);
            FileWriter myWriter = new FileWriter(file);
            for (HashMap.Entry<String, Double> entry : costs.entrySet()) 
                myWriter.write(entry.getKey() + ": " + df.format(entry.getValue()*100000)+ "\n");
            myWriter.close();
        } catch (IOException e) {}
    }

    // get all valid item names (used for tab completion)

    public Set<String> getItemNames() {
        return costs.keySet();
    }
}