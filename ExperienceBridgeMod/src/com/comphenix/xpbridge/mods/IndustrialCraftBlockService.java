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

public class IndustrialCraftBlockService implements BlockService {
	
	public static String NAME = "INDUSTRIAL_CRAFT_2";
	public static String REWARD_PROCESSING = "experiencebridgemod.rewards.processing";
	public static String ACTION_PROCESSING = "PROCESSING";
	
	// Blocks
	private Integer MACHINE_BLOCK = null;
	
	// Constants for IC2
	private int IRON_FURNACE = 1;
	private int ELECTRIC_FURNACE = 2;
	private int MACERATOR = 3;
	private int EXTRACTOR = 4;
	private int COMPRESSOR = 5;
	private int CANNING_MACHINE = 6;
	private int RECYCLER = 11;
	
	private int INDUCTION_FURNACE = 13;

	private boolean loaded;
	
	public IndustrialCraftBlockService(ActionTypes types, ItemNameParser parser, Debugger debugger) throws ParsingException {
		
		MACHINE_BLOCK = parser.getFirstRegistered("BLOCKMACHINE");

		if (MACHINE_BLOCK != null) {
			loaded = true;
			types.register(ACTION_PROCESSING, "PROCESSING_RESULT");
			debugger.printDebug(this, "Registering the PROCESSING action type.");
		} else {
			debugger.printWarning(this, "Cannot load IC2.");
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
		
		// IC2 not loaded
		if (!loaded || block == null) {
			return BlockResponse.FAILURE;
		}
		
		// Iron/electric furnace things
		if (block.match(MACHINE_BLOCK, IRON_FURNACE) || 
			block.match(MACHINE_BLOCK, ELECTRIC_FURNACE)) {
			
			if (ItemQuery.hasItems(current) && rawSlot == 2)
				return new BlockResponse(InventoryType.FURNACE, ActionTypes.SMELTING, Permissions.REWARDS_SMELTING);
		}
		
		// Processing
		if (block.match(MACHINE_BLOCK, MACERATOR) ||
			block.match(MACHINE_BLOCK, EXTRACTOR) ||
			block.match(MACHINE_BLOCK, COMPRESSOR) ||
			block.match(MACHINE_BLOCK, CANNING_MACHINE) || 
			block.match(MACHINE_BLOCK, RECYCLER)) {
			
			if (ItemQuery.hasItems(current) && rawSlot == 2)
				return new BlockResponse(InventoryType.FURNACE, ACTION_PROCESSING, REWARD_PROCESSING);
		}
		
		// Induction furnace
		if (block.match(MACHINE_BLOCK, INDUCTION_FURNACE)) {
			
			// Two output slots
			if (ItemQuery.hasItems(current) && (rawSlot == 3 || rawSlot == 4))
				return new BlockResponse(InventoryType.FURNACE, ActionTypes.SMELTING, Permissions.REWARDS_SMELTING);
		}
		
		return BlockResponse.FAILURE;
	}
}
