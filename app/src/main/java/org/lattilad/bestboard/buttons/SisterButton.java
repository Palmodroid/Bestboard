package org.lattilad.bestboard.buttons;

/**
 * Sister-buttons are joined together, so any change on one sister button affect all the others.
 *
 * Sister button functionality can be implemented as:
 * - interface (*)
 * - all-button-behavior - like changingButtons
 *
 * Sister buttons can be collected:
 * - in a separate "sister-button" list
 * - together with all changingButtons (*) - each sister-button is a changing-button
 * - as part of all buttons
 *
 * Any change on sister-buttons can trigger a call on all other sisters
 * - at the time of the change (through softboarddata - layouts - changingbuttons)
 * - at the time of the change (same layout) - layout change (other layouts through
 *   LayoutView.setLayout) (*)
 */
public interface SisterButton
    {

    void refreshSisterButton(String id );
    }
