package dev.j3fftw.litexpansion.diagnostics;

import dev.j3fftw.litexpansion.LiteXpansion;
import dev.j3fftw.litexpansion.items.CargoConfigurator;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

/** Reflective bridge to optional Slimefun Legacy same-ID schema migration APIs. */
public final class LegacyDoctorBridge {

    private static final String SCHEMA_PROBE_API =
            "io.github.thebusybiscuit.slimefun4.api.diagnostics.LegacyItemSchemaProbe";
    private static final String SCHEMA_CANDIDATE_API =
            "io.github.thebusybiscuit.slimefun4.api.diagnostics.LegacyItemSchemaCandidate";
    private static final String SCHEMA_READINESS_API =
            "io.github.thebusybiscuit.slimefun4.api.diagnostics.LegacyItemSchemaCandidate$Readiness";
    private static final String SCHEMA_MIGRATOR_API =
            "io.github.thebusybiscuit.slimefun4.api.diagnostics.LegacyItemSchemaMigrator";
    private static final String ITEM_ID = "CARGO_CONFIGURATOR";
    private static final String CANDIDATE_TYPE = "legacy-cargo-config-properties";

    private LegacyDoctorBridge() {}

    public static void register(LiteXpansion plugin) {
        Plugin slimefun = Bukkit.getPluginManager().getPlugin("Slimefun");
        if (slimefun == null) return;
        ClassLoader loader = slimefun.getClass().getClassLoader();
        registerSchemaProbe(plugin, loader);
        registerSchemaMigrator(plugin, loader);
    }

    private static void registerSchemaProbe(LiteXpansion plugin, ClassLoader loader) {
        try {
            Class<?> probeInterface = Class.forName(SCHEMA_PROBE_API, false, loader);
            Class<?> candidateClass = Class.forName(SCHEMA_CANDIDATE_API, false, loader);
            Class<?> readinessClass = Class.forName(SCHEMA_READINESS_API, false, loader);
            Constructor<?> candidateConstructor;
            boolean supportsPrivateClaim;
            try {
                candidateConstructor = candidateClass.getConstructor(
                        String.class, readinessClass, String.class, String.class);
                supportsPrivateClaim = true;
            } catch (NoSuchMethodException ignored) {
                candidateConstructor = candidateClass.getConstructor(String.class, readinessClass, String.class);
                supportsPrivateClaim = false;
            }
            Method readinessValueOf = readinessClass.getMethod("valueOf", String.class);
            Constructor<?> finalConstructor = candidateConstructor;
            boolean finalSupportsClaim = supportsPrivateClaim;
            InvocationHandler handler = (proxy, method, arguments) -> invokeSchemaProbe(
                    proxy, method, arguments, finalConstructor, readinessValueOf, finalSupportsClaim);
            Object provider = Proxy.newProxyInstance(loader, new Class<?>[] {probeInterface}, handler);
            registerRaw(Bukkit.getServicesManager(), probeInterface, provider, plugin);
            plugin.getLogger().info("Registered LiteXpansion Cargo Configurator schema probe with Slimefun Doctor.");
        } catch (ClassNotFoundException ignored) {
            // Optional API is not present on older/non-Legacy Slimefun builds.
        } catch (ReflectiveOperationException | RuntimeException exception) {
            plugin.getLogger().log(Level.WARNING, "Could not register the optional schema probe.", exception);
        }
    }

    private static void registerSchemaMigrator(LiteXpansion plugin, ClassLoader loader) {
        try {
            Class<?> migratorInterface = Class.forName(SCHEMA_MIGRATOR_API, false, loader);
            Object provider = Proxy.newProxyInstance(
                    loader, new Class<?>[] {migratorInterface}, (proxy, method, arguments) ->
                            invokeSchemaMigrator(proxy, method, arguments));
            registerRaw(Bukkit.getServicesManager(), migratorInterface, provider, plugin);
            plugin.getLogger().info("Registered LiteXpansion Cargo Configurator schema migrator with Slimefun Doctor.");
        } catch (ClassNotFoundException ignored) {
            // Mutation API is optional and only available on newer Slimefun Legacy builds.
        } catch (RuntimeException exception) {
            plugin.getLogger().log(Level.WARNING, "Could not register the optional schema migrator.", exception);
        }
    }

    public static void unregister(LiteXpansion plugin) {
        Bukkit.getServicesManager().unregisterAll(plugin);
    }

    private static Object invokeSchemaProbe(
            Object proxy,
            Method method,
            Object[] arguments,
            Constructor<?> candidateConstructor,
            Method readinessValueOf,
            boolean supportsPrivateClaim)
            throws ReflectiveOperationException {
        return switch (method.getName()) {
            case "getMigrationName" -> "LiteXpansion Cargo Configurator migration";
            case "getSupportedItemIds" -> Set.of(ITEM_ID);
            case "probeItem" -> {
                if (arguments == null
                        || arguments.length < 2
                        || !(arguments[0] instanceof ItemStack item)
                        || !ITEM_ID.equals(arguments[1])) {
                    yield null;
                }
                String claim = CargoConfigurator.legacyMigrationClaim(item);
                if (claim == null) yield null;
                Object readiness = readinessValueOf.invoke(null, "READY");
                String detail = "Legacy LX2 Properties cargo configuration can be converted to canonical JSON.";
                if (supportsPrivateClaim) {
                    yield candidateConstructor.newInstance(CANDIDATE_TYPE, readiness, detail, claim);
                }
                yield candidateConstructor.newInstance(CANDIDATE_TYPE, readiness,
                        detail + " This Slimefun Legacy build can diagnose it but cannot fingerprint READY migration.");
            }
            case "toString" -> "LiteXpansionCargoConfiguratorSchemaProbe";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> arguments != null && arguments.length == 1 && arguments[0] == proxy;
            default -> throw new UnsupportedOperationException(
                    "Unsupported LegacyItemSchemaProbe method: " + method.getName());
        };
    }

    private static Object invokeSchemaMigrator(Object proxy, Method method, Object[] arguments) {
        return switch (method.getName()) {
            case "getSupportedCandidateTypes" -> Set.of(CANDIDATE_TYPE);
            case "migrateItem" -> {
                if (arguments == null
                        || arguments.length < 5
                        || !(arguments[0] instanceof ItemStack item)
                        || !ITEM_ID.equals(arguments[1])
                        || !CANDIDATE_TYPE.equals(arguments[2])
                        || !(arguments[3] instanceof String claim)
                        || !(arguments[4] instanceof String)) {
                    yield false;
                }
                yield CargoConfigurator.migrateLegacyConfig(item, claim);
            }
            case "toString" -> "LiteXpansionCargoConfiguratorSchemaMigrator";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> arguments != null && arguments.length == 1 && arguments[0] == proxy;
            default -> throw new UnsupportedOperationException(
                    "Unsupported LegacyItemSchemaMigrator method: " + method.getName());
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerRaw(ServicesManager services, Class service, Object provider, LiteXpansion plugin) {
        services.register(service, provider, plugin, ServicePriority.Normal);
    }
}
