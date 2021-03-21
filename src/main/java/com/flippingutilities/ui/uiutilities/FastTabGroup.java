
package com.flippingutilities.ui.uiutilities;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

//have to extend MaterialTabGroup just to make it compatible with MaterialTab...not actually using any functionality
//from the parent. The difference between this TabGroup and MaterialTabGroup is just that this uses a card layout
//cause its faster when switching between different views
@Slf4j
public class FastTabGroup extends MaterialTabGroup
{
	/* The panel on which the tab's content or the view will be displayed on. */
	private final JPanel display;
	/* A list of all the tabs contained in this group. */
	private final List<MaterialTab> tabs = new ArrayList<>();
	@Getter
	private String lastSelectedTab;
	@Getter
	private JComponent currentlySelectedTab;
	private boolean currentlyShowingView = false;

	public FastTabGroup(JPanel display)
	{
		this.display = display;
		this.display.setLayout(new CardLayout());
		setLayout(new FlowLayout(FlowLayout.CENTER, 8, 0));
		setOpaque(false);
	}

	public void addTab(MaterialTab tab)
	{
		tabs.add(tab);
		display.add(tab.getContent(), tab.getText());
		add(tab, BorderLayout.NORTH);
	}

	/**
	 * A "view" is just a Jpanel. It just doesn't have a tab that can be selected by the user to display it. As such, a view
	 * is displayed programatically when it needs to be. For example, the GeHistoryTabPanel is a "view" and doesn't have
	 * a tab. It is displayed only when the user looks at their ge history.
	 * @param panel the view to add to the main display panel
	 * @param name the name of the view. This name is so that the view can be shown using it.
	 */
	public void addView(JPanel panel, String name) {
		display.add(panel, name);
	}

	public boolean select(MaterialTab selectedTab)
	{
		// If the OnTabSelected returned false, exit the method to prevent tab switching
		if (!selectedTab.select())
		{
			return false;
		}
		currentlyShowingView = false;
		lastSelectedTab = selectedTab.getText();
		currentlySelectedTab = selectedTab.getContent();
		CardLayout cardLayout = (CardLayout) display.getLayout();
		cardLayout.show(display, selectedTab.getText());

		//Unselect all other tabs
		for (MaterialTab tab : tabs)
		{
			if (!tab.equals(selectedTab))
			{
				tab.unselect();
			}
		}
		return true;
	}

	public void unselectAll() {
		tabs.forEach(tab-> tab.unselect());
	}

	public void showView(String name) {
		currentlyShowingView = true;
		unselectAll();
		CardLayout cardLayout = (CardLayout) display.getLayout();
		cardLayout.show(display, name);
	}

	public void revertToSafeDisplay() {
		if (currentlyShowingView) {

			selectPreviouslySelectedTab();
		}
	}

	public void selectPreviouslySelectedTab() {
		for (MaterialTab tab: tabs) {
			if (tab.getText().equals(lastSelectedTab)) {
				select(tab);
				break;
			}
		}
	}
}
