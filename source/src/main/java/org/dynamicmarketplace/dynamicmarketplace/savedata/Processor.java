package org.dynamicmarketplace.dynamicmarketplace.savedata;

import java.io.File;
import org.bukkit.Bukkit;
import org.dynamicmarketplace.dynamicmarketplace.Interactions;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class Processor {

    // Verify that a file exists

    public static File verifyFile ( String f ) {
        File file = new File ( f );

        if ( file.exists() ) {
            return file;
        }
        else {
            Interactions.fileNotFound( file.getPath() );
            Bukkit.shutdown();
            return null;
        }
    }

    // Load in a file as a List of lines

    public static ArrayList<String> loadFile (File f) throws FileNotFoundException {
        ArrayList<String> lines = new ArrayList<String> ();
        try (Scanner scanner = new Scanner(f)) {
            while (scanner.hasNextLine()) 
                lines.add(scanner.nextLine().trim());
        }
        return lines;
    }

    public static void loadError ( String line ) {
        Interactions.fileLoadWarning( line );
    }

}