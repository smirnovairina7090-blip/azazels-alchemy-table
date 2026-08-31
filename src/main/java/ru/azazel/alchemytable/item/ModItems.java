package ru.azazel.alchemytable.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import ru.azazel.alchemytable.AzazelSAlchemyTable;

public final class ModItems {

    public static final Item MAGIC_WAND = Registry.register(
            BuiltInRegistries.ITEM,
            AzazelSAlchemyTable.id("magic_wand"),
            new Item(new Item.Properties().stacksTo(1))
    );

    public static void registerModItems() {
        ItemGroupEvents.modifyEntriesEvent(
                CreativeModeTabs.TOOLS_AND_UTILITIES
        ).register(entries -> entries.accept(MAGIC_WAND));
    }

    private ModItems() {
    }
}
