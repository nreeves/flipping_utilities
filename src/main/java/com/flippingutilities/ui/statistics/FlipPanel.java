
package com.flippingutilities.ui.statistics;

import com.flippingutilities.model.Flip;
import com.flippingutilities.ui.uiutilities.CustomColors;
import com.flippingutilities.ui.uiutilities.TimeFormatters;
import com.flippingutilities.ui.uiutilities.UIUtilities;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.QuantityFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class FlipPanel extends JPanel
{
	JLabel title = new JLabel("", SwingConstants.CENTER);

	private Flip flip;

	FlipPanel(Flip flip)
	{
		this.flip = flip;

		setLayout(new BorderLayout());


		int profitEach = flip.getSellPrice() - flip.getBuyPrice();
		int profitTotal = profitEach * flip.getQuantity();

		title.setOpaque(true);
		title.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		title.setFont(FontManager.getRunescapeSmallFont());
		updateTitle();

		JLabel buyPriceText = new JLabel("Buy Price:");
		JLabel sellPriceText = new JLabel("Sell Price:");
		JLabel profitText = new JLabel((profitTotal >= 0) ? "Profit: " : "Loss: ");

		JLabel buyPriceVal = new JLabel(QuantityFormatter.formatNumber(flip.getBuyPrice()) + " gp", SwingConstants.RIGHT);
		JLabel sellPriceVal = new JLabel(QuantityFormatter.formatNumber(flip.getSellPrice()) + " gp", SwingConstants.RIGHT);

		String profitString = UIUtilities.quantityToRSDecimalStack(profitTotal, true) + " gp"
			+ ((flip.getQuantity() <= 1) ? "" : " (" + UIUtilities.quantityToRSDecimalStack(profitEach, false) + " gp ea.)");

		JLabel profitVal = new JLabel(profitString);

		JLabel[] labelList = {buyPriceText, buyPriceVal, sellPriceText, sellPriceVal, profitText, profitVal};

		for (JLabel label : labelList)
		{
			label.setFont(FontManager.getRunescapeSmallFont());
			label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		}

		profitText.setForeground((profitTotal >= 0) ? ColorScheme.GRAND_EXCHANGE_PRICE : CustomColors.OUTDATED_COLOR);
		profitVal.setForeground((profitTotal >= 0) ? ColorScheme.GRAND_EXCHANGE_PRICE : CustomColors.OUTDATED_COLOR);

		JPanel buyPricePanel = new JPanel(new BorderLayout());
		JPanel sellPricePanel = new JPanel(new BorderLayout());
		JPanel profitPanel = new JPanel(new BorderLayout());

		buyPricePanel.add(buyPriceText, BorderLayout.WEST);
		buyPricePanel.add(buyPriceVal, BorderLayout.EAST);

		sellPricePanel.add(sellPriceText, BorderLayout.WEST);
		sellPricePanel.add(sellPriceVal, BorderLayout.EAST);

		profitPanel.add(profitText, BorderLayout.WEST);
		profitPanel.add(profitVal, BorderLayout.EAST);

		JPanel infoContainer = new JPanel(new DynamicGridLayout(3, 2, 0, 2));

		infoContainer.add(buyPricePanel);
		infoContainer.add(sellPricePanel);
		infoContainer.add(profitPanel);

		infoContainer.setBorder(new EmptyBorder(0, 2, 1, 2));

		add(title, BorderLayout.NORTH);
		add(infoContainer, BorderLayout.CENTER);
	}

	public void updateTitle()
	{
		if (flip.isMarginCheck())
		{
			title.setText("Margin Checked " + "(" + TimeFormatters.formatDurationTruncated(flip.getTime()) + " ago)");
			title.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
		}

		else if (flip.isOngoing())
		{
			title.setText(QuantityFormatter.formatNumber(flip.getQuantity()) + " Flipped (ongoing)");
			title.setForeground(CustomColors.VIBRANT_YELLOW);
		}

		else
		{
			title.setText(QuantityFormatter.formatNumber(flip.getQuantity()) + " Flipped (" + TimeFormatters.formatDurationTruncated(flip.getTime()) + " ago)");
			title.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
		}
	}

}

