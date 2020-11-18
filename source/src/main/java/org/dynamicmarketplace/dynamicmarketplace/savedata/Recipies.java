package org.dynamicmarketplace.dynamicmarketplace.savedata;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/* ==================================================================
    Recipie Save Data
    - Controlls access and loading of the varius recipies/*.txt files 
    - Reads and interprits file as item name and recipie listings
================================================================== */

public class Recipies{
 
    private SingleRecipieFile[] recipieFiles;
    
    // Initalization

    public Recipies (ArrayList<String> filePaths) throws FileNotFoundException {

        int recipieFileCount = filePaths.size();
        recipieFiles = new SingleRecipieFile[recipieFileCount];
        
        for (int i=0; i < recipieFileCount; i++ ){
            recipieFiles[i] = new SingleRecipieFile(filePaths.get(i));
        }

    }

    // Get Data

    public Recipie getRecipie ( String item ){
        for ( int i=0 ; i<recipieFiles.length ; i++)
            if ( recipieFiles[i].recipies.containsKey( item ))
                return recipieFiles[i].recipies.get(item);
        return null;
    }

    // get all recipe names, for tab completion
    public Set<String> getItemNames() {
        Set<String> names = new HashSet<String>();

        for(SingleRecipieFile recipeFile : recipieFiles) {
            names.addAll(recipeFile.getItemNames());
        }
        return names;
    }
}