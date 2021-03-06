package com.flippingutilities.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class is the representation of an item that a user is flipping. It contains information about the
 * margin of the item (buying and selling price), the latest buy and sell times, and the history of the item
 * which is all of the offers that make up the trade history of that item. This history is managed by the
 * {@link HistoryManager} and is used to get the profits for this item, how many more of it you can buy
 * until the ge limit refreshes, and when the next ge limit refreshes.
 * <p>
 * This class is the model behind a FlippingItemPanel as its data is used to create the contents
 * of a panel which is then displayed.
 */
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class FlippingItem
{

	@SerializedName("id")
	@Getter
	private int itemId;

	@SerializedName("name")
	@Getter
	@Setter
	private String itemName;

	@SerializedName("tGL")
	@Getter
	@Setter
	private int totalGELimit;

	@SerializedName("h")
	@Getter
	@Setter
	private HistoryManager history = new HistoryManager();

	@SerializedName("fB")
	@Getter
	private String flippedBy;

	//whether the item should be on the flipping panel or not.
	@SerializedName("vFPI")
	@Getter
	@Setter
	private Boolean validFlippingPanelItem;

	@Getter
	@Setter
	private boolean favorite;

	@Getter
	@Setter
	private String favoriteCode = "1";

	//non persisted fields start here.
	@Setter
	@Getter
	private transient Optional<OfferEvent> latestMarginCheckBuy;

	@Setter
	@Getter
	private transient Optional<OfferEvent> latestMarginCheckSell;

	@Setter
	@Getter
	private transient Optional<OfferEvent> latestBuy;

	@Setter
	@Getter
	private transient Optional<OfferEvent> latestSell;

	//does not have to Optional because a flipping item always has at least one offer, which establishes
	//latestActivityTime.
	@Getter
	private transient Instant latestActivityTime;

	@Getter
	@Setter
	private transient Boolean expand;

	public FlippingItem(int itemId, String itemName, int totalGeLimit, String flippedBy)
	{
		this.latestMarginCheckBuy = Optional.empty();
		this.latestMarginCheckSell = Optional.empty();
		this.latestBuy = Optional.empty();
		this.latestSell = Optional.empty();
		this.itemName = itemName;
		this.itemId = itemId;
		this.totalGELimit = totalGeLimit;
		this.flippedBy = flippedBy;
	}

	public FlippingItem clone()
	{
		return new FlippingItem(
				itemId,
				itemName,
				totalGELimit,
				history.clone(),
				flippedBy,
				validFlippingPanelItem,
				favorite,
				favoriteCode,
				latestMarginCheckBuy,
				latestMarginCheckSell,
				latestBuy,
				latestSell,
				latestActivityTime,
				expand);
	}

	/**
	 * This method updates the history of a FlippingItem. This history is used to calculate profits,
	 * next ge limit refresh, and how many items were bought during this limit window.
	 *
	 * @param newOffer the new offer that just came in
	 */
	public void updateHistory(OfferEvent newOffer)
	{
		history.updateHistory(newOffer);
	}

	/**
	 * Updates the latest margin check/buy/sell offers. Technically, we don't need this and we can just
	 * query the history manager, but this saves us from querying the history manager which would have
	 * to search through the offers.
	 *
	 * @param newOffer new offer just received
	 */
	public void updateLatestProperties(OfferEvent newOffer)
	{
		if (newOffer.isBuy())
		{
			if (newOffer.isMarginCheck())
			{
				latestMarginCheckBuy = Optional.of(newOffer);
			}
			latestBuy = Optional.of(newOffer);
		}
		else
		{
			if (newOffer.isMarginCheck())
			{
				latestMarginCheckSell = Optional.of(newOffer);
			}
			latestSell = Optional.of(newOffer);
		}
		latestActivityTime = newOffer.getTime();
	}

	/**
	 * combines two flipping items together (this only makes sense if they are for the same item) by adding
	 * their histories together and retaining the other properties of the latest active item.
	 *
	 * @return merged flipping item
	 */
	public static FlippingItem merge(FlippingItem item1, FlippingItem item2)
	{
		if (item1 == null)
		{
			return item2;
		}

		if (item1.getLatestActivityTime().compareTo(item2.getLatestActivityTime()) >= 0)
		{
			item1.getHistory().getCompressedOfferEvents().addAll(item2.getHistory().getCompressedOfferEvents());
			item1.setFavorite(item1.isFavorite() || item2.isFavorite());
			return item1;
		}
		else
		{
			item2.getHistory().getCompressedOfferEvents().addAll(item1.getHistory().getCompressedOfferEvents());
			item2.setFavorite(item2.isFavorite() || item1.isFavorite());
			return item2;
		}
	}

	public long currentProfit(List<OfferEvent> tradeList)
	{
		return history.currentProfit(tradeList);
	}

	public long getFlippedCashFlow(List<OfferEvent> tradeList, boolean getExpense)
	{
		return history.getFlippedCashFlow(tradeList, getExpense);
	}

	public long getFlippedCashFlow(Instant earliestTime, boolean getExpense)
	{
		return history.getFlippedCashFlow(getIntervalHistory(earliestTime), getExpense);
	}

	public long getTotalCashFlow(List<OfferEvent> tradeList, boolean getExpense)
	{
		return history.getTotalCashFlow(tradeList, getExpense);
	}

	public int countItemsFlipped(List<OfferEvent> tradeList)
	{
		return history.countItemsFlipped(tradeList);
	}

	public ArrayList<OfferEvent> getIntervalHistory(Instant earliestTime)
	{
		return history.getIntervalsHistory(earliestTime);
	}

	public int getRemainingGeLimit()
	{
		return totalGELimit - history.getItemsBoughtThisLimitWindow();
	}

	public int getItemsBoughtThisLimitWindow()
	{
		return history.getItemsBoughtThisLimitWindow();
	}

	public Instant getGeLimitResetTime()
	{
		return history.getNextGeLimitRefresh();
	}

	public void validateGeProperties()
	{
		history.validateGeProperties();
	}

	public List<Flip> getFlips(Instant earliestTime)
	{
		return history.getFlips(earliestTime);
	}

	public boolean hasValidOffers()
	{
		return history.hasValidOffers();
	}

	public void invalidateOffers(ArrayList<OfferEvent> offerList)
	{
		history.invalidateOffers(offerList);
	}

	public void setValidFlippingPanelItem(boolean isValid)
	{
		validFlippingPanelItem = isValid;
		if (!isValid)
		{
			latestMarginCheckBuy = Optional.empty();
			latestMarginCheckSell = Optional.empty();
			latestBuy = Optional.empty();
			latestSell = Optional.empty();
		}
	}

	public Optional<Integer> getPotentialProfit(boolean includeMarginCheck, boolean shouldUseRemainingGeLimit)
	{
		if (!getLatestMarginCheckBuy().isPresent() || !getLatestMarginCheckSell().isPresent()) {
			return Optional.empty();
		}

		int profitEach = getCurrentProfitEach().get();
		int remainingGeLimit = getRemainingGeLimit();
		int geLimit = shouldUseRemainingGeLimit ? remainingGeLimit : totalGELimit;
		int profitTotal = geLimit * profitEach;
		if (includeMarginCheck)
		{
			profitTotal -= profitEach;
		}
		return Optional.of(profitTotal);
	}

	public List<OfferEvent> getOfferMatches(OfferEvent offerEvent, int limit)
	{
		return history.getOfferMatches(offerEvent, limit);
	}

	public Optional<Float> getCurrentRoi() {
		return getCurrentProfitEach().isPresent()?
				Optional.of((float)getCurrentProfitEach().get() / getLatestMarginCheckSell().get().getPrice() * 100) : Optional.empty();
	}

	public Optional<Integer> getCurrentProfitEach() {
		return getLatestMarginCheckBuy().isPresent() && getLatestMarginCheckSell().isPresent()?
				Optional.of(getLatestMarginCheckBuy().get().getPrice() - getLatestMarginCheckSell().get().getPrice()) : Optional.empty();
	}

	/**
	 * When the plugin starts up, the flipping items are constructed, but they are going to be missing
	 * values for certain fields that aren't persisted. I chose not to persist those fields as those fields
	 * can be constructed using the history that is already persisted. The downside, is that I have to
	 * manually sync state when flipping items are created at plugin startup.
	 */
	public void syncState() {
		latestBuy = history.getLatestOfferThatMatchesPredicate(offer -> offer.isBuy());
		latestSell = history.getLatestOfferThatMatchesPredicate(offer -> !offer.isBuy());
		latestMarginCheckBuy = history.getLatestOfferThatMatchesPredicate(offer -> offer.isBuy() & offer.isMarginCheck());
		latestMarginCheckSell = history.getLatestOfferThatMatchesPredicate(offer -> !offer.isBuy() & offer.isMarginCheck());
		latestActivityTime = history.getCompressedOfferEvents().size() == 0? Instant.now() : history.getCompressedOfferEvents().get(history.getCompressedOfferEvents().size()-1).getTime();
	}

	public void setOfferMadeBy() {
		history.getCompressedOfferEvents().forEach(o -> o.setMadeBy(flippedBy));
	}

	public void resetGeLimit() {
		history.resetGeLimit();
	}

}
