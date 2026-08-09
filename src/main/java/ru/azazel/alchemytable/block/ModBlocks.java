package ru.azazel.alchemytable.block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import ru.azazel.alchemytable.AzazelSAlchemyTable;

public final class ModBlocks {

    public static final Block ALCHEMY_TABLE = registerBlock(
            "alchemy_table",
            new AlchemyTableBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.5f, 6.0f)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
            )
    );

    private static Block registerBlock(String name, Block block) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                AzazelSAlchemyTable.MOD_ID,
                name
        );

        Block registeredBlock = Registry.register(
                BuiltInRegistries.BLOCK,
                id,
                block
        );

        Registry.register(
                BuiltInRegistries.ITEM,
                id,
                new BlockItem(registeredBlock, new Item.Properties())
        );

        return registeredBlock;
    }

    public static void registerModBlocks() {
        ItemGroupEvents.modifyEntriesEvent(
                CreativeModeTabs.FUNCTIONAL_BLOCKS
        ).register(entries -> entries.accept(ALCHEMY_TABLE));
    }

    private ModBlocks() {
    }
}
