package ru.azazel.alchemytable.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import ru.azazel.alchemytable.block.ModBlocks;
import ru.azazel.alchemytable.client.screen.AlchemyTableScreen;
import ru.azazel.alchemytable.menu.ModMenuTypes;

public class AzazelSAlchemyTableClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(
                ModBlocks.ALCHEMY_TABLE,
                RenderType.cutout()
        );

        MenuScreens.register(
                ModMenuTypes.ALCHEMY_TABLE_MENU,
                AlchemyTableScreen::new
        );
    }
}
