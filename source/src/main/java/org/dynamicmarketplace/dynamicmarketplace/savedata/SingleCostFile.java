package org.dynamicmarketplace.dynamicmarketplace.savedata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.text.DecimalFormat;

public class SingleCostFile {

    public HashMap<String, Double> costs;

    // Init
    public SingleCostFile(File file) throws FileNotFoundException {
        costs = new HashMap<String, Double>();
        System.out.println("[DynaMark] Loading cost file " + file.getPath());
        load(file);
    }

    // Load data
    public void load(File file) throws FileNotFoundException {
        ArrayList<String> lines = Processor.loadFile(file);
        for (String line : lines) {
            if (line.length() == 0 || line.charAt(0) == '#')
                continue;
            String[] _line = line.split("\\s*:\\s*");
            double cost = Double.parseDouble(_line[1]);
            costs.put(_line[0], cost / 100000);
        }
    }

    // Update
    public void updateCost(String item, double cost) {
        costs.put(item, cost);
    }

    // Write out
    public void save(File file) throws IOException {
        DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(8);
        FileWriter myWriter = new FileWriter(file);
        for (HashMap.Entry<String, Double> entry : costs.entrySet()) {
            myWriter.write(entry.getKey() + ": " + df.format(entry.getValue()*100000)+ "\n");
        }
        myWriter.close();
    }

    // get all valid item names (used for tab completion)

    public Set<String> getItemNames() {
        return costs.keySet();
    }
}