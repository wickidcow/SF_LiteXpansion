package dev.j3fftw.litexpansion.items;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

/** Exact list rules and rich-component preservation, not a live cargo-transfer simulation. */
class CargoConfiguratorLoreTest {
    private static final Component RICH = Component.text("Original description", NamedTextColor.AQUA)
            .hoverEvent(Component.text("Original hover")).clickEvent(ClickEvent.suggestCommand("/help"));

    @Test void absentLoreUsesDetachedDefaults() {
        var defaults = List.of(RICH);
        var result = CargoConfiguratorLore.current(null, defaults);
        assertEquals(defaults, result);
        assertSame(RICH, result.getFirst());
        result.clear();
        assertEquals(1, defaults.size());
    }

    @Test void absentTemplateLoreStaysEmpty() {
        assertTrue(CargoConfiguratorLore.orEmpty(null).isEmpty());
    }

    @Test void explicitEmptyLoreIsNotReplacedWithDefaults() {
        assertTrue(CargoConfiguratorLore.current(List.of(), List.of(RICH)).isEmpty());
    }

    @Test void sameLengthClearPreservesCustomizedRichLore() {
        var result = CargoConfiguratorLore.cleared(List.of(RICH), List.of(Component.text("Default")));
        assertSame(RICH, result.getFirst());
    }

    @Test void changedLengthClearRestoresTheOriginalTemplateComponents() {
        for (var existing : List.of(List.<Component>of(), List.<Component>of(Component.empty(), Component.empty()))) {
            assertSame(RICH, CargoConfiguratorLore.cleared(existing, List.of(RICH)).getFirst());
        }
    }

    @Test void copyAppendsTwoLinesAndKeepsUnrelatedRichComponents() {
        var result = CargoConfiguratorLore.copied(List.of(RICH), List.of(Component.text("Default")), Component.text("Input"));
        assertEquals(3, result.size());
        assertSame(RICH, result.getFirst());
        assertEquals("", plain(result.get(1)));
        assertEquals("> Copied Input config!", plain(result.get(2)));
    }

    @Test void exactGeneratedTailLengthResetsBeforeCopying() {
        var result = CargoConfiguratorLore.copied(
                List.of(Component.text("custom"), Component.empty(), Component.text("old tail")),
                List.of(RICH), Component.text("Output"));
        assertEquals(3, result.size());
        assertSame(RICH, result.getFirst());
        assertEquals("> Copied Output config!", plain(result.getLast()));
    }

    @Test void otherTailLengthsKeepTheHistoricalAppendBehavior() {
        var current = List.of(RICH, Component.text("second"));
        var result = CargoConfiguratorLore.copied(current, List.of(Component.text("default")), Component.text("Node"));
        assertEquals(4, result.size());
        assertEquals(current, result.subList(0, 2));
    }

    @Test void copiedNodeKeepsHoverAndCustomStyle() {
        var result = CargoConfiguratorLore.copied(List.of(), List.of(), RICH);
        Component node = result.getLast().children().get(1);
        assertEquals(RICH, node);
    }

    @Test void absentNodeNameRetainsEmptyLegacyDisplayNameBehavior() {
        assertEquals("> Copied  config!", plain(CargoConfiguratorLore.copied(List.of(), List.of(), null).getLast()));
    }

    @Test void repeatedCopyDoesNotGrowTheGeneratedTail() {
        var defaults = List.of(RICH);
        var result = CargoConfiguratorLore.copied(defaults, defaults, Component.text("First"));
        for (int i = 0; i < 20; i++) result = CargoConfiguratorLore.copied(result, defaults, Component.text("Next"));
        assertEquals(3, result.size());
        assertSame(RICH, result.getFirst());
    }

    @Test void randomizedLayoutRulesMatchThePreviousStringImplementation() {
        Random random = new Random(951106L);
        for (int trial = 0; trial < 500; trial++) {
            List<Component> defaults = new ArrayList<>();
            List<Component> current = new ArrayList<>();
            for (int i = random.nextInt(8); i > 0; i--) defaults.add(Component.text("default-" + i));
            for (int i = random.nextInt(12); i > 0; i--) current.add(Component.text("current-" + i));
            var defaultsBefore = List.copyOf(defaults);
            var currentBefore = List.copyOf(current);
            List<String> legacyClear = strings(current);
            if (legacyClear.size() != defaults.size()) legacyClear = strings(defaults);
            assertEquals(legacyClear, strings(CargoConfiguratorLore.cleared(current, defaults)));
            List<String> legacyCopy = strings(current);
            if (legacyCopy.size() == defaults.size() + 2) legacyCopy = strings(defaults);
            legacyCopy.add("");
            legacyCopy.add("> Copied Node config!");
            assertEquals(legacyCopy, strings(CargoConfiguratorLore.copied(current, defaults, Component.text("Node"))));
            assertEquals(defaultsBefore, defaults);
            assertEquals(currentBefore, current);
        }
    }

    private static String plain(Component value) {
        return PlainTextComponentSerializer.plainText().serialize(value);
    }

    private static List<String> strings(List<Component> values) {
        return new ArrayList<>(values.stream().map(CargoConfiguratorLoreTest::plain).toList());
    }
}
