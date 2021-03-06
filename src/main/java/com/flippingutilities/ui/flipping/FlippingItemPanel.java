

package com.flippingutilities.ui.flipping;

import com.flippingutilities.controller.FlippingPlugin;
import com.flippingutilities.model.FlippingItem;
import com.flippingutilities.model.OfferEvent;
import com.flippingutilities.ui.uiutilities.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.QuantityFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Represents an instance of one of the many panels on the FlippingPanel. It is used to display information such as
 * the margin check prices of the flipping item, the ge limit left, when the ge limit will refresh, the ROI, etc.
 */
@Slf4j
public class FlippingItemPanel extends JPanel
{
	private static final String NUM_FORMAT = "%,d";

	@Getter
	private final FlippingItem flippingItem;
	private FlippingPlugin plugin;

	//All the labels that hold the actual values for these properties.
	JLabel priceCheckBuyVal = new JLabel();
	JLabel priceCheckSellVal = new JLabel();
	JLabel latestBuyPriceVal = new JLabel();
	JLabel latestSellPriceVal = new JLabel();
	JLabel profitEachVal = new JLabel();
	JLabel potentialProfitVal = new JLabel();
	JLabel roiLabelVal = new JLabel();
	JLabel limitLabelVal = new JLabel();
	JLabel priceCheckBuyTimeVal = new JLabel();
	JLabel priceCheckSellTimeVal = new JLabel();
	JLabel latestBuyTimeVal = new JLabel();
	JLabel latestSellTimeVal = new JLabel();
	JLabel geRefreshLabel = new JLabel();
	JLabel geRefreshAtLabel = new JLabel();
	JLabel latestPcBuyAt = new JLabel();
	JLabel latestPcSellAt = new JLabel();
	JLabel latestBoughtAt = new JLabel();
	JLabel latestSoldAt = new JLabel();

	JLabel priceCheckBuyText = new JLabel("Last margin buy: ");
	JLabel priceCheckSellText = new JLabel("Last margin sell: ");
	JLabel latestBuyPriceText = new JLabel("Last buy price: ");
	JLabel latestSellPriceText = new JLabel("Last sell price: ");
	JLabel profitEachText = new JLabel("Profit each: ");
	JLabel profitTotalText = new JLabel("Potential profit: ");
	JLabel roiText = new JLabel("ROI:", JLabel.CENTER);
	JLabel geLimitText = new JLabel("GE limit:",JLabel.CENTER);

	JPanel itemInfo;
	JPanel timeInfoPanel;

	JLabel searchCodeLabel;

	FlippingItemPanel(final FlippingPlugin plugin, AsyncBufferedImage itemImage, final FlippingItem flippingItem)
	{
		this.flippingItem = flippingItem;
		this.plugin = plugin;
		flippingItem.validateGeProperties();
		setBackground(CustomColors.DARK_GRAY);
		setLayout(new BorderLayout());
		setToolTipText("Flipped by " + flippingItem.getFlippedBy());

		setDescriptionLabels();
		setValueLabels();
		updateTimerDisplays();

		JPanel titlePanel = createTitlePanel(createItemIcon(itemImage), createDeleteButton(), createItemNameLabel(), createFavoriteIcon());
		itemInfo = createItemInfoPanel();
		timeInfoPanel = createTimeInfoPanel();
		timeInfoPanel.setVisible(false);
		add(titlePanel, BorderLayout.NORTH);
		add(itemInfo, BorderLayout.CENTER);
		add(timeInfoPanel, BorderLayout.SOUTH);

		//if it is enabled, the itemInfo panel is visible by default so no reason to check it
		if (!plugin.getConfig().verboseViewEnabled())
		{
			collapse();
		}

		//if user has "overridden" the config option by expanding/collapsing that item, use what they set instead of the config value.
		if (flippingItem.getExpand() != null)
		{
			if (flippingItem.getExpand())
			{
				expand();
			}
			else
			{
				collapse();
			}
		}
	}

