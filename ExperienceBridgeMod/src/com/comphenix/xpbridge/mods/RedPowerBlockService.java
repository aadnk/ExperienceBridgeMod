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

public class RedPowerBlockService implements BlockService {

	public static String NAME = "RED_POWER_2";
	
	private Integer REDPOWER_PROCESS_BLOCK = null;

	// Constants from RedPower
	private int ALLOY_FURNACE = 0;
	private int BLUELECTRIC_FURNACE = 1;
	private int PROJECT_TABLE = 3;
	private int BLUELECTRIC_ALLOY_FURNACE = 4;

	private Debugger debugger;
	
	// Whether or not RedPower was successfully found
	private boolean loaded;
	
	public RedPowerBlockService(ItemNameParser parser, Debugger debugger) throws ParsingException {

		this.debugger = debugger;
		this.REDPOWER_PROCESS_BLOCK = parser.getFirstRegistered("RPAFURNACE");

		if (REDPOWER_PROCESS_BLOCK != null) {
			loaded = true;
		} else {
			debugger.printWarning(this, "Cannot load RedPower.");
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
		
		InventoryType type = event.getInventory().getType();
		ItemStack current = event.getCurrentItem();
		int rawSlot = event.getRawSlot();
	
		// Detect no redpower mod
		if (!loaded || block == null) {
			return BlockResponse.FAILURE;
		}
		
		// Print slot and inventory type'
		debugger.printDebug(this, "Raw: %d, Type: %s, Block: %s", rawSlot, type, block);
	
		// Check inventory type and filter out click events on empty slots
		boolean standardChecks = 
				type == InventoryType.CHEST && ItemQuery.hasItems(current);
		
		// Alloy furnance
		if (block.match(REDPOWER_PROCESS_BLOCK, ALLOY_FURNACE)) {
			if (standardChecks && rawSlot == 10)
				return new BlockResponse(InventoryType.FURNACE, ActionTypes.SMELTING, Permissions.REWARDS_SMELTING);
		}
		
		// Bluelectric furnace
		if (block.match(REDPOWER_PROCESS_BLOCK, BLUELECTRIC_FURNACE)) {
			if (standardChecks && rawSlot == 1)
				return new BlockResponse(InventoryType.FURNACE, ActionTypes.SMELTING, Permissions.REWARDS_SMELTING);
		}
		
		// Bluelectric alloy furnace
		if (block.match(REDPOWER_PROCESS_BLOCK, BLUELECTRIC_ALLOY_FURNACE)) {
			if (standardChecks && rawSlot == 9)
				return new BlockResponse(InventoryType.FURNACE, ActionTypes.SMELTING, Permissions.REWARDS_SMELTING);
		}
		
		// Project table
		if (block.match(REDPOWER_PROCESS_BLOCK, PROJECT_TABLE)) {
		
			// Unfortunately, current item is not the correct slot. So,
			// we'll force the crafting hack (typically, only used for shift-clicks)
			if (type == InventoryType.CHEST && rawSlot == 9) {
				
				BlockResponse response = new BlockResponse(InventoryType.WORKBENCH, ActionTypes.CRAFTING, Permissions.REWARDS_CRAFTING);

				response.setCurrentItem(null);
				response.setOverrideCurrent(true);
				response.setForceHack(true);
				return response;
			}
		}
		
		// We don't know
		return BlockResponse.FAILURE;
	}
}
