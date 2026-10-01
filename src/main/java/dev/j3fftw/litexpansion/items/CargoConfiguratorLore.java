package dev.j3fftw.litexpansion.items;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/** Presentation-only rules; never reads or changes cargo payloads or item identity. */
final class CargoConfiguratorLore {
    private CargoConfiguratorLore() {}

    static List<Component> orEmpty(List<Component> lore) {
        return lore == null ? new ArrayList<>() : new ArrayList<>(lore);
    }

    static List<Component> current(List<Component> lore, List<Component> defaults) {
        return orEmpty(lore == null ? defaults : lore);
    }

    static List<Component> cleared(List<Component> lore, List<Component> defaults) {
        // Preserve the historical size rule: same-sized customized descriptions stay.
        return orEmpty(lore.size() == defaults.size() ? lore : defaults);
    }

    static List<Component> copied(List<Component> lore, List<Component> defaults, Component nodeName) {
        // The old configurator replaced exactly one generated two-line tail.
        List<Component> result = orEmpty(lore.size() == defaults.size() + 2 ? defaults : lore);
        result.add(Component.empty());
        result.add(Component.empty().decoration(TextDecoration.ITALIC, false)
                .append(Component.text("> Copied ", NamedTextColor.GRAY))
                .append(nodeName == null ? Component.empty() : nodeName.colorIfAbsent(NamedTextColor.WHITE))
                .append(Component.text(" config!", NamedTextColor.GRAY)));
        return result;
    }
}
