package org.dynamicmarketplace.dynamicmarketplace.savedata;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class Config{
 
    public ArrayList<File> recipieFiles;
    public ArrayList<File> costFiles;
    public HashMap<String, Double> multipliers;
    public double scalar;
    public double tax;

    // Initalization
    public Config (File parentFolder, File file) throws FileNotFoundException {
        reset();
        load(parentFolder, file);
    }

    public void reset () {
        recipieFiles = new ArrayList<File>();
        costFiles = new ArrayList<File>();
        multipliers = new HashMap<String, Double>();
        tax = 1.03;
        scalar = 1000;
    }

    // Load data 

    public void load (File parentFolder, File file) throws FileNotFoundException {
        ArrayList<String> lines = Processor.loadFile( file );
        for ( String line : lines ) {
            if (line.length() == 0 || line.charAt(0) == '#') continue;
            String[] _line = line.split("\\s*:\\s*");
            recieveLineData(parentFolder, _line[0], _line[1]);
        }
    }

    private void recieveLineData (File parentFolder, String key, String data ){

        switch( key.toLowerCase() ){

            case "recipies":
                recipieFiles.add(new File(parentFolder, data));
                return;

            case "costs":
                costFiles.add(new File(parentFolder, data));
                return;

            case "tax":
                tax = Double.parseDouble(data);
                return;

            case "quantityscalar":
                scalar = Double.parseDouble(data);
                return;

            case "multiplier":
                String[] splitData = data.split(" ");
                multipliers.put( splitData[0], Double.parseDouble(splitData[1]));
                return;

            default :
                throw new IllegalStateException("Key " + key + " doesn't exist within the main config file.");
        }
    }

    // Save Data
    
    // TODO: Add some save functionality?
    // Currently no plans to have the config file change dynamically
    public void save () {}

}