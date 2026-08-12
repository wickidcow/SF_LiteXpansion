<div align="center">

# ⚡🏗️ LiteXpansion — Slimefun Legacy

**IndustrialCraft-inspired machines, energy, tools, materials, and UU Matter for Slimefun.**

![Slimefun Legacy](https://img.shields.io/badge/Slimefun-Legacy-6bd425?style=for-the-badge)
![Paper 26.2+](https://img.shields.io/badge/Paper-26.2%2B-blue?style=for-the-badge)
![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue?style=for-the-badge)
![Maintained for AlbionMC.com](https://img.shields.io/badge/Maintained%20for-albionmc.com-7b68ee?style=for-the-badge)

</div>

> [!IMPORTANT]
> LiteXpansion Legacy is an **unofficial community maintenance fork** developed for **Slimefun Legacy** and for use on **albionmc.com**. Its purpose is to preserve LiteXpansion's gameplay and compatibility while carrying it forward to modern servers.

## ⚙️ What does LiteXpansion do?

LiteXpansion brings an **IndustrialCraft-inspired technology progression** into Slimefun. It adds machines, power-dependent processing, advanced materials, electric equipment, Cargo-compatible systems, and configurable **UU Matter** recipes.

The maintenance fork preserves the original gameplay, items, machines, recipes, research IDs, Slimefun IDs, and established configuration formats wherever practical.

## 🧪 Slimefun Legacy target

- **Minecraft:** 1.21.11+
- **Primary server target:** Paper 26.2+
- **Primary Slimefun runtime:** Slimefun Legacy
- **Modern build runtime:** Java 25
- **Expected artifact style:** `SF_LiteXpansion_Legacy_v1.x.x.jar`

### Legacy maintenance highlights

- compiles against the current Slimefun Legacy release API;
- preserves production-proven storage-cache handling for Cargo Configurator, Glass Cutter, Mining Drill, and Slimefun-block safety checks;
- preserves established Cargo Configurator JSON data while accepting the short-lived `LX2` format as an upgrade fallback;
- removes the obsolete custom glow-enchantment registration in favor of modern item-meta glint handling;
- updates modern Paper attribute handling and player UUID lookup;
- updates thorium GEO data for modern biomes;
- removes obsolete pre-1.19 compatibility branches from this modern-only maintenance line;
- removes obsolete metrics/Lombok build baggage;
- keeps passive electric-item inventory scans on the safe server thread;
- disables the historical external updater;
- makes cross-addon generator nerfing **opt-in** instead of silently changing other addons.

## 🧬 Maintenance sources reviewed

Useful compatibility work has been reviewed or selectively adapted from multiple public branches rather than copying one fork wholesale:

- **Slimefun-Addon-Community/LiteXpansion** — primary classic community baseline and immediate upstream for this repository.
- **SlimefunGuguProject/LiteXpansion** — later maintenance ideas, including glow/update/default behavior changes.
- **AquaVille/LiteXpansion** — modern Minecraft/Paper compatibility work.
- **J3fftw1/LiteXpansion** — original project lineage and gameplay foundation.
- production-tested historical builds — used as compatibility references for storage and Cargo behavior.

Gugu-only API assumptions are not required by the maintained Slimefun Legacy build.

## 🧪 UU Matter configuration

UU Matter recipes are configured in `plugins/LiteXpansion/uumatter.yml`. Outputs may be Bukkit materials or Slimefun item IDs, with optional amounts.

Example:

```yaml
recipes:
  'COAL:20':
    - '  x'
    - 'x  '
    - '  x'
```

Recommended safety defaults remain:

```yaml
options:
  auto-update: false
  nerf-other-addons: false
```

## ❤️ Credits & project lineage

- **J3fftw1** — original creator of **LiteXpansion** and its IndustrialCraft-inspired Slimefun gameplay.
- **Slimefun-Addon-Community/LiteXpansion** — community-maintained upstream and the immediate source of this fork.
- **SlimefunGuguProject/LiteXpansion** — later maintenance and compatibility work reviewed by this fork.
- **AquaVille/LiteXpansion** — modern compatibility work reviewed and selectively adapted.
- **LiteXpansion and Slimefun contributors** — fixes, testing, APIs, and community maintenance over the project's lifetime.
- **wickidcow / Slimefun Legacy** — current compatibility and preservation work for modern servers and albionmc.com.

All credit for the original design and gameplay remains with the original developers and contributors. This maintenance fork is intended to preserve that work.

## 📜 GNU General Public License v3.0

LiteXpansion is licensed under the **GNU General Public License v3.0 (GPLv3)**. See `LICENSE` for the complete terms.

If you distribute LiteXpansion or a modified GPL-covered version, comply with GPLv3, including preserving applicable notices, identifying modified versions, licensing covered modified source under GPLv3, and making the required Corresponding Source available when distributing object code.

The software is provided **without warranty** as described by GPLv3.

## ⚖️ Independence & trademark notice

**NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT.**

LiteXpansion, Slimefun Legacy, and this maintenance fork are independent community projects. They are not sponsored, endorsed, approved, or operated by Mojang Studios or Microsoft. Minecraft-related names, brands, and assets remain the property of their respective rights holders.

This fork is also not represented as an official release of J3fftw1, the Slimefun-Addon-Community, SlimefunGuguProject, AquaVille, or the original Slimefun developers unless explicitly stated by those parties.

---

<div align="center">

**⚡ Industrial progression, Slimefun style. Keep LiteXpansion alive. 🏗️**

</div>
