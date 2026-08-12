# LiteXpansion Legacy

LiteXpansion is an IndustrialCraft-inspired Slimefun addon originally created by **J3fftw1** and maintained over the years by the Slimefun addon community. This fork keeps the original gameplay, items, machines, recipes, research IDs, and Slimefun IDs intact while updating the addon for the modern **Slimefun Legacy** stack.

## Target platform

- **Minecraft:** 1.21.11+
- **Primary server target:** Paper 26.2+
- **Java:** 25
- **Primary Slimefun runtime:** `wickidcow/Slimefun-Legacy`
- **Build artifact:** `SF_LiteXpansion_Legacy_v1.0.0.jar`

The GitHub Actions workflow compiles against the actual Slimefun Legacy release JAR, then exposes the finished LiteXpansion JAR directly instead of wrapping the downloadable artifact in another archive.

## Modernization sources

This fork deliberately uses the original LiteXpansion code as its compatibility base and selectively backports useful maintenance from the wider fork family rather than replacing it wholesale with a Gugu-specific implementation.

Useful work reviewed or incorporated from:

- **SlimefunGuguProject/LiteXpansion** — removal of the obsolete custom glow enchant and the safer `nerf-other-addons: false` default.
- **AquaVille/LiteXpansion** — Minecraft 1.21 modernization, current item glint handling, UUID lookup updates, modern thorium biome data, newer Paper attribute handling, removal of old metrics/build baggage, and removal of obsolete pre-1.19 paths.
- **Slimefun-Addon-Community/LiteXpansion** — the established gameplay/API baseline used by this Legacy fork.
- Other public LiteXpansion forks were reviewed for fixes; changes that relied on unsafe material-only item conversion or could conflict with other Slimefun addons were intentionally not imported.

Gugu-only API assumptions such as the newer `SlimefunItemStack.item()` accessor are intentionally not required. This fork remains compatible with Slimefun Legacy's established item model.

## Legacy / Paper modernization

Key changes in this fork include:

- Java 25 build and Paper 26.2 API target while retaining a `plugin.yml` API floor of 1.21.11.
- Actual CI compilation against the current Slimefun Legacy release JAR.
- Removed the obsolete custom `GlowEnchant` registration and replaced visual glints with modern item-meta glint overrides.
- Updated Nano Blade attack-damage modifiers for the modern Paper attribute API while preserving its on/off behavior.
- Ported Cargo Configurator copy/apply behavior away from removed legacy `BlockStorage` JSON/reload methods to Slimefun Legacy's current key/value storage model.
- Updated thorium GEO data to the modern Minecraft 1.21 biome map.
- Updated player lookup to UUID-based access.
- Removed old pre-1.19 generator compatibility branches; this fork only targets 1.21.11+.
- Removed the obsolete metrics service and Lombok build dependency.
- Moved the passive electric-item inventory scan back to the safe server thread instead of reading Bukkit player inventories asynchronously.
- Disabled the historical external Blob auto-updater so it cannot overwrite the Legacy build.
- Changed cross-addon generator nerfing to **opt-in** instead of silently modifying power output in Slimefun or other addons.

## Configuration

### UU Matter

UU Matter recipes are configured in:

`/plugins/LiteXpansion/uumatter.yml`

Under `recipes`, the output can be a Bukkit material or a Slimefun item ID. Add `:<amount>` when more than one output item is required. Each recipe is a three-line pattern where `x` represents UU Matter and a space represents an empty slot.

```yaml
recipes:
  'COAL:20':
    - '  x'
    - 'x  '
    - '  x'
```

### Legacy safety defaults

`/plugins/LiteXpansion/config.yml` uses:

```yaml
options:
  auto-update: false
  nerf-other-addons: false
```

- `auto-update: false` prevents this Legacy fork from replacing itself with an unrelated upstream build.
- `nerf-other-addons: false` prevents LiteXpansion from silently modifying generator output in Slimefun, Infinity Expansion, Supreme, or other addons. Servers that explicitly want the historical rebalance behavior can enable it manually.

## Building

Run the **Build LiteXpansion Legacy** GitHub Actions workflow. A successful run outputs:

`SF_LiteXpansion_Legacy_v1.0.0.jar`

The workflow validates the Java/Paper/Slimefun Legacy contract before compiling and packaging the addon.

## Credits

All credit for LiteXpansion's original design and gameplay belongs to its original author and community maintainers. This fork exists to preserve that work and keep it usable on current Minecraft/Paper releases within the Slimefun Legacy ecosystem.

Upstream/reference projects:

- J3fftw1/LiteXpansion
- Slimefun-Addon-Community/LiteXpansion
- SlimefunGuguProject/LiteXpansion
- AquaVille/LiteXpansion
