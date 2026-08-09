package ru.azazel.alchemytable;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.azazel.alchemytable.block.ModBlocks;
import ru.azazel.alchemytable.block.entity.ModBlockEntities;
import ru.azazel.alchemytable.menu.ModMenuTypes;

public class AzazelSAlchemyTable implements ModInitializer {

    public static final String MOD_ID = "azazels-alchemy-table";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModBlocks.registerModBlocks();
        ModBlockEntities.registerModBlockEntities();
        ModMenuTypes.registerModMenuTypes();

        LOGGER.info("Azazel's Alchemy Table initialized");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
