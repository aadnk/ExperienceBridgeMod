package com.comphenix.xpbridge;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.server.Item;
import net.minecraft.server.ItemStack;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.xp.ActionTypes;
import com.comphenix.xp.ExperienceMod;
import com.comphenix.xp.mods.CustomBlockProviders;
import com.comphenix.xp.parser.ParsingException;
import com.comphenix.xp.parser.TextParser;
import com.comphenix.xp.parser.Utility;
import com.comphenix.xp.parser.text.ItemNameParser;
import com.comphenix.xpbridge.mods.*;

public class ExperienceBridgeMod extends JavaPlugin {

	private static final String EXP_BRIDGE_COMMAND = "experiencebridgemod";
	private static final String EXP_BRIDGE_ALIAS = "expbridge";
	private static final String SUB_COMMAND_GET = "get";
	
	private ExperienceMod experienceMod;
	private ItemNameParser itemParser;
	private Logger logger;
	
	// ID to Name mapping
	private Map<Integer, String> registered = new HashMap<Integer, String>();
	
	@Override
	public void onLoad() {

		// Load logger
		logger = getLogger();

		// Load mod
		experienceMod = loadExperienceMod(logger);
		
		// Any failures?
		if (experienceMod == null) {
			return;
		}

		// Load item parser
		itemParser = experienceMod.getConfigLoader().getNameParser();
		
		// Custom items and blocks
		loadItemsAndBlocks(itemParser);
		
		// Register crafting and furnace handlers
		try {
			CustomBlockProviders providers = experienceMod.getCustomBlockProvider();
			ActionTypes types = experienceMod.getActionTypes();
			
			providers.register(new RedPowerBlockService(itemParser, experienceMod));
			providers.register(new IndustrialCraftBlockService(types, itemParser, experienceMod));
			providers.register(new IndustrialExtensionBlockService(itemParser,experienceMod));
			providers.register(new RailCraftBlockService(itemParser, experienceMod));
			providers.register(new EquivalentExchangeBlockService(types, itemParser, experienceMod));
			providers.register(new BuildCraftBlockService(itemParser, experienceMod));
			providers.setDefaultName(RedPowerBlockService.NAME);
			
		} catch (ParsingException e) {
			logger.severe("Error loading mod blocks: " + e.getMessage());
		}
	}	
	
	@Override
	public void onEnable() {

	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (sender != null && label.equalsIgnoreCase(EXP_BRIDGE_COMMAND) ||
				              label.equalsIgnoreCase(EXP_BRIDGE_ALIAS)) {
			
			// Check the parameter count
			if (args.length == 2) {
				
				if (args[0].equalsIgnoreCase(SUB_COMMAND_GET)) {
					
					Integer id = TextParser.tryParse(args[1]);
					
					// Display registered items
					if (id != null) {
						sender.sendMessage("Registered name: " + registered.get(id));
					} else {
						sender.sendMessage("Item or block was not found.");
					}
					
				} else {
					sender.sendMessage("Unknown subcommand " + args[0]);
				}
				
			} else {
				sender.sendMessage("Must have two parameters.");
			}
			
			return true;
		}
		
		// We cannot handle that command
		return false;
	}
	
	// Initialize basic support for modded items
	private void loadItemsAndBlocks(ItemNameParser parser) {

		// Add every item to the parser
		for (Item item : Item.byId) {
			if (item != null) {
				ItemStack stack = new ItemStack(item);
				
				String rawName = Utility.getEnumName(stack.k());

				// Alternative name
				if (rawName.length() == 0) {
					rawName = Utility.getEnumName(item.l());
				}
				
				// Remove noise
				String clear = rawName.
						replaceFirst("NAME+$", "").          // Remove suffix
						replaceFirst("^(TILE|ITEM)+", "").   // Remove tile or item in front
						replace("_", "");                    // Underscores
				 
				// Really?
				if (clear.equalsIgnoreCase("NULL")) {
					clear = Utility.getEnumName(item.a(stack));
				}
				
				// Are we left with a valid name
				if (!clear.matches("[0-9]+") && clear.length() > 0) {

					Collection<Integer> previous = parser.getRegistered(clear);
					
					// Name collision with a block?
					if (previous.size() > 0) {
						if (hasBlockID(previous) && item.id > 256) {
							// Add the item suffix
							clear += "ITEM";
						}
					}
					
					register(parser, clear, item.id);
					
				} else {
		
					// Damn it
					logger.info("Cannot register item " + item.id + ": No valid name found.");
				}
				
			}
		}
	}
	
	// See if we have a block ID here
	private boolean hasBlockID(Collection<Integer> list) {
		
		for (Integer id : list) {
			if (id < 256)
				return true;
		}
		
		return false;
	}

	private void register(ItemNameParser parser, String name, int id) {
		
		// Register 
		parser.register(name, id);
		
		// Info
		registered.put(id, name);
	}
	
	private ExperienceMod loadExperienceMod(Logger logger) {
		
		PluginManager manager = getServer().getPluginManager();
		
		// First, load experience mod
		Plugin plugin = manager.getPlugin("ExperienceMod");
		
		// It's either not there, or not the correct plugin
		if (plugin instanceof ExperienceMod) {
			return (ExperienceMod) plugin;
		} else if (plugin == null) {
			// Unlikely, but we'll check anyway
			logger.severe("Cannot find ExperienceMod. Quitting.");
		} else {
			logger.severe("Loaded ExperienceMod is corrupt/invalid. Quitting.");
		}
		
		// Failure
		return null;
	}
}
