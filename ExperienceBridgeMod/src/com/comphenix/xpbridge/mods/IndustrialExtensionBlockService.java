package com.comphenix.xpbridge.mods;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import com.comphenix.xp.Debugger;
import com.comphenix.xp.lookup.ItemQuery;
import com.comphenix.xp.mods.BlockResponse;
import com.comphenix.xp.mods.BlockService;
import com.comphenix.xp.parser.ParsingException;
import com.comphenix.xp.parser.text.ItemNameParser;

public class IndustrialExtensionBlockService implements BlockService {

	public static final String NAME = "INDUSTRIAL_EXTENSIONS";
	public static String REWARD_PROCESSING = "experiencebridgemod.rewards.processing";
	public static String ACTION_PROCESSING = "PROCESSING";
	
	// Blocks
	private Integer EXTENSION_BLOCK = null;
	
	// IDs
	private static final int ROTARY_MACERATOR = 0;
	private static final int SINGULARITY_COMPRESSOR = 1;
	private static final int CENTRIFUGE_EXTRACTOR = 2;
	
	private boolean loaded;
	
	public IndustrialExtensionBlockService(ItemNameParser parser, Debugger debugger) throws ParsingException {
		
		EXTENSION_BLOCK = parser.getFirstRegistered("BLOCKROTARYMACERATOR");

		if (EXTENSION_BLOCK != null) {
			loaded = true;
		} else {
			debugger.printWarning(this, "Cannot load IC2 extensions.");
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
		
		// IC2 extensions are not loaded
		if (!loaded || block == null) {
			return BlockResponse.FAILURE;
		}
		
		// Rotary macerator
		if (block.match(EXTENSION_BLOCK, ROTARY_MACERATOR)) {
			
			// Two output slots
			if (ItemQuery.hasItems(current) && (rawSlot == 2 || rawSlot == 3))
				return new BlockResponse(InventoryType.FURNACE, ACTION_PROCESSING, REWARD_PROCESSING);
		}
		
		// Singularity compressor
		if (block.match(EXTENSION_BLOCK, SINGULARITY_COMPRESSOR)) {
			
			if (ItemQuery.hasItems(current) && rawSlot == 2)
				return new BlockResponse(InventoryType.FURNACE, ACTION_PROCESSING, REWARD_PROCESSING);
		}
		
		// Centrifuge extractor
		if (block.match(EXTENSION_BLOCK, CENTRIFUGE_EXTRACTOR)) {
		
			if (ItemQuery.hasItems(current) && rawSlot >= 2 && rawSlot <= 4)
				return new BlockResponse(InventoryType.FURNACE, ACTION_PROCESSING, REWARD_PROCESSING);
		}
		
		return BlockResponse.FAILURE;
	}
	
}
