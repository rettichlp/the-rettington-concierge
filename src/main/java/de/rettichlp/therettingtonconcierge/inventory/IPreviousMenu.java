package de.rettichlp.therettingtonconcierge.inventory;

import net.kyori.adventure.text.Component;

import java.util.List;

public interface IPreviousMenu {

    Component previousMenuItemName();

    List<Component> previousMenuItemTooltip();

    GameMenu previousMenu();
}
