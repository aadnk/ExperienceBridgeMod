package com.comphenix.xpbridge.mods;

import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import com.comphenix.xp.ActionTypes;
import com.comphenix.xp.Debugger;
import com.comphenix.xp.ExperienceMod;
import com.comphenix.xp.extra.Permissions;
import com.comphenix.xp.listeners.ExperienceItemListener;
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
	private Integer COLLECTOR = null;
	
	// Metadata
	private static final int DM_FURNACE = 3;
	private static final int RM_FURNACE = 4;
	
	// Only used for its utility methods
	private ExperienceItemListener itemListener;
	
	private boolean loaded;
	private ExperienceMod expMod;
	
	public EquivalentExchangeBlockService(ActionTypes types, ItemNameParser parser,
										  Debugger debugger, ExperienceMod expMod) throws ParsingException {
	
		TRANSMUTATION_TABLET = parser.getFirstRegistered("TRANSTABLET");
		TRANSMUTATION_ITEM = parser.getFirstRegistered("TRANSTABLETITEM");
		COLLECTOR = parser.getFirstRegistered("COLLECTOR");
		
		// Load fields
		this.expMod = expMod;
		this.itemListener = new ExperienceItemListener(debugger, null, null, null);
		
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
		ItemQuery inHand = getSafely(player.getItemInHand());
		
		int rawSlot = event.getRawSlot();
		
		// EE is not loaded
		if (!loaded) {
			return BlockResponse.FAILURE;
		}
		
		// Check for a transmutation tablet
		if ((block != null && block.match(TRANSMUTATION_TABLET, 0)) ||
		    (inHand != null && inHand.match(TRANSMUTATION_ITEM, null))) {
			
			// See if we're transmuting anything
			if (ItemQuery.hasItems(current) && (rawSlot == 8 || (rawSlot >= 10 && rawSlot <= 25))) {

				// Use the new "transmute" action
				BlockResponse response = new BlockResponse(ACTION_TRANSMUTE, REWARDS_TRANSMUTE);
				
				response.setForceHack(true);
				return response;
			}
		}
		
		// This is our fix for the furnaces - DO NOT allow items to be placed into the output slot
		if (block != null && block.match(COLLECTOR, DM_FURNACE)) {
			
			// Output slots
			if (rawSlot >= 10 && rawSlot <= 18) {
			
				if (event.isShiftClick())
					return createFurnaceResponse();
				else
					// Bad mod, BAD mod! We'll have to take over.
					return handleAsFurnace(event, player, current);
			}
		}
		
		// And for Red Matter furnaces too
		if (block != null && block.match(COLLECTOR, RM_FURNACE)) {
			
			// Output slots
			if (rawSlot >= 14 && rawSlot <= 26) {
			
				if (event.isShiftClick())
					return createFurnaceResponse();
				else
					// Bad mod, BAD mod! We'll have to take over.
					return handleAsFurnace(event, player, current);
			}
		}
		
		return BlockResponse.FAILURE;
	}
	
	private BlockResponse handleAsFurnace(InventoryClickEvent event, Player player, ItemStack toTake) {
	
		ItemStack toStore = event.getCursor();
		boolean hasStore = ItemQuery.hasItems(toStore);
		
		// The items are stored in the cursor. Make sure there's enough space.
		int count = itemListener.getStorageCount(toStore, toTake, false);
		
		// This will also be TRUE when "toTake" is null
		if (count <= 0) {
			// Nope. Deny everything.
			return cancelClickEvent(event);
		}
		
		// Now, let ExperienceMod find the same count and award experience
		expMod.processInventoryClick(event, createFurnaceResponse());
		
		// Some cruft here - the stack is only divided when the user has no cursor items
		if (event.isRightClick() && !hasStore) {
			count = count / 2 + count % 2;
		}

		// Create cursor store if we need it
		if (!hasStore) {
			toStore = toTake.clone();
			toStore.setAmount(count);
			event.setCursor(toStore);
			
		} else {

			// Move items
			toStore.setAmount(toStore.getAmount() + count);
		}
			
		// Decrement or remove entirely
		if (count < toTake.getAmount()) {
			toTake.setAmount(toTake.getAmount() - count);
		} else {
			event.setCurrentItem(null);
		}
		
		// Make this result our result
		event.setResult(Result.ALLOW);
		
		// ExperienceMod has already processed this event, so tell it to ignore it
		return createIgnoreReponse();
	}
	
	private BlockResponse cancelClickEvent(InventoryClickEvent event) {
		
		// Create a custom behavior.
		// Note that this version of ExperienceMod hasn't got setCurrentBehavior()
		BlockResponse cancel = createIgnoreReponse();
		
		event.setCancelled(true);
		return cancel;
	}
	
	private BlockResponse createIgnoreReponse() {
		return new BlockResponse(true, (InventoryType) null, "", "");
	}
	
	// Simple response furnace factory
	private BlockResponse createFurnaceResponse() {
		return new BlockResponse(InventoryType.FURNACE, ActionTypes.SMELTING, Permissions.REWARDS_SMELTING);
	}
	
	private ItemQuery getSafely(ItemStack stack) {
		return stack != null ? ItemQuery.fromExact(stack) : null;
	}

}
