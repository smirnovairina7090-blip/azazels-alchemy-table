package ru.azazel.alchemytable.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import ru.azazel.alchemytable.AzazelSAlchemyTable;
import ru.azazel.alchemytable.block.ModBlocks;

public final class ModBlockEntities {

    public static final BlockEntityType<AlchemyTableBlockEntity>
            ALCHEMY_TABLE_BLOCK_ENTITY = Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(
                            AzazelSAlchemyTable.MOD_ID,
                            "alchemy_table"
                    ),
                    FabricBlockEntityTypeBuilder.create(
                            AlchemyTableBlockEntity::new,
                            ModBlocks.ALCHEMY_TABLE
                    ).build()
            );

    public static void registerModBlockEntities() {
    }

    private ModBlockEntities() {
    }
}
