package dev.j3fftw.litexpansion.items;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import dev.j3fftw.litexpansion.Items;
import dev.j3fftw.litexpansion.LiteXpansion;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CargoConfigurator extends SimpleSlimefunItem<ItemUseHandler> implements Listener {

    private static final Gson GSON = new Gson();
    private static final Type CONFIG_TYPE = new TypeToken<Map<String, String>>() {}.getType();
    private static final NamespacedKey CARGO_BLOCK = new NamespacedKey(LiteXpansion.getInstance(), "cargo_block");
    private static final NamespacedKey CARGO_CONFIG = new NamespacedKey(LiteXpansion.getInstance(), "cargo_config");
    private static final String LEGACY_PROPERTIES_FORMAT = "LX2\n";

    public CargoConfigurator() {
        super(Items.LITEXPANSION, Items.CARGO_CONFIGURATOR, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
            Items.REFINED_IRON, SlimefunItems.REINFORCED_PLATE, Items.REFINED_IRON,
            SlimefunItems.REINFORCED_PLATE, SlimefunItems.CARGO_MANAGER, SlimefunItems.REINFORCED_PLATE,
            Items.REFINED_IRON, SlimefunItems.REINFORCED_PLATE, Items.REFINED_IRON
        });

        Bukkit.getPluginManager().registerEvents(this, LiteXpansion.getInstance());
    }

    @Nonnull
    @Override
    public ItemUseHandler getItemHandler() {
        return e -> e.setUseBlock(Event.Result.DENY);
    }

    private boolean canUseCargoConfigurator(@Nonnull Player p, @Nonnull Block clicked) {
        return Slimefun.getProtectionManager().hasPermission(p, clicked, Interaction.INTERACT_BLOCK);
    }

    @EventHandler
    public void onCargoConfiguratorItemClick(PlayerInteractEvent e) {
        if (e.getItem() == null || e.getMaterial() != Material.COMPASS) {
            return;
        }

        final ItemStack clickedItem = e.getItem();
        final SlimefunItem configurator = SlimefunItem.getByItem(Items.CARGO_CONFIGURATOR);
        if (!this.isItem(clickedItem) || configurator == null || configurator.isDisabled()) {
            return;
        }

        final ItemMeta meta = clickedItem.getItemMeta();
        final List<String> defaultLore = Items.CARGO_CONFIGURATOR.getItemMetaSnapshot().getLore()
            .orElse(new ArrayList<>());
        final List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>(defaultLore);

        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
            && e.getPlayer().isSneaking()) {
            clearConfig(e.getPlayer(), clickedItem, meta, defaultLore, lore);
            e.setCancelled(true);
            return;
        }

        if ((e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_BLOCK)
            || e.getClickedBlock() == null) {
            return;
        }

        final Block clickedBlock = e.getClickedBlock();
        final SlimefunItem block = StorageCacheUtils.getSlimefunItem(clickedBlock.getLocation());
        if (block == null) {
            return;
        }

        final ItemStack clickedItemStack = block.getItem();
        final String blockId = block.getId();
        if (!blockId.equals(SlimefunItems.CARGO_INPUT_NODE.getItemId())
            && !blockId.equals(SlimefunItems.CARGO_OUTPUT_NODE.getItemId())
            && !blockId.equals(SlimefunItems.CARGO_OUTPUT_NODE_2.getItemId())) {
            return;
        }

        final Player p = e.getPlayer();
        if (!canUseCargoConfigurator(p, clickedBlock) && !p.hasPermission("slimefun.cargo.bypass")) {
            Slimefun.getLocalization().sendMessage(p, "inventory.no-access", true);
            return;
        }

        e.setCancelled(true);
        runActions(e, clickedItem, clickedItemStack, meta, blockId, lore, defaultLore);
    }

    private void clearConfig(@Nonnull Player player, @Nonnull ItemStack itemStack, @Nonnull ItemMeta meta,
                             @Nonnull List<String> defaultLore, @Nonnull List<String> lore) {
        PersistentDataAPI.remove(meta, CARGO_BLOCK);
        PersistentDataAPI.remove(meta, CARGO_CONFIG);
        player.sendMessage(ChatColor.RED + "Cleared node configuration!");

        if (lore.size() != defaultLore.size()) {
            lore.clear();
            lore.addAll(defaultLore);
        }

        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }

    private void runActions(@Nonnull PlayerInteractEvent e, @Nonnull ItemStack configuratorItem,
                            @Nonnull ItemStack clickedItemStack, @Nonnull ItemMeta meta,
                            @Nonnull String blockId, @Nonnull List<String> lore,
                            @Nonnull List<String> defaultLore) {
        final Block clickedBlock = e.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        final SlimefunBlockData blockData = StorageCacheUtils.getBlock(clickedBlock.getLocation());
        if (blockData == null) {
            e.getPlayer().sendMessage(ChatColor.RED + "Could not read this cargo node's data.");
            return;
        }

        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            final String copiedBlock = PersistentDataAPI.getString(meta, CARGO_BLOCK);
            final String config = PersistentDataAPI.getString(meta, CARGO_CONFIG);
            if (copiedBlock == null || config == null) {
                e.getPlayer().sendMessage(ChatColor.RED + "You do not have a config copied!");
                return;
            }

            if (!copiedBlock.equals(blockId)) {
                e.getPlayer().sendMessage(ChatColor.RED + "You can't apply the config to this node!");
                return;
            }

            StorageCacheUtils.executeAfterLoad(blockData, () -> {
                final Map<String, String> values = decodeConfig(config);
                if (values == null) {
                    e.getPlayer().sendMessage(ChatColor.RED + "The copied cargo configuration is invalid. Copy the node again first.");
                    return;
                }

                values.forEach(blockData::setData);
                e.getPlayer().sendMessage(ChatColor.GREEN + "Applied configuration!");
            }, true);
        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            StorageCacheUtils.executeAfterLoad(blockData, () -> {
                PersistentDataAPI.setString(meta, CARGO_BLOCK, blockId);
                PersistentDataAPI.setString(meta, CARGO_CONFIG, GSON.toJson(blockData.getAllData()));

                if (lore.size() == defaultLore.size() + 2) {
                    lore.clear();
                    lore.addAll(defaultLore);
                }
                lore.addAll(Arrays.asList("", ChatColor.GRAY + "> Copied "
                    + ChatColor.RESET + clickedItemStack.getItemMeta().getDisplayName()
                    + ChatColor.GRAY + " config!"
                ));

                meta.setLore(lore);
                configuratorItem.setItemMeta(meta);
                e.getPlayer().sendMessage(ChatColor.GREEN + "Copied node configuration!");
            }, true);
        }
    }

    /**
     * Build35 stored cargo data as JSON. Keep that format as the canonical output
     * so existing Albion configurators remain compatible. The LX2 Properties format
     * from the first Legacy test build is accepted as an upgrade fallback.
     */
    private Map<String, String> decodeConfig(@Nonnull String serialized) {
        if (serialized.startsWith(LEGACY_PROPERTIES_FORMAT)) {
            final Properties properties = new Properties();
            try (StringReader reader = new StringReader(serialized.substring(LEGACY_PROPERTIES_FORMAT.length()))) {
                properties.load(reader);
            } catch (IOException | IllegalArgumentException ex) {
                LiteXpansion.getInstance().getLogger().warning("Could not read copied cargo configuration: " + ex.getMessage());
                return null;
            }

            final Map<String, String> values = new java.util.HashMap<>();
            for (String key : properties.stringPropertyNames()) {
                values.put(key, properties.getProperty(key));
            }
            return values;
        }

        try {
            return GSON.fromJson(serialized, CONFIG_TYPE);
        } catch (JsonSyntaxException | IllegalStateException ex) {
            LiteXpansion.getInstance().getLogger().warning("Could not read copied cargo configuration: " + ex.getMessage());
            return null;
        }
    }
}
