package dev.j3fftw.litexpansion.items;

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
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

public class CargoConfigurator extends SimpleSlimefunItem<ItemUseHandler> implements Listener {

    private static final NamespacedKey CARGO_BLOCK = new NamespacedKey(LiteXpansion.getInstance(), "cargo_block");
    private static final NamespacedKey CARGO_CONFIG = new NamespacedKey(LiteXpansion.getInstance(), "cargo_config");
    private static final String CONFIG_FORMAT = "LX2\n";

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
        final List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>(defaultLore);

        // Clear the config and lore.
        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
            && e.getPlayer().isSneaking()
        ) {
            clearConfig(e.getPlayer(), clickedItem, meta, defaultLore, lore);
            e.setCancelled(true);
            return;
        }

        if ((e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_BLOCK)
            || e.getClickedBlock() == null) {
            return;
        }

        final SlimefunItem block = BlockStorage.check(e.getClickedBlock());
        if (block == null) {
            return;
        }

        final ItemStack clickedItemStack = block.getItem();
        final String blockId = block.getId();
        if (!blockId.equals(SlimefunItems.CARGO_INPUT_NODE.getItemId())
            && !blockId.equals(SlimefunItems.CARGO_OUTPUT_NODE.getItemId())
            && !blockId.equals(SlimefunItems.CARGO_OUTPUT_NODE_2.getItemId())
        ) {
            return;
        }

        final Player p = e.getPlayer();
        if (!canUseCargoConfigurator(p, e.getClickedBlock()) && !p.hasPermission("slimefun.cargo.bypass")) {
            Slimefun.getLocalization().sendMessage(p, "inventory.no-access", true);
            return;
        }

        e.setCancelled(true);
        runActions(e, clickedItemStack, meta, blockId, lore, defaultLore);

        meta.setLore(lore);
        clickedItem.setItemMeta(meta);
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

    private void runActions(@Nonnull PlayerInteractEvent e, @Nonnull ItemStack clickedItemStack, @Nonnull ItemMeta meta,
                            @Nonnull String blockId, @Nonnull List<String> lore, @Nonnull List<String> defaultLore) {
        final Block clickedBlock = e.getClickedBlock();
        if (clickedBlock == null) {
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

            if (!applyBlockConfig(clickedBlock, config)) {
                e.getPlayer().sendMessage(ChatColor.RED + "That copied configuration is from an older LiteXpansion build. Copy the node again first.");
                return;
            }

            e.getPlayer().sendMessage(ChatColor.GREEN + "Applied configuration!");
        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            PersistentDataAPI.setString(meta, CARGO_BLOCK, blockId);
            PersistentDataAPI.setString(meta, CARGO_CONFIG, serializeBlockConfig(clickedBlock));

            if (lore.size() == defaultLore.size() + 2) {
                lore.clear();
                lore.addAll(defaultLore);
            }
            lore.addAll(Arrays.asList("", ChatColor.GRAY + "> Copied "
                + ChatColor.RESET + clickedItemStack.getItemMeta().getDisplayName()
                + ChatColor.GRAY + " config!"
            ));
            e.getPlayer().sendMessage(ChatColor.GREEN + "Copied node configuration!");
        }
    }

    /**
     * Slimefun Legacy no longer exposes the old BlockStorage JSON helpers. Store
     * the node's key/value data in a Java Properties payload instead. Properties
     * escaping preserves arbitrary values while keeping the configurator data as
     * one PersistentDataContainer string on the item.
     */
    @Nonnull
    private String serializeBlockConfig(@Nonnull Block block) {
        final Config data = BlockStorage.getLocationInfo(block.getLocation());
        final Properties properties = new Properties();

        for (String key : data.getKeys()) {
            final String value = data.getString(key);
            if (value != null) {
                properties.setProperty(key, value);
            }
        }

        try (StringWriter writer = new StringWriter()) {
            properties.store(writer, null);
            return CONFIG_FORMAT + writer;
        } catch (IOException ex) {
            throw new IllegalStateException("Could not serialize cargo node configuration", ex);
        }
    }

    private boolean applyBlockConfig(@Nonnull Block block, @Nonnull String serialized) {
        if (!serialized.startsWith(CONFIG_FORMAT)) {
            return false;
        }

        final Properties properties = new Properties();
        try (StringReader reader = new StringReader(serialized.substring(CONFIG_FORMAT.length()))) {
            properties.load(reader);
        } catch (IOException | IllegalArgumentException ex) {
            LiteXpansion.getInstance().getLogger().warning("Could not read a copied cargo node configuration: " + ex.getMessage());
            return false;
        }

        final Config target = BlockStorage.getLocationInfo(block.getLocation());

        // Replace, rather than merge, the copied configuration just like the old
        // BlockStorage.setBlockInfo(...) implementation did.
        for (String key : new HashSet<>(target.getKeys())) {
            target.setValue(key, null);
        }
        for (String key : properties.stringPropertyNames()) {
            target.setValue(key, properties.getProperty(key));
        }

        // A currently-open cargo menu was built from the previous configuration.
        // Close it so Slimefun rebuilds its visual state the next time it is opened.
        final var menu = BlockStorage.getInventory(block);
        if (menu != null && menu.toInventory() != null) {
            for (HumanEntity viewer : new ArrayList<>(menu.toInventory().getViewers())) {
                viewer.closeInventory();
            }
        }

        return true;
    }
}
