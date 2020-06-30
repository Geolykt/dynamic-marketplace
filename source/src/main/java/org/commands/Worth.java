package org.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class Worth implements CommandExecutor, TabCompleter {

    // stores valid item names
    private Set<String> itemNames;

    // default constructor, should not be used
    public Worth() {
        itemNames = new HashSet<String>();
    }

    // construct worth command object with a list of valid item names to tab complete with
    public Worth(Set<String> itemNames) {
        this.itemNames = itemNames;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(args.length > 0) { // check that args[1] actually exists

            // copy the item names list, and only return the ones that start with the argument
            return new ArrayList<String>(itemNames).stream().filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
        }
        return null;            
    }
    
}