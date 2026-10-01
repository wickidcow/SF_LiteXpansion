from pathlib import Path
import hashlib
import re

def blob(data):
    return hashlib.sha1(b'blob ' + str(len(data)).encode() + b'\0' + data).hexdigest()

path = Path('src/main/java/dev/j3fftw/litexpansion/items/CargoConfigurator.java')
original = path.read_text()
assert blob(path.read_bytes()) == 'fade872af331a27b5f337bc05b587a2886414646'
text = original
replacements = (
    ('import org.bukkit.ChatColor;', 'import net.kyori.adventure.text.Component;\nimport net.kyori.adventure.text.format.NamedTextColor;'),
    ('import java.util.Arrays;\n', ''),
    ('        final List<String> defaultLore = Items.CARGO_CONFIGURATOR.getItemMetaSnapshot().getLore().orElse(new ArrayList<>());\n        final List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>(defaultLore);', '        final List<Component> canonicalLore = Items.CARGO_CONFIGURATOR.getItemMeta().lore();\n        final List<Component> defaultLore = canonicalLore == null ? List.of() : canonicalLore;\n        final List<Component> lore = CargoConfiguratorLore.current(meta.lore(), defaultLore);'),
    ('        if (lore.size() != defaultLore.size()) {\n            lore.clear();\n            lore.addAll(defaultLore);\n        }\n\n        meta.setLore(lore);', '        meta.lore(CargoConfiguratorLore.cleared(lore, defaultLore));'),
    ('                if (lore.size() == defaultLore.size() + 2) {\n                    lore.clear();\n                    lore.addAll(defaultLore);\n                }\n                lore.addAll(Arrays.asList("", ChatColor.GRAY + "> Copied "\n                        + ChatColor.RESET + clickedItemStack.getItemMeta().getDisplayName()\n                        + ChatColor.GRAY + " config!"));\n\n                meta.setLore(lore);', '                meta.lore(CargoConfiguratorLore.copied(lore, defaultLore,\n                        clickedItemStack.getItemMeta().displayName()));'),
    ('import java.util.ArrayList;\n', ''),
)
for before, after in replacements:
    assert text.count(before) == 1, before
    text = text.replace(before, after)
text = text.replace('@Nonnull List<String> defaultLore', '@Nonnull List<Component> defaultLore').replace('@Nonnull List<String> lore', '@Nonnull List<Component> lore')
text, count = re.subn(r'sendMessage\(ChatColor\.(RED|GREEN) \+ ("[^"\n]*")\);', r'sendMessage(Component.text(\2, NamedTextColor.\1));', text)
assert count == 7
marker = '    /**\n     * Returns a deterministic'
assert original[original.index(marker):] == text[text.index(marker):]
assert blob(text.encode()) == '4433b4dbb53327e5b81f8bcc9bef17e81c53277b'
path.write_text(text)
pom = Path('pom.xml')
assert blob(pom.read_bytes()) == '4fcebb212b4ea979ffa065112ea2be9ce2353e47'
text = pom.read_text().replace('    </dependencies>', '        <dependency><groupId>junit</groupId><artifactId>junit</artifactId><version>4.13.2</version><scope>test</scope></dependency>\n    </dependencies>', 1)
text = text.replace('        <plugins>', '        <plugins>\n            <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-surefire-plugin</artifactId><version>3.5.4</version></plugin>', 1)
assert blob(text.encode()) == 'c7dd239d88f203088cb433cd8cdd6fab6b7e8129'
pom.write_text(text)
print('Source patch verified; decoder, fingerprint, item-local migration and persistent keys unchanged.')
