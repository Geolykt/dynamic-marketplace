package org.dynamicmarketplace.dynamicmarketplace.savedata;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Recipies{
 
    private final SingleRecipieFile[] recipieFiles;

    // Initalization
    public Recipies (Collection<File> files) throws FileNotFoundException {
        int recipieFileCount = files.size();
        recipieFiles = new SingleRecipieFile[recipieFileCount];
        int i = 0;
        for (File file : files) {
            recipieFiles[i++] = new SingleRecipieFile(file);
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