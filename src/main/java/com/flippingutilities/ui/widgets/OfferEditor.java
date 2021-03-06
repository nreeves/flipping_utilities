

package com.flippingutilities.ui.widgets;

import com.flippingutilities.ui.uiutilities.Icons;
import net.runelite.api.Client;
import net.runelite.api.FontID;
import net.runelite.api.VarClientStr;
import net.runelite.api.widgets.*;

import javax.swing.*;

public class OfferEditor
{
    private final Client client;
    private Widget text;
    private Widget buttonText;

    public OfferEditor(Widget parent, Client client)
    {
        this.client = client;

        if (parent == null)
        {
            return;
        }

        text = parent.createChild(-1, WidgetType.TEXT);
        buttonText = parent.createChild(-1, WidgetType.TEXT);

        prepareTextWidget(buttonText, WidgetPositionMode.ABSOLUTE_TOP, 5);
        prepareTextWidget(text, WidgetPositionMode.ABSOLUTE_BOTTOM, 15);

        buttonText.setFontId(FontID.QUILL_8);
    }


    private void prepareTextWidget(Widget widget, int yMode, int originalY) {
        widget.setTextColor(0x800000);
        widget.setFontId(FontID.VERDANA_11_BOLD);
        widget.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER);
        widget.setOriginalX(0);
        widget.setYPositionMode(yMode);
        widget.setOriginalY(originalY);
        widget.setOriginalHeight(20);
        widget.setXTextAlignment(WidgetTextAlignment.CENTER);
        widget.setYTextAlignment(WidgetTextAlignment.CENTER);
        widget.setWidthMode(WidgetSizeMode.MINUS);
        widget.setHasListener(true);
        widget.setOnMouseRepeatListener((JavaScriptCallback) ev -> widget.setTextColor(0xFFFFFF));
        widget.setOnMouseLeaveListener((JavaScriptCallback) ev -> widget.setTextColor(0x800000));
        widget.revalidate();
    }

    public void update(String mode, int value)
    {
        switch (mode)
        {
            case ("quantity"):
                text.setText("OR click this to use the quantity editor hotkeys!");
                text.setAction(1, "pic");
                text.setOnOpListener((JavaScriptCallback) ev -> {
                    SwingUtilities.invokeLater(()-> {
                        JOptionPane.showMessageDialog(null, Icons.QUANTITY_EDITOR_PIC);
                    });
                });

                buttonText.setText("click this to set to remaining GE limit: " + value);
                buttonText.setAction(1, "Set quantity");
                buttonText.setOnOpListener((JavaScriptCallback) ev ->
                {
                    client.getWidget(WidgetInfo.CHATBOX_FULL_INPUT).setText(value + "*");
                    client.setVar(VarClientStr.INPUT_TEXT, String.valueOf(value));
                });
                break;
            case ("setSellPrice"):
                text.setText("OR click this to use the price editor hotkeys!");
                text.setAction(1, "pic");
                text.setOnOpListener((JavaScriptCallback) ev -> {
                    SwingUtilities.invokeLater(()-> {
                        JOptionPane.showMessageDialog(null, Icons.PRICE_EDITOR_PIC);
                    });
                });

                if (value != 0) {
                    buttonText.setText("click this to set to latest margin sell price: " + String.format("%,d", value) + " gp");
                    buttonText.setAction(1, "Set price");
                    buttonText.setOnOpListener((JavaScriptCallback) ev ->
                    {
                        client.getWidget(WidgetInfo.CHATBOX_FULL_INPUT).setText(value + "*");
                        client.setVar(VarClientStr.INPUT_TEXT, String.valueOf(value));
                    });
                }

                break;
            case ("setBuyPrice"):
                text.setText("OR click this to use the price editor hotkeys!");
                text.setAction(1, "pic");
                text.setOnOpListener((JavaScriptCallback) ev -> {
                    SwingUtilities.invokeLater(()-> {
                        JOptionPane.showMessageDialog(null, Icons.PRICE_EDITOR_PIC);
                    });
                });
                if (value != 0) {
                    buttonText.setText("click this to set to latest margin buy price: " + String.format("%,d", value) + " gp");
                    buttonText.setAction(1, "Set price");
                    buttonText.setOnOpListener((JavaScriptCallback) ev ->
                    {
                        client.getWidget(WidgetInfo.CHATBOX_FULL_INPUT).setText(value + "*");
                        client.setVar(VarClientStr.INPUT_TEXT, String.valueOf(value));
                    });
                }

                break;
            case ("reset"):
                text.setText("");
        }
    }
}