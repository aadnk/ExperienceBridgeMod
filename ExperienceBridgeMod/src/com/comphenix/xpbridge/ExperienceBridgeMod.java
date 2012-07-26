package com.comphenix.xpbridge;

import java.util.Collection;
import java.util.logging.Logger;

import net.minecraft.server.Item;
import net.minecraft.server.ItemStack;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.xp.ActionTypes;
import com.comphenix.xp.ExperienceMod;
import com.comphenix.xp.mods.CustomBlockProviders;
import com.comphenix.xp.parser.ParsingException;
import com.comphenix.xp.parser.Utility;
import com.comphenix.xp.parser.text.ItemNameParser;
import com.comphenix.xpbridge.mods.*;

public class ExperienceBridgeMod extends JavaPlugin {

	private ExperienceMod experienceMod;
	private ItemNameParser itemParser;
	private Logger logger;
	
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
			providers.register(new EquivalentExchangeBlockService(types, itemParser, experienceMod));
			providers.setDefaultName(RedPowerBlockService.NAME);
						
		} catch (ParsingException e) {
			logger.severe("Error loading mod blocks: " + e.getMessage());
		}
	}	
	
	@Override
	public void onEnable() {

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
						replace("_", "");                    // Remove underscores
				 
				Collection<Integer> previous = parser.getRegistered(clear);
				
				// Name collision with a block?
				if (previous.size() > 0) {
					if (hasBlockID(previous) && item.id > 256) {
						// Add the item suffix
						clear += "ITEM";
					}
				}
				
				if (clear.length() > 0) {
					register(parser, clear, item.id);
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
		logger.info(String.format("Registering %s with id %d", name, id));
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
