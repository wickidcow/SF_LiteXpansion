#!/usr/bin/env python3
"""Verify LiteXpansion Cargo Configurator Doctor migration stays item-local and exact."""

from pathlib import Path
import sys

ROOT = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
ERRORS: list[str] = []


def read(path: str) -> str:
    file = ROOT / path
    if not file.is_file():
        ERRORS.append(f"missing required file: {path}")
        return ""
    return file.read_text(encoding="utf-8")


def require(value: bool, message: str) -> None:
    if not value:
        ERRORS.append(message)


def reject(value: bool, message: str) -> None:
    if value:
        ERRORS.append(message)


cargo = read("src/main/java/dev/j3fftw/litexpansion/items/CargoConfigurator.java")
bridge = read("src/main/java/dev/j3fftw/litexpansion/diagnostics/LegacyDoctorBridge.java")
plugin = read("src/main/java/dev/j3fftw/litexpansion/LiteXpansion.java")

require('LEGACY_PROPERTIES_FORMAT = "LX2\\n"' in cargo,
        "historical LX2 Properties format marker must remain stable")
require('"cargo_config"' in cargo and '"cargo_block"' in cargo,
        "Cargo Configurator PDC keys must remain stable")
require('legacyMigrationClaim(@Nonnull ItemStack item)' in cargo,
        "item-local legacy claim helper is missing")
require('MessageDigest.getInstance("SHA-256")' in cargo,
        "legacy cargo claim must use SHA-256")
require('serialized.startsWith(LEGACY_PROPERTIES_FORMAT)' in cargo,
        "only the known LX2 Properties format may become a migration candidate")
require('decodeConfig(serialized) == null' in cargo,
        "invalid legacy Properties payloads must fail closed without an executable claim")
require('migrateLegacyConfig(@Nonnull ItemStack item, @Nonnull String expectedClaim)' in cargo,
        "item-local migration entry point is missing")
require('!sha256(serialized).equals(expectedClaim)' in cargo,
        "live migration must reproduce the exact fingerprinted legacy payload claim")
require('PersistentDataAPI.setString(meta, CARGO_CONFIG, GSON.toJson(values))' in cargo,
        "migration must replace only cargo_config with canonical JSON")
reject('StorageCacheUtils' in cargo[cargo.find('public static boolean migrateLegacyConfig'):],
       "item-local Cargo Configurator migration must not access block persistence")
reject('Player' in cargo[cargo.find('public static boolean migrateLegacyConfig'):cargo.find('private static Map<String, String> decodeConfig')],
       "item-local migration must not mutate player state")

require('"io.github.thebusybiscuit.slimefun4.api.diagnostics.LegacyItemSchemaProbe"' in bridge,
        "reflective schema probe binding is missing")
require('"io.github.thebusybiscuit.slimefun4.api.diagnostics.LegacyItemSchemaMigrator"' in bridge,
        "reflective schema migrator binding is missing")
require('ITEM_ID = "CARGO_CONFIGURATOR"' in bridge,
        "schema migration must remain scoped to the stable Cargo Configurator ID")
require('CANDIDATE_TYPE = "legacy-cargo-config-properties"' in bridge,
        "Cargo Configurator candidate type must remain stable")
require('readinessValueOf.invoke(null, "READY")' in bridge,
        "self-contained Cargo Configurator migration must remain READY/item-local")
require('candidateConstructor.newInstance(CANDIDATE_TYPE, readiness, detail, claim)' in bridge,
        "READY probe must pass the deterministic private claim on capable Legacy cores")
require('CargoConfigurator.migrateLegacyConfig(item, claim)' in bridge,
        "schema migrator must delegate only the exact live item-local conversion")
require('arguments.length < 5' in bridge,
        "reflective migrator must retain the five-argument Legacy contract")
require('Bukkit.getServicesManager().unregisterAll(plugin)' in bridge,
        "optional Doctor services must unregister cleanly")
reject('LegacyItemSchemaValidator' in bridge,
       "item-local READY migration must not add a fake external validator")

require('LegacyDoctorBridge.register(this)' in plugin,
        "LiteXpansion must register the optional Doctor schema bridge")
require('LegacyDoctorBridge.unregister(this)' in plugin,
        "LiteXpansion must unregister Doctor services on disable")

if ERRORS:
    print("LiteXpansion Doctor Cargo Configurator verification failed:")
    for error in ERRORS:
        print(" -", error)
    raise SystemExit(1)

print("LiteXpansion Doctor Cargo Configurator verification passed.")
