package de.rettichlp.therettingtonconcierge.inventory;

import org.bukkit.Sound;

public interface IOpenSound {

    /**
     * Plays and returns the sound triggered when a menu is opened.
     *
     * @return the Sound instance to be played when a menu is opened
     */
    Sound openSound();
}
