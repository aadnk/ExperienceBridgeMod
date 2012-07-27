package com.comphenix.xpbridge.mods;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

import com.comphenix.xp.ActionTypes;
import com.comphenix.xp.Debugger;
import com.comphenix.xp.extra.Permissions;
import com.comphenix.xp.lookup.ItemQuery;
import com.comphenix.xp.mods.BlockResponse;
import com.comphenix.xp.mods.BlockService;
import com.comphenix.xp.parser.ParsingException;
import com.comphenix.xp.parser.text.ItemNameParser;

public class BuildCraftBlockService implements BlockService {
	public static final String NAME = "BUILD_CRAFT";

	// Blocks
	private Integer AUTOWORKBENCHBLOCK = null;
	
	private boolean loaded;
	
	public BuildCraftBlockService(ItemNameParser parser, Debugger debugger) throws ParsingException {
		
		AUTOWORKBENCHBLOCK = parser.getFirstRegistered("AUTOWORKBENCHBLOCK");

		if (AUTOWORKBENCHBLOCK != null) {
			loaded = true;
		} else {
			debugger.printWarning(this, "Cannot load BuildCraft.");
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

		//ItemStack current = event.getCurrentItem();
		int rawSlot = event.getRawSlot();
		
		// Buildcraft is not loaded
		if (!loaded || block == null) {
			return BlockResponse.FAILURE;
		}
		
		// Automatic Crafting table
		if (block.match(AUTOWORKBENCHBLOCK, null)) {
			
			// See if we're crafting
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
