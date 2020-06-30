package org.dynamicmarketplace.dynamicmarketplace.savedata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/* ==================================================================
    Cost Save Data
    - Controlls access and loading of the varius cost/*.txt files 
    - Reads and interprits file as key double pairs
================================================================== */


public class Costs{
 
    private SingleCostFile[] costFiles;
    private ArrayList<Integer> unSavedCosts = new ArrayList<Integer>();
    
    // Initalization

    public Costs ( ArrayList<String> filePaths ) {

        int costFileCount = filePaths.size();
        costFiles = new SingleCostFile[costFileCount];
        
        for ( int i=0; i < costFileCount; i ++ ){
            costFiles[i] = new SingleCostFile( filePaths.get(i) );
        }

    }

    // Get Data

    public double getCost ( String item ){
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

    public void save () {
        for ( int s : unSavedCosts )
            costFiles[s].save();
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