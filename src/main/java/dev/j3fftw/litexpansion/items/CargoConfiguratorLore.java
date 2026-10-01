package dev.j3fftw.litexpansion.items;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

/** Component-only edits retaining the historical configurator's size-based layout rules. */
final class CargoConfiguratorLore {
    private CargoConfiguratorLore() {}

    static List<Component> current(@Nullable List<Component> existing, List<Component> defaults) {
        return new ArrayList<>(existing == null ? defaults : existing);
    }

    static List<Component> cleared(List<Component> existing, List<Component> defaults) {
        // Matching-size custom lore has always been retained by clear.
        return new ArrayList<>(existing.size() == defaults.size() ? existing : defaults);
    }

    static List<Component> copied(List<Component> existing, List<Component> defaults,
                                  @Nullable Component nodeName) {
        // Only the exact two-line suffix case is replaced; unusual old layouts
        // continue to append. Never flatten unrelated components through Strings.
        List<Component> result = new ArrayList<>(
                existing.size() == defaults.size() + 2 ? defaults : existing);
        result.add(Component.empty());
        result.add(Component.empty()
                .append(Component.text("> Copied ", NamedTextColor.GRAY))
                .append(nodeName == null ? Component.empty() : nodeName)
                .append(Component.text(" config!", NamedTextColor.GRAY)));
        return result;
    }
}
