package org.dynamicmarketplace.dynamicmarketplace.savedata;

import org.dynamicmarketplace.dynamicmarketplace.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;


/* ==================================================================
    Config Save Data
    - Controlls access and loading of the CONFIG.txt file
    - Reads and interprits file
================================================================== */

public class Config{
 
    public ArrayList<String> recipieFiles;
    public ArrayList<String> costFiles;
    public HashMap<String, Double> multipliers;
    public double scalar;
    public double tax;

    private File file;
    
    // Initalization
    public Config ( String filePath ) throws FileNotFoundException {
        reset();
        load();
    }

    public void reset () {
        recipieFiles = new ArrayList<String>();
        costFiles = new ArrayList<String>();
        multipliers = new HashMap<String, Double>();
        tax = 1.03;
        scalar = 1000;
    }

    // Load data 

    public void load () throws FileNotFoundException {
        ArrayList<String> lines = Processor.loadFile( file );
        
        for ( String line : lines ) {

            if (line.length() == 0 ) continue;
            if (line.charAt(0) == '#' ) continue;

            String[] _line = line.split("\\s*:\\s*");

            try{
                recieveLineData( _line[0], _line[1] );
            }
            catch(Exception e) {
                 Processor.loadError(line);
            }
        }
    }

    private void recieveLineData ( String key, String data ){
        
        switch( key.toLowerCase() ){

            case "recipies":
                recipieFiles.add( data );
                return;

            case "costs":
                costFiles.add( data );
                return;

            case "tax":
                tax = Double.parseDouble( data );
                return;

            case "quantityscalar":
                scalar = Double.parseDouble( data );
                return;

            case "multiplier":
                String[] _data = data.split(" ");
                multipliers.put( _data[0], Double.parseDouble(_data[1]));
                return;
            
            default :
                Interactions.fileUnknownKey(key, "Config");

        }
    }

    // Save Data
    
    // TODO: Add some save functionality?
    // Currently no plans to have the config file change dynamicly
    public void save () {}

}