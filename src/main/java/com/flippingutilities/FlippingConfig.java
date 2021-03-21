package com.flippingutilities;

import com.flippingutilities.controller.FlippingPlugin;
import com.flippingutilities.ui.uiutilities.CustomColors;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Units;
import net.runelite.client.ui.ColorScheme;

import java.awt.*;

@ConfigGroup(FlippingPlugin.CONFIG_GROUP)
public interface FlippingConfig extends Config
{
	@ConfigItem(
		keyName = "roiGradientMax",
		name = "Set ROI gradient range limit",
		description = "Set the limit of the range before the gradient is bright green"
	)
	@Units(Units.PERCENT)
	default int roiGradientMax()
	{
		return 2;
	}

	@ConfigItem(
		keyName = "marginCheckLoss",
		name = "Account for margin check loss",
		description = "Subtract the loss from margin checking the item when calculating the total profit"
	)
	default boolean marginCheckLoss()
	{
		return true;
	}

	@ConfigItem(
		keyName = "twelveHourFormat",
		name = "12 hour format",
		description = "Shows times in a 12 hour format (AM/PM)"
	)
	default boolean twelveHourFormat()
	{
		return true;
	}

	@ConfigItem(
		keyName = "remainingGELimitProfit",
		name = "Calculate potential profit from remaining GE limit",
		description = "If unchecked, the potential profit will be calculated from total GE limit"
	)
	default boolean geLimitProfit()
	{
		return false;
	}

	@ConfigItem(
		keyName = "tradeStagnationTime",
		name = "Set trade stagnation time",
		description = "Set how long before the offer slot activity timer indicates that a trade has become stagnant"
	)
	@Units(Units.MINUTES)
	default int tradeStagnationTime()
	{
		return 15;
	}

	@ConfigItem(
		keyName = "slotTimersEnabled",
		name = "toggle slot timers",
		description = "Have a timer on active GE slots that will show the last time an offer came for the slot. This is useful" +
			"for knowing whether you should change your offer's price"
	)
	default boolean slotTimersEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "verboseView",
		name = "toggle verbose view",
		description = "show items in the flipping tab with all their tracked info like buy/sell price, roi, potential" +
			"profit, etc"
	)
	default boolean verboseViewEnabled() { return true; }

	@ConfigItem(
			keyName = "slotTimerBuyTextColor",
			name = "slot timer buy text color",
			description = "the color of the buy text on the slot timers"
	)
	default Color slotTimerBuyColor() {
		return ColorScheme.GRAND_EXCHANGE_LIMIT;
	}

	@ConfigItem(
			keyName = "slotTimerSellTextColor",
			name = "slot timer sell text color",
			description = "the color of the sell text on the slot timers"
	)
	default Color slotTimerSellColor() {
		return CustomColors.VIBRANT_YELLOW;
	}
}
