package dev.j3fftw.litexpansion.resources;

import dev.j3fftw.litexpansion.Items;
import dev.j3fftw.litexpansion.LiteXpansion;
import io.github.thebusybiscuit.slimefun4.api.geo.GEOResource;
import io.github.thebusybiscuit.slimefun4.utils.biomes.BiomeMap;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class ThoriumResource implements GEOResource {

    private final NamespacedKey key = new NamespacedKey(LiteXpansion.getInstance(), "thorium");

    private BiomeMap<Integer> map;

    public ThoriumResource() {
        final LiteXpansion instance = LiteXpansion.getInstance();

        try {
            map = BiomeMap.getIntMapFromResource(key, instance, "/biome-maps/thorium_v1.21.json");
        } catch (Exception e) {
            instance.getLogger().log(Level.SEVERE, "Failed to load the Minecraft 1.21 thorium biome map!", e);
        }
    }

    @Override
    public int getDefaultSupply(@Nonnull World.Environment environment, Biome biome) {
        return map == null ? 1 : map.getOrDefault(biome, 1);
    }

    @Nonnull
    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public int getMaxDeviation() {
        return 1;
    }

    @Nonnull
    @Override
    public String getName() {
        return "Thorium";
    }

    @Nonnull
    @Override
    public ItemStack getItem() {
        return Items.THORIUM.clone();
    }

    @Override
    public boolean isObtainableFromGEOMiner() {
        return true;
    }
}
