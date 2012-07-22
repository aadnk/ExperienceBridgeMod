package com.comphenix.xpbridge.mods;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.comphenix.xp.ActionTypes;
import com.comphenix.xp.Debugger;
import com.comphenix.xp.lookup.ItemQuery;
import com.comphenix.xp.mods.BlockResponse;
import com.comphenix.xp.mods.BlockService;
import com.comphenix.xp.parser.ParsingException;
import com.comphenix.xp.parser.text.ItemNameParser;

public class EquivalentExchangeBlockService implements BlockService {

	public static String NAME = "EQUIVALENT_EXCHANGE";
	public static String ACTION_TRANSMUTE = "TRANSMUTE";
	public static String REWARDS_TRANSMUTE = "experiencebridgeemod.rewards.transmute";

	// Blocks
	private Integer TRANSMUTATION_TABLET = null;
	private Integer TRANSMUTATION_ITEM = null;
	
	private boolean loaded;
	
	public EquivalentExchangeBlockService(ActionTypes types, ItemNameParser parser, Debugger debugger) throws ParsingException {
	
		TRANSMUTATION_TABLET = parser.getFirstRegistered("TRANSTABLET");
		TRANSMUTATION_ITEM = parser.getFirstRegistered("TRANSTABLETITEM");
		
		if (TRANSMUTATION_TABLET != null && TRANSMUTATION_ITEM != null) {
			loaded = true;
			types.register(ACTION_TRANSMUTE, "TRANSMUTE_RESULT");
			debugger.printDebug(this, "Registering the TRANSMUTE action type.");
		} else {
			debugger.printWarning(this, "Cannot load EquivalentExchange.");
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
		Player player = (Player) event.getWhoClicked();
		ItemQuery inHand = ItemQuery.fromExact(player.getItemInHand());
		
		int rawSlot = event.getRawSlot();
		
		// EE is not loaded
		if (!loaded) {
			return BlockResponse.FAILURE;
		}
		
		// Check for a transmutation tablet
		if ((block != null && block.match(TRANSMUTATION_TABLET, 0)) | 
		     inHand.match(TRANSMUTATION_ITEM, null)) {
			
			// See if we're transmuting anything
			if (ItemQuery.hasItems(current) && (rawSlot == 8 || (rawSlot >= 10 && rawSlot <= 25))) {

				// Use the new "transmute" action
				BlockResponse response = new BlockResponse(ACTION_TRANSMUTE, REWARDS_TRANSMUTE);
				
				response.setForceHack(true);
				return response;
			}
		}
		
		return BlockResponse.FAILURE;
	}

}
