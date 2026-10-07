package com.crs.ui.theme;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The custom components can be created without a window and give sensible values. */
class ThemeComponentsTest {

    @Test
    void avatarUsesFirstTwoInitials() {
        assertEquals("AK", Avatar.initialsOf("Arun Kumar"));
        assertEquals("PS", Avatar.initialsOf("  priya   sharma "));
        assertEquals("A", Avatar.initialsOf("Admin"));
        assertEquals("?", Avatar.initialsOf(" "));
    }

    @Test
    void everyChipStyleHasTextBackgroundAndBorderColours() {
        for (Chip.Style style : Chip.Style.values()) {
            java.awt.Color[] colors = Chip.colors(style);
            assertEquals(3, colors.length);
            for (java.awt.Color c : colors) assertNotNull(c, style.name());
        }
    }

    @Test
    void everyButtonVariantCanBeBuilt() {
        for (UiButton.Variant variant : UiButton.Variant.values()) {
            UiButton button = new UiButton("Go", Icons.Name.CHECK, variant);
            assertEquals("Go", button.getText());
            assertNotNull(button.getIcon());
        }
    }

    @Test
    void everyIconHasTheRequestedSize() {
        for (Icons.Name name : Icons.Name.values()) {
            assertEquals(18, Icons.of(name, 18, Theme.PRIMARY).getIconWidth());
        }
    }

    @Test
    void statCardShowsValueAndSuffix() {
        StatCard card = new StatCard("Total credits", Icons.Name.BOOK, Chip.Style.PRIMARY);
        card.setValue("7", "credits");
        assertTrue(card.getComponentCount() > 0);
    }
}
