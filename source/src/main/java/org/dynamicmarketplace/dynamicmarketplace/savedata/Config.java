package org.dynamicmarketplace.dynamicmarketplace.savedata;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.dynamicmarketplace.dynamicmarketplace.Util;

public class Config{
    public double tax;

    // Initalization
    public Config (File parentFolder, File file) throws FileNotFoundException {
        reset();
        load(parentFolder, file);
    }

    public void reset () {
        tax = 1.03;
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
        switch(key.toLowerCase()){
            case "tax":
                tax = Double.parseDouble(data);
                return;

            default :
                throw new IllegalStateException("Key " + key + " doesn't exist within the main config file.");
        }
    }
}