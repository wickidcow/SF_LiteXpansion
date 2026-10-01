package dev.j3fftw.litexpansion.items;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.Test;

/** Exercises the production pure component helper, not a live cargo-transfer or Doctor migration. */
public class CargoConfiguratorLoreTest {
    private static final Component RICH = Component.text("existing", TextColor.color(0x1278AB))
            .decorate(TextDecoration.BOLD)
            .hoverEvent(HoverEvent.showText(Component.text("retained hover")))
            .clickEvent(ClickEvent.suggestCommand("/sf doctor status"))
            .insertion("original insertion")
            .append(Component.translatable("item.minecraft.diamond"));
    private static final List<Component> DEFAULTS = List.of(Component.text("default1"), Component.text("default2"));

    @Test public void missingLoreUsesDefaultsWithoutAliasingTheInput() {
        List<Component> result = CargoConfiguratorLore.current(null, DEFAULTS);
        assertEquals(DEFAULTS, result);
        result.clear();
        assertEquals(2, DEFAULTS.size());
    }

    @Test public void emptyLoreRemainsDifferentFromAbsentLore() {
        assertTrue(CargoConfiguratorLore.current(List.of(), DEFAULTS).isEmpty());
        assertEquals(DEFAULTS, CargoConfiguratorLore.current(null, DEFAULTS));
    }

    @Test public void currentRetainsEveryExistingRichComponent() {
        assertSame(RICH, CargoConfiguratorLore.current(List.of(RICH), DEFAULTS).get(0));
    }

    @Test public void clearKeepsMatchingSizeCustomLoreExactly() {
        List<Component> custom = List.of(RICH, Component.text("player line"));
        List<Component> result = CargoConfiguratorLore.cleared(custom, DEFAULTS);
        assertEquals(custom, result);
        assertSame(RICH, result.get(0));
    }

    @Test public void clearRevertsDifferentSizedLoreToDefaults() {
        assertEquals(DEFAULTS, CargoConfiguratorLore.cleared(List.of(RICH), DEFAULTS));
        assertEquals(DEFAULTS, CargoConfiguratorLore.cleared(List.of(RICH, RICH, RICH), DEFAULTS));
    }

    @Test public void clearDoesNotChangeEitherSourceList() {
        List<Component> existing = new ArrayList<>(List.of(RICH));
        List<Component> defaults = new ArrayList<>(DEFAULTS);
        CargoConfiguratorLore.cleared(existing, defaults).clear();
        assertEquals(List.of(RICH), existing);
        assertEquals(DEFAULTS, defaults);
    }

    @Test public void copyReplacesOnlyTheExactTwoLineTailCase() {
        List<Component> existing = List.of(RICH, RICH, Component.text("old spacer"), Component.text("old node"));
        List<Component> result = CargoConfiguratorLore.copied(existing, DEFAULTS, Component.text("new node"));
        assertEquals(4, result.size());
        assertEquals(DEFAULTS, result.subList(0, 2));
        assertEquals("", plain(result.get(2)));
        assertEquals("> Copied new node config!", plain(result.get(3)));
    }

    @Test public void copyPreservesUnusualLegacyLayoutsRatherThanNormalizingThem() {
        for (int size : new int[] {0, 1, 2, 3, 5, 6, 20}) {
            List<Component> existing = new ArrayList<>();
            for (int i = 0; i < size; i++) existing.add(RICH);
            List<Component> result = CargoConfiguratorLore.copied(existing, DEFAULTS, Component.text("node"));
            assertEquals(size + 2, result.size());
            for (int i = 0; i < size; i++) assertSame(RICH, result.get(i));
        }
    }

    @Test public void copyPreservesRichNodeNameAndDoesNotInheritPrefixColor() {
        Component node = RICH.color(null);
        List<Component> result = CargoConfiguratorLore.copied(List.of(), DEFAULTS, node);
        Component summary = result.get(1);
        assertNull(summary.color());
        assertEquals(NamedTextColor.GRAY, summary.children().get(0).color());
        assertSame(node, summary.children().get(1));
        assertEquals(NamedTextColor.GRAY, summary.children().get(2).color());
        assertNotNull(summary.children().get(1).hoverEvent());
    }

    @Test public void copyAllowsAnAbsentDisplayNameWithoutInventingAName() {
        assertEquals("> Copied  config!", plain(CargoConfiguratorLore.copied(List.of(), DEFAULTS, null).get(1)));
    }

    @Test public void copyDoesNotMutateInputLists() {
        List<Component> existing = new ArrayList<>(List.of(RICH));
        List<Component> defaults = new ArrayList<>(DEFAULTS);
        CargoConfiguratorLore.copied(existing, defaults, RICH).clear();
        assertEquals(List.of(RICH), existing);
        assertEquals(DEFAULTS, defaults);
    }

    @Test public void repeatedCopiesRetainTheHistoricalSizeDrivenBehavior() {
        List<Component> lore = CargoConfiguratorLore.current(null, DEFAULTS);
        for (int i = 0; i < 30; i++) {
            lore = CargoConfiguratorLore.copied(lore, DEFAULTS, Component.text("node" + i));
            assertEquals(4, lore.size());
            assertEquals("> Copied node" + i + " config!", plain(lore.get(3)));
        }
        assertEquals(DEFAULTS, CargoConfiguratorLore.cleared(lore, DEFAULTS));
    }

    @Test public void nativeLoreRetainsWhatTheOldStringRoundTripLost() {
        Component oldRoundTrip = LegacyComponentSerializer.legacySection().deserialize(
                LegacyComponentSerializer.legacySection().serialize(RICH));
        assertNotEquals(RICH, oldRoundTrip);
        assertNull(oldRoundTrip.hoverEvent());
        assertSame(RICH, CargoConfiguratorLore.copied(List.of(RICH), DEFAULTS, RICH).get(0));
    }

    @Test public void randomizedLayoutsMatchTheOriginalSelectionAndAppendRules() {
        Random random = new Random(991271L);
        for (int attempt = 0; attempt < 1000; attempt++) {
            int d = random.nextInt(15), n = random.nextInt(22);
            List<Component> defaults = new ArrayList<>(), existing = new ArrayList<>();
            for (int i = 0; i < d; i++) defaults.add(Component.text("default" + i));
            for (int i = 0; i < n; i++) existing.add(Component.text("old" + i));
            List<String> expected = strings(existing);
            if (n == d + 2) expected = strings(defaults);
            expected.add("");
            expected.add("> Copied node config!");
            assertEquals(expected, strings(CargoConfiguratorLore.copied(existing, defaults, Component.text("node"))));
            assertEquals(strings(n == d ? existing : defaults), strings(CargoConfiguratorLore.cleared(existing, defaults)));
            assertEquals(strings(existing), strings(CargoConfiguratorLore.current(existing, defaults)));
        }
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
    private static List<String> strings(List<Component> components) {
        List<String> result = new ArrayList<>();
        for (Component component : components) result.add(plain(component));
        return result;
    }
}