	/**
	 * Creates the panel which contains all the info about the item like its price check prices, limit
	 * remaining, etc.
	 * @return
	 */
	private JPanel createItemInfoPanel()
	{
		JPanel itemInfo = new JPanel(new DynamicGridLayout(9, 1));
		itemInfo.setBackground(getBackground());

		JPanel priceCheckBuyPanel = new JPanel(new BorderLayout());
		JPanel priceCheckSellPanel = new JPanel(new BorderLayout());
		JPanel latestBuyPanel = new JPanel(new BorderLayout());
		JPanel latestSellPanel = new JPanel(new BorderLayout());
		JPanel profitEachPanel = new JPanel(new BorderLayout());
		JPanel potentialProfitPanel = new JPanel(new BorderLayout());

		makePropertyPanelEditable(priceCheckBuyPanel, priceCheckBuyVal);
		makePropertyPanelEditable(priceCheckSellPanel, priceCheckSellVal);
		makePropertyPanelEditable(latestBuyPanel, latestBuyPriceVal);
		makePropertyPanelEditable(latestSellPanel, latestSellPriceVal);

		JPanel[] panels = {latestBuyPanel, latestSellPanel, priceCheckBuyPanel, priceCheckSellPanel, profitEachPanel, potentialProfitPanel};
		JLabel[] descriptionLabels = {latestBuyPriceText, latestSellPriceText, priceCheckBuyText, priceCheckSellText, profitEachText, profitTotalText};
		JLabel[] valueLabels = {latestBuyPriceVal, latestSellPriceVal, priceCheckBuyVal, priceCheckSellVal, profitEachVal, potentialProfitVal};

		boolean isFirstInPair = true;

		for (int i=0;i<panels.length;i++) {
			panels[i].setBackground(CustomColors.DARK_GRAY);
			if (isFirstInPair) {
				panels[i].setBorder(new EmptyBorder(6,8,3,8));
			}
			else {
				panels[i].setBorder(new EmptyBorder(2,8,8,8));
			}

			isFirstInPair = !isFirstInPair;
			panels[i].add(descriptionLabels[i], BorderLayout.WEST);
			panels[i].add(valueLabels[i], BorderLayout.EAST);
			itemInfo.add(panels[i]);
			if (i == panels.length-1) {
				panels[i].setBorder(new EmptyBorder(2,8,3,8));
			}
		}

		itemInfo.add(createGeLimitRefreshTimeAndRoiPanel());
		itemInfo.add(createBottomPanel());

		return itemInfo;
	}

