package org.dynamicmarketplace.dynamicmarketplace.savedata;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import org.dynamicmarketplace.dynamicmarketplace.Util;

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
        recipieFiles = new ArrayList<>();
        costFiles = new ArrayList<>();
        multipliers = new HashMap<>();
        tax = 1.03;
        scalar = 1000;
    }

    // Load data 
    public void load (File parentFolder, File file) throws FileNotFoundException {
        ArrayList<String> lines = Util.getLines(file);
        for (String line : lines) {
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
}