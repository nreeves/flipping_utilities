
package com.flippingutilities.model;

import com.flippingutilities.controller.FlippingPlugin;
import com.flippingutilities.ui.widgets.TradeActivityTimer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.ItemManager;
import net.runelite.http.api.item.ItemStats;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class AccountData
{
	private Map<Integer, OfferEvent> lastOffers = new HashMap<>();
	private List<FlippingItem> trades = new ArrayList<>();
	private Instant sessionStartTime = Instant.now();
	private Duration accumulatedSessionTime = Duration.ZERO;
	private Instant lastSessionTimeUpdate;
	private List<TradeActivityTimer> slotTimers;

	/**
	 * Resets all session related data associated with an account. This is called when the plugin first starts
	 * as that's when a new session is "started" and when a user wants to start a new session for an account.
	 */
	public void startNewSession()
	{
		sessionStartTime = Instant.now();
		accumulatedSessionTime = Duration.ZERO;
		lastSessionTimeUpdate = null;
	}

	/**
	 * Over time as we delete/add fields, we need to make sure the fields are set properly the first time the user
	 * loads their trades after the new update. This method serves as a way to sanitize the data. It also ensures
	 * that the FlippingItems have their non persisted fields set from history.
	 */
	public void prepareForUse(FlippingPlugin plugin)
	{
		for (FlippingItem item : trades)
		{
			//in case ge limits have been updated
			int tradeItemId = item.getItemId();
			ItemStats itemStats = plugin.getItemManager().getItemStats(tradeItemId, false);
			int geLimit = itemStats != null ? itemStats.getGeLimit() : 0;

			item.setOfferMadeBy();
			item.setTotalGELimit(geLimit);
			item.syncState();
			//when this change was made the field will not exist and will be null
			if (item.getValidFlippingPanelItem() == null)
			{
				item.setValidFlippingPanelItem(true);
			}
		}

		if (slotTimers == null)
		{
			setSlotTimers(setupSlotTimers(plugin));
		}
		else
		{
			slotTimers.forEach(timer -> {
				timer.setClient(plugin.getClient());
				timer.setPlugin(plugin);
			});
		}
	}

	private List<TradeActivityTimer> setupSlotTimers(FlippingPlugin plugin)
	{
		ArrayList<TradeActivityTimer> slotTimers = new ArrayList<>();
		for (int slotIndex = 0; slotIndex < 8; slotIndex++)
		{
			slotTimers.add(new TradeActivityTimer(plugin, plugin.getClient(), slotIndex));
		}
		return slotTimers;
	}
}