	private JPanel createBottomPanel() {
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(new EmptyBorder(3,21,3,8));
		bottomPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

		JLabel searchIconLabel = new JLabel(Icons.SEARCH);
		searchIconLabel.setToolTipText("Click to search item on platinumtokens or osrs ge!");
		JPopupMenu popupMenu = ItemLookUpPopup.createGeTrackerLinksPopup(flippingItem);
		searchIconLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				popupMenu.show(searchIconLabel, e.getX(), e.getY());
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				searchIconLabel.setIcon(Icons.SEARCH_HOVER);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				searchIconLabel.setIcon(Icons.SEARCH);
			}
		});

		TextField searchCodeTextField = new TextField(10);

		JPanel searchCodePanel = new JPanel();
		searchCodePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

		searchCodeLabel = new JLabel("<html> quick search code: " + UIUtilities.colorText(flippingItem.getFavoriteCode(), CustomColors.VIBRANT_YELLOW) + "</html>", JLabel.CENTER);
		if (flippingItem.isFavorite()) {
			searchCodeLabel.setText("<html> quick search code: " + UIUtilities.colorText(flippingItem.getFavoriteCode(), ColorScheme.GRAND_EXCHANGE_PRICE) + "</html>");
		}
		else {
			searchCodeLabel.setText("<html> quick search code: " + UIUtilities.colorText("N/A", CustomColors.VIBRANT_YELLOW) + "</html>");
		}
		searchCodeLabel.setToolTipText("<html>If you have favorited this item, you can type the search code when you are <br>" +
				"searching for items in the ge to populate your ge results with any item with this code</html>");
		searchCodeLabel.setFont(FontManager.getRunescapeSmallFont());

		searchCodePanel.add(searchCodeLabel);

		final boolean[] isHighlighted = {false};
		MouseListener l = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (!flippingItem.isFavorite()) {
					JOptionPane.showMessageDialog(searchCodeLabel, "<html>Item is not favorited.<br> Favorite the item to be able to use/edit the quick search code</html>");
					return;
				}

				if (isHighlighted[0]) {
					searchCodePanel.remove(searchCodeTextField);
					searchCodePanel.add(searchCodeLabel);
					isHighlighted[0] = false;
				}
				else {
					searchCodePanel.remove(searchCodeLabel);
					searchCodePanel.add(searchCodeTextField);
					isHighlighted[0] = true;
				}
				repaint();
				revalidate();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				searchCodePanel.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				searchCodePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
			}
		};
		searchCodePanel.addMouseListener(l);
		searchCodeLabel.addMouseListener(l);

		searchCodeTextField.setBackground(ColorScheme.DARK_GRAY_COLOR);
		searchCodeTextField.setText(flippingItem.getFavoriteCode());
		searchCodeTextField.addActionListener(e -> {
			isHighlighted[0] = false;
			if (plugin.getAccountCurrentlyViewed().equals(FlippingPlugin.ACCOUNT_WIDE)) {
				plugin.setFavoriteCodeOnAllAccounts(flippingItem, searchCodeTextField.getText());
			}
			else {
				plugin.markAccountTradesAsHavingChanged(plugin.getAccountCurrentlyViewed());
			}

			flippingItem.setFavoriteCode(searchCodeTextField.getText());

			searchCodeLabel.setText("<html> quick search code: " + UIUtilities.colorText(flippingItem.getFavoriteCode(), CustomColors.VIBRANT_YELLOW) + "</html>");

			searchCodePanel.remove(searchCodeTextField);
			searchCodePanel.add(searchCodeLabel);
			repaint();
			revalidate();
		});

		bottomPanel.add(searchIconLabel, BorderLayout.EAST);
		bottomPanel.add(searchCodePanel, BorderLayout.CENTER);
		return bottomPanel;
	}

	private void makePropertyPanelEditable(JPanel propertyPanel, JLabel valueLabel) {
		final boolean[] isHighlighted = {false};
		TextField textField = new TextField(10);
		textField.setBackground(ColorScheme.DARK_GRAY_COLOR);
		String currentText = valueLabel.getText();
		String textWithoutGp = currentText.substring(0, currentText.length()-3);
		textField.setText(textWithoutGp);
		textField.addActionListener((e1 -> {
			isHighlighted[0] = false;
			try {
				int num = Integer.parseInt(textField.getText().replace(",", ""));
				if (num <= 0) {
					JOptionPane.showMessageDialog(this,"You cannot input zero or a negative number");
					return;
				}
				valueLabel.setText(String.format(NUM_FORMAT, num) + " gp");
				OfferEvent dummyOffer;
				if (valueLabel == priceCheckBuyVal) {
					dummyOffer = OfferEvent.dummyOffer(false, true, num, flippingItem.getItemId(), flippingItem.getItemName());
					flippingItem.setLatestMarginCheckSell(Optional.of(dummyOffer));
				}
				else if (valueLabel == priceCheckSellVal){
					dummyOffer = OfferEvent.dummyOffer(true, true, num, flippingItem.getItemId(), flippingItem.getItemName());
					flippingItem.setLatestMarginCheckBuy(Optional.of(dummyOffer));
				}
				else if (valueLabel == latestBuyPriceVal){
					dummyOffer = OfferEvent.dummyOffer(true, false, num, flippingItem.getItemId(), flippingItem.getItemName());
					flippingItem.setLatestBuy(Optional.of(dummyOffer));
				}
				else {
					dummyOffer = OfferEvent.dummyOffer(false, false, num, flippingItem.getItemId(), flippingItem.getItemName());
					flippingItem.setLatestSell(Optional.of(dummyOffer));
				}

				refreshProperties();
			}
			catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "You need to input a number");
				return;
			}
			propertyPanel.remove(textField);
			propertyPanel.add(valueLabel, BorderLayout.EAST);
			revalidate();
			repaint();
		}));

		propertyPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (isHighlighted[0]) {
					isHighlighted[0] = false;
					propertyPanel.remove(textField);
					propertyPanel.add(valueLabel, BorderLayout.EAST);
				}
				else {
					isHighlighted[0] = true;
					propertyPanel.remove(valueLabel);
					propertyPanel.add(textField, BorderLayout.EAST);
				}
				revalidate();
				repaint();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				propertyPanel.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				propertyPanel.setBackground(CustomColors.DARK_GRAY);
			}
		});
	}


	/**
	 * holds the ge limit remaining on the left, the ge refresh timer in the center, and the roi on the right.
	 * @return
	 */
	private JPanel createGeLimitRefreshTimeAndRoiPanel() {
		JPanel geLimitPanel = new JPanel(new DynamicGridLayout(2,1,0,5));
		geLimitPanel.setBorder(new EmptyBorder(0,0,0,10));
		geLimitPanel.setBackground(CustomColors.DARK_GRAY);
		geLimitPanel.add(geLimitText);
		geLimitPanel.add(limitLabelVal);

		geLimitPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int result = JOptionPane.showConfirmDialog(FlippingItemPanel.this, "Reset ge limit?");
				if (result == 0) {
					flippingItem.resetGeLimit();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				geLimitPanel.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				geLimitPanel.setBackground(CustomColors.DARK_GRAY);
			}
		});

		JPanel roiPanel = new JPanel(new DynamicGridLayout(2,1,0,5));
		roiPanel.setBackground(CustomColors.DARK_GRAY);
		roiPanel.setBorder(new EmptyBorder(0,15,0,0));
		roiPanel.add(roiText);
		roiPanel.add(roiLabelVal);

		//hold the ge limit timer and the text that shows the local time the limit will refresh at
		JPanel geRefreshTimePanel = new JPanel(new DynamicGridLayout(2,1,0, 2));
		geRefreshTimePanel.setBorder(new EmptyBorder(5,0,5,0));
		geRefreshTimePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
		geRefreshTimePanel.add(geRefreshLabel);
		geRefreshTimePanel.add(geRefreshAtLabel);

		//holds the ge limit remaining on the left, the ge refresh timer in the center, and the roi on the right.
		JPanel geLimitRefreshTimeAndRoiPanel = new JPanel(new BorderLayout());
		geLimitRefreshTimeAndRoiPanel.setBorder(new EmptyBorder(10,8,6,8));
		geLimitRefreshTimeAndRoiPanel.setBackground(CustomColors.DARK_GRAY);
		geLimitRefreshTimeAndRoiPanel.add(geRefreshTimePanel, BorderLayout.CENTER);
		geLimitRefreshTimeAndRoiPanel.add(geLimitPanel, BorderLayout.WEST);
		geLimitRefreshTimeAndRoiPanel.add(roiPanel, BorderLayout.EAST);

		MouseAdapter geRefreshLabelsListener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (timeInfoPanel.isVisible()) {
					timeInfoPanel.setVisible(false);
				}
				else {
					timeInfoPanel.setVisible(true);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if (!timeInfoPanel.isVisible()) {
					geRefreshTimePanel.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (!timeInfoPanel.isVisible()) {
					geRefreshTimePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
				}
			}
		};

		//i have to attach it to everything cause otherwise it only listens to the specific area not covered by some other component.
		geRefreshTimePanel.addMouseListener(geRefreshLabelsListener);
		geRefreshLabel.addMouseListener(geRefreshLabelsListener);
		geRefreshAtLabel.addMouseListener(geRefreshLabelsListener);

		return geLimitRefreshTimeAndRoiPanel;
	}

	private JPanel createTimeInfoPanel() {
		JPanel timeInfoPanel = new JPanel(new DynamicGridLayout(5, 1));
		timeInfoPanel.setBackground(getBackground());

		JPanel priceCheckBuyTimePanel = new JPanel(new BorderLayout());
		JPanel priceCheckSellTimePanel = new JPanel(new BorderLayout());
		JPanel latestBuyTimePanel = new JPanel(new BorderLayout());
		JPanel latestSellTimePanel = new JPanel(new BorderLayout());

		JLabel priceCheckBuyTimeText = new JLabel("Time since PC buy: ");
		JLabel priceCheckSellTimeText = new JLabel("Time since PC sell: ");
		JLabel latestBuyTimeText = new JLabel("Time since last buy: ");
		JLabel latestSellTimeText = new JLabel("Time since last sell: ");

		JPanel[] panels = {priceCheckBuyTimePanel, priceCheckSellTimePanel, latestBuyTimePanel, latestSellTimePanel};
		JLabel[] descriptionLabels = {priceCheckBuyTimeText, priceCheckSellTimeText, latestBuyTimeText, latestSellTimeText};
		JLabel[] timerValueLabels = {priceCheckBuyTimeVal,priceCheckSellTimeVal,latestBuyTimeVal,latestSellTimeVal};
		JLabel[] dateLabels = {latestPcBuyAt, latestPcSellAt, latestBoughtAt, latestSoldAt};

		for (int i=0;i<panels.length;i++) {
			descriptionLabels[i].setFont(plugin.getFont());
			descriptionLabels[i].setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
			timerValueLabels[i].setHorizontalAlignment(JLabel.CENTER);
			dateLabels[i].setHorizontalAlignment(JLabel.CENTER);
			dateLabels[i].setFont(plugin.getFont());
			dateLabels[i].setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
			timerValueLabels[i].setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
			timerValueLabels[i].setFont(FontManager.getRunescapeBoldFont());
			timerValueLabels[i].setOpaque(true);
			timerValueLabels[i].setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
			panels[i].setBackground(CustomColors.DARK_GRAY);
			panels[i].setBorder(new EmptyBorder(4,8,8,8));
			panels[i].add(descriptionLabels[i], BorderLayout.WEST);
			panels[i].add(createTimerAndDatePanel(timerValueLabels[i], dateLabels[i]), BorderLayout.EAST);
			timeInfoPanel.add(panels[i]);
		}
		return timeInfoPanel;
	}

	private JPanel createTimerAndDatePanel(JLabel timerLabel, JLabel dateLabel) {
		JPanel timerAndDatePanel = new JPanel(new DynamicGridLayout(2,1,0,2));
		timerAndDatePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
		timerAndDatePanel.setBorder(new EmptyBorder(5,5,5,5));
		timerAndDatePanel.add(timerLabel);
		timerAndDatePanel.add(dateLabel);
		return timerAndDatePanel;
	}



	private void setValueLabels() {
		Arrays.asList(latestBuyPriceVal, latestSellPriceVal, priceCheckBuyVal, priceCheckSellVal, profitEachVal, potentialProfitVal,
				roiLabelVal, limitLabelVal).
				forEach(label -> {
					label.setHorizontalAlignment(JLabel.RIGHT);
					label.setFont(plugin.getFont());
					if (label == limitLabelVal) {
						limitLabelVal.setHorizontalAlignment(JLabel.CENTER);
						label.setForeground(ColorScheme.GRAND_EXCHANGE_LIMIT);
					}
					if (label == roiLabelVal) {
						roiLabelVal.setHorizontalAlignment(JLabel.CENTER);
					}
				});

		latestBuyPriceVal.setForeground(Color.white);
		latestSellPriceVal.setForeground(Color.white);
		latestBuyPriceVal.setFont(CustomFonts.RUNESCAPE_BOLD_FONT);
		latestSellPriceVal.setFont(CustomFonts.RUNESCAPE_BOLD_FONT);

		priceCheckBuyVal.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
		priceCheckSellVal.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);

		profitEachVal.setForeground(CustomColors.PROFIT_COLOR);
		potentialProfitVal.setForeground(CustomColors.PROFIT_COLOR);

		geRefreshLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
		geRefreshLabel.setFont(FontManager.getRunescapeBoldFont());
		geRefreshLabel.setHorizontalAlignment(JLabel.CENTER);
		geRefreshLabel.setToolTipText("This is a timer displaying how much time is left before the GE limit refreshes for this item");

		geRefreshAtLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
		geRefreshAtLabel.setFont(FontManager.getRunescapeSmallFont());
		geRefreshAtLabel.setHorizontalAlignment(JLabel.CENTER);
		geRefreshAtLabel.setToolTipText("This shows the local time when the ge limit will refresh");

		roiLabelVal.setToolTipText("<html>Return on investment:<br>Percentage of profit relative to gp invested</html>");

		refreshProperties();
	}

	private void setDescriptionLabels() {
		Arrays.asList(latestBuyPriceText, latestSellPriceText, priceCheckBuyText, priceCheckSellText, profitEachText, profitTotalText, geLimitText, roiText).
				forEach(label -> {
					label.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
					label.setFont(plugin.getFont());
				});

		/* Tooltips */
		priceCheckBuyText.setToolTipText("The buy price according to your latest margin check. This is the price you insta sold the item for");
		priceCheckSellText.setToolTipText("The sell price according to your latest margin check. This is the price you insta bought the item for");
		latestBuyPriceText.setToolTipText("The last price you bought this item for");
		latestSellPriceText.setToolTipText("The last price you sold this item for");
		profitEachText.setToolTipText("The profit margin according to your latest margin check");
		profitTotalText.setToolTipText("The potential profit according to your latest margin check and GE 4-hour limit");
		geLimitText.setToolTipText("Remaining ge limit");

		if (flippingItem.getTotalGELimit() <= 0) {
			geLimitText.setText("Bought:");
			geLimitText.setToolTipText("Item has unknown limit, so this just displays how many you have bought in a 4 hour window");
		}
	}

	/**
	 * Creates the title panel which holds the item icon, delete button (shows up only when you hover over the item icon),
	 * the item name label, and the favorite button.
	 *
	 * @param itemIcon
	 * @param deleteButton
	 * @param itemNameLabel
	 * @param favoriteButton
	 * @return
	 */
	private JPanel createTitlePanel(JLabel itemIcon, JButton deleteButton, JLabel itemNameLabel, JLabel favoriteButton)
	{
		JPanel itemClearPanel = new JPanel(new BorderLayout());
		itemClearPanel.setBackground(getBackground());
		itemClearPanel.add(itemIcon, BorderLayout.WEST);
		itemClearPanel.add(deleteButton, BorderLayout.EAST);

		JPanel titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBackground(getBackground());
		titlePanel.add(itemClearPanel, BorderLayout.WEST);
		titlePanel.add(itemNameLabel, BorderLayout.CENTER);
		titlePanel.add(favoriteButton, BorderLayout.EAST);
		titlePanel.setBorder(new EmptyBorder(2, 1, 2, 1));
		titlePanel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				itemIcon.setVisible(false);
				deleteButton.setVisible(true);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				deleteButton.setVisible(false);
				itemIcon.setVisible(true);
			}
		});
		return titlePanel;
	}

	/**
	 * Creates the image icon located on the title panel
	 *
	 * @param itemImage the image of the item as given by the ItemManager
	 * @return
	 */
	private JLabel createItemIcon(AsyncBufferedImage itemImage)
	{
		JLabel itemIcon = new JLabel();
		itemIcon.setAlignmentX(Component.LEFT_ALIGNMENT);
		itemIcon.setPreferredSize(Icons.ICON_SIZE);
		if (itemImage != null)
		{
			itemImage.addTo(itemIcon);
		}
		return itemIcon;
	}

	/**
	 * Creates the delete button located on the title panel which shows up when you hover over the image icon.
	 *
	 * @return
	 */
	private JButton createDeleteButton()
	{
		JButton clearButton = new JButton(Icons.DELETE_ICON);
		clearButton.setPreferredSize(Icons.ICON_SIZE);
		clearButton.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		clearButton.setBorder(null);
		clearButton.setBorderPainted(false);
		clearButton.setContentAreaFilled(false);
		clearButton.setVisible(false);
		clearButton.setToolTipText("Delete item");
		clearButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON1)
				{
					flippingItem.setValidFlippingPanelItem(false);
					if (!plugin.getAccountCurrentlyViewed().equals(FlippingPlugin.ACCOUNT_WIDE)) {
						plugin.markAccountTradesAsHavingChanged(plugin.getAccountCurrentlyViewed());
					}
					plugin.getFlippingPanel().rebuild(plugin.viewTradesForCurrentView());
				}
			}
		});
		return clearButton;
	}

	/**
	 * Creates the item name label that is located on the title panel. The item name label can be clicked on to
	 * expand or collapse the itemInfo panel
	 *
	 * @return
	 */
	private JLabel createItemNameLabel()
	{
		JLabel itemNameLabel = new JLabel(flippingItem.getItemName(), SwingConstants.CENTER);
		itemNameLabel.setFont(FontManager.getRunescapeBoldFont());
		itemNameLabel.setPreferredSize(new Dimension(0, 0)); //Make sure the item name fits
		itemNameLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (isCollapsed())
				{
					expand();
					flippingItem.setExpand(true);
				}
				else
				{
					collapse();
					flippingItem.setExpand(false);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				if (isCollapsed())
				{
					itemNameLabel.setText("Expand");
				}
				else
				{
					itemNameLabel.setText("Collapse");
				}
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				itemNameLabel.setText(flippingItem.getItemName());
			}
		});
		return itemNameLabel;
	}

	/**
	 * Creates the favorite icon used for favoriting items.
	 *
	 * @return
	 */
	private JLabel createFavoriteIcon()
	{
		JLabel favoriteIcon = new JLabel();
		favoriteIcon.setIcon(flippingItem.isFavorite() ? Icons.STAR_ON_ICON : Icons.STAR_OFF_ICON);
		favoriteIcon.setAlignmentX(Component.RIGHT_ALIGNMENT);
		favoriteIcon.setPreferredSize(new Dimension(24, 24));
		favoriteIcon.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (plugin.getAccountCurrentlyViewed().equals(FlippingPlugin.ACCOUNT_WIDE))
				{
					plugin.setFavoriteOnAllAccounts(flippingItem, !flippingItem.isFavorite());
				}
				else {
					plugin.markAccountTradesAsHavingChanged(plugin.getAccountCurrentlyViewed());
				}
				flippingItem.setFavorite(!flippingItem.isFavorite());
				favoriteIcon.setIcon(flippingItem.isFavorite()? Icons.STAR_ON_ICON:Icons.STAR_OFF_ICON);

				if (flippingItem.isFavorite()) {
					searchCodeLabel.setText("<html> quick search code: " + UIUtilities.colorText(flippingItem.getFavoriteCode(), ColorScheme.GRAND_EXCHANGE_PRICE) + "</html>");
				}
				else {
					searchCodeLabel.setText("<html> quick search code: " + UIUtilities.colorText("N/A", CustomColors.VIBRANT_YELLOW) + "</html>");
				}
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				if (!flippingItem.isFavorite())
				{
					favoriteIcon.setIcon(Icons.STAR_HALF_ON_ICON);
				}
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				if (!flippingItem.isFavorite())
				{
					favoriteIcon.setIcon(Icons.STAR_OFF_ICON);
				}
			}
		});

		return favoriteIcon;
	}

	public void expand()
	{
		if (isCollapsed())
		{
			itemInfo.setVisible(true);
		}
	}

	public void collapse()
	{
		if (!isCollapsed())
		{
			itemInfo.setVisible(false);
		}
	}

	public boolean isCollapsed()
	{
		return !itemInfo.isVisible();
	}

	public void refreshProperties() {
		Optional<OfferEvent> latestMarginCheckBuy = flippingItem.getLatestMarginCheckBuy();
		Optional<OfferEvent> latestMarginCheckSell = flippingItem.getLatestMarginCheckSell();

		Optional<OfferEvent> latestBuy = flippingItem.getLatestBuy();
		Optional<OfferEvent> latestSell = flippingItem.getLatestSell();

		Optional<Integer> profitEach = flippingItem.getCurrentProfitEach();
		Optional<Integer> potentialProfit = flippingItem.getPotentialProfit(plugin.getConfig().marginCheckLoss(), plugin.getConfig().geLimitProfit());

		Optional<Float> roi =  flippingItem.getCurrentRoi();

		priceCheckBuyVal.setText(latestMarginCheckSell.isPresent() ? String.format(NUM_FORMAT, latestMarginCheckSell.get().getPrice()) + " gp":"N/A");
		priceCheckSellVal.setText(latestMarginCheckBuy.isPresent() ? String.format(NUM_FORMAT, latestMarginCheckBuy.get().getPrice()) + " gp" : "N/A");

		latestBuyPriceVal.setText(latestBuy.isPresent() ? String.format(NUM_FORMAT, latestBuy.get().getPrice()) + " gp" : "N/A");
		latestSellPriceVal.setText(latestSell.isPresent() ? String.format(NUM_FORMAT, latestSell.get().getPrice()) + " gp" : "N/A");

		profitEachVal.setText(profitEach.isPresent()? QuantityFormatter.quantityToRSDecimalStack(profitEach.get()) + " gp": "N/A");
		potentialProfitVal.setText(potentialProfit.isPresent() ? QuantityFormatter.quantityToRSDecimalStack(potentialProfit.get()) + " gp": "N/A");

		roiLabelVal.setText(roi.isPresent()? String.format("%.2f", roi.get()) + "%" : "N/A");
		//Color gradient red-yellow-green depending on ROI.
		roiLabelVal.setForeground(UIUtilities.gradiatePercentage(roi.orElse(0F), plugin.getConfig().roiGradientMax()));

		latestPcBuyAt.setText(latestMarginCheckBuy.isPresent()? TimeFormatters.formatTime(latestMarginCheckBuy.get().getTime(), true, true):"N/A");
		latestPcSellAt.setText(latestMarginCheckSell.isPresent()? TimeFormatters.formatTime(latestMarginCheckSell.get().getTime(), true, true):"N/A");
		latestBoughtAt.setText(latestBuy.isPresent()? TimeFormatters.formatTime(latestBuy.get().getTime(), true, true):"N/A");
		latestSoldAt.setText(latestSell.isPresent()? TimeFormatters.formatTime(latestSell.get().getTime(), true, true):"N/A");

		if (flippingItem.getTotalGELimit() > 0) {
			limitLabelVal.setText(String.format(NUM_FORMAT, flippingItem.getRemainingGeLimit()));
		} else {
			limitLabelVal.setText(String.format(NUM_FORMAT, flippingItem.getItemsBoughtThisLimitWindow()));
			//can't have potential profit if the limit is unknown
			potentialProfitVal.setText("N/A");
		}
	}

	public void updateTimerDisplays() {
		flippingItem.validateGeProperties();

		geRefreshLabel.setText(flippingItem.getGeLimitResetTime() == null?
				TimeFormatters.formatDuration(Duration.ZERO):
				TimeFormatters.formatDuration(Instant.now(), flippingItem.getGeLimitResetTime()));

		//need to update this so it can be reset when the timer runs down.
		if (flippingItem.getTotalGELimit() > 0) {
			limitLabelVal.setText(String.format(NUM_FORMAT, flippingItem.getRemainingGeLimit()));
		} else {
			limitLabelVal.setText(String.format(NUM_FORMAT, flippingItem.getItemsBoughtThisLimitWindow()));
		}

		geRefreshAtLabel.setText(flippingItem.getGeLimitResetTime() == null? "Now": TimeFormatters.formatTime(flippingItem.getGeLimitResetTime(), true, false));

		setTimeString(flippingItem.getLatestMarginCheckBuy(), priceCheckBuyTimeVal);
		setTimeString(flippingItem.getLatestMarginCheckSell(), priceCheckSellTimeVal);
		setTimeString(flippingItem.getLatestBuy(), latestBuyTimeVal);
		setTimeString(flippingItem.getLatestSell(), latestSellTimeVal);
	}

	private void setTimeString(Optional<OfferEvent> offerEvent, JLabel timeLabel) {
		if (!offerEvent.isPresent()) {
			timeLabel.setText("N/A");
		}
		else {
			//if difference is more than a day don't show it as HH:MM:SS
			if (Instant.now().getEpochSecond() - offerEvent.get().getTime().getEpochSecond() > 86400) {
				timeLabel.setText(TimeFormatters.formatDurationTruncated(offerEvent.get().getTime()));
			}
			else {
				timeLabel.setText(TimeFormatters.formatDuration(offerEvent.get().getTime()));
			}
		}
	}

}
