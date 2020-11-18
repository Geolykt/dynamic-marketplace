package org.dynamicmarketplace.dynamicmarketplace.savedata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/* ==================================================================
    Cost Save Data
    - Controlls access and loading of the varius cost/*.txt files 
    - Reads and interprits file as key double pairs
================================================================== */


public class Costs{
 
    private SingleCostFile[] costFiles;
    private File[] costFileFiles;
    private ArrayList<Integer> unSavedCosts = new ArrayList<Integer>();

    // Initalization
    public Costs (Collection<File> files) throws FileNotFoundException {
        int costFileCount = files.size();
        costFiles = new SingleCostFile[costFileCount];
        int i = 0;
        for (File file : files) {
            costFiles[i++] = new SingleCostFile(file);
        }
    }

    // Get Data
    public double getCost (String item){
        for ( int i=0 ; i<costFiles.length ; i++)
            if ( costFiles[i].costs.containsKey( item ))
                return costFiles[i].costs.get(item);
        return -1;
    }

    public void updateCost ( String item, double cost ) {
        for ( int i=0 ; i<costFiles.length ; i++)
            if ( costFiles[i].costs.containsKey( item )){
                unSavedCosts.add(i);
                costFiles[i].updateCost(item, cost);
            }
    }

    public void save () throws IOException {
        for (int index : unSavedCosts) {
            costFiles[index].save(costFileFiles[index]);
        }
    }

    // get all item names, for tab completion
    public Set<String> getItemNames() {
        Set<String> names = new HashSet<String>();

        for(SingleCostFile costFile : costFiles) {
            names.addAll(costFile.getItemNames());
        }
        return names;
    }
}