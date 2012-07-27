package com.comphenix.xpbridge.mods;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import com.comphenix.xp.ActionTypes;
import com.comphenix.xp.Debugger;
import com.comphenix.xp.extra.Permissions;
import com.comphenix.xp.lookup.ItemQuery;
import com.comphenix.xp.mods.BlockResponse;
import com.comphenix.xp.mods.BlockService;
import com.comphenix.xp.parser.ParsingException;
import com.comphenix.xp.parser.text.ItemNameParser;

public class RailCraftBlockService implements BlockService {
	
	public static final String NAME = "RAIL_CRAFT";
	public static String REWARD_PROCESSING = "experiencebridgemod.rewards.processing";
	public static String ACTION_PROCESSING = "PROCESSING";
	
	// Blocks
	private Integer UTILITYLOADERITEM = null;
	
	// IDs
	private static final int COKE_OVEN_BRICK = 7;
	private static final int ROLLING_MACHINE = 8;
	private static final int BLAST_FURNACE = 12;

	private boolean loaded;
	
	public RailCraftBlockService(ItemNameParser parser, Debugger debugger) throws ParsingException {
		
		UTILITYLOADERITEM = parser.getFirstRegistered("UTILITYLOADERITEM");

		if (UTILITYLOADERITEM != null) {
			loaded = true;
		} else {
			debugger.printWarning(this, "Cannot load RailCraft.");
		}
	}
	
	@Override
	public String getServiceName() {
		return NAME;
	}

	public boolean isLoaded() {
		return loaded;
	}

	@Override
	public BlockResponse processClickEvent(InventoryClickEvent event, ItemQuery block) {

		ItemStack current = event.getCurrentItem();
		int rawSlot = event.getRawSlot();
		
		// Railcraft is not loaded
		if (!loaded || block == null) {
			return BlockResponse.FAILURE;
		}
		
		// Coke Oven
		if (block.match(UTILITYLOADERITEM, COKE_OVEN_BRICK)) {
			
			// Two output slots
			if (ItemQuery.hasItems(current) && (rawSlot == 1 || rawSlot == 2))
				return new BlockResponse(InventoryType.FURNACE, ActionTypes.SMELTING, Permissions.REWARDS_SMELTING);
		}
		
		// Blast furnace
		if (block.match(UTILITYLOADERITEM, BLAST_FURNACE)) {
			
			// One output slot
			if (ItemQuery.hasItems(current) && rawSlot == 2)
				return new BlockResponse(InventoryType.FURNACE, ActionTypes.SMELTING, Permissions.REWARDS_SMELTING);
		}
		
		// Rolling machine
		if (block.match(UTILITYLOADERITEM, ROLLING_MACHINE)) {

			// We can't get the inventory content (due to a buggy implementation), nor can we rely on the current
			// item. So we're forced to use the hack all the time, EVEN if the inventory slot is empty.
			// Note, though, that there is spam control - a user can never spawn more than one task per tick.
			
			// One output slot. 
			if (rawSlot == 0) {
				BlockResponse response = new BlockResponse(InventoryType.WORKBENCH, ActionTypes.CRAFTING, Permissions.REWARDS_CRAFTING);
				
				response.setCurrentItem(null);
				response.setOverrideCurrent(true);
				response.setForceHack(true);
				return response;
			}
		}
		
		return BlockResponse.FAILURE;
	}
}
