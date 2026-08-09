package ru.azazel.alchemytable.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import ru.azazel.alchemytable.AzazelSAlchemyTable;
import ru.azazel.alchemytable.menu.AlchemyTableMenu;

public class AlchemyTableScreen
        extends AbstractContainerScreen<AlchemyTableMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    AzazelSAlchemyTable.MOD_ID,
                    "textures/gui/alchemy_table.png"
            );

    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 166;

    // Координаты надписей ВНУТРИ окна.
    // Они всегда считаются от leftPos/topPos.
    private static final int TITLE_Y = 6;
    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 72;

    public AlchemyTableScreen(
            AlchemyTableMenu menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);

        this.imageWidth = TEXTURE_WIDTH;
        this.imageHeight = TEXTURE_HEIGHT;
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        // Затемняем мир стандартным способом.
        this.renderBackground(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );

        // Рисуем фон GUI, предметы и слоты.
        // renderLabels() ниже намеренно пустой.
        super.render(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );

        // Рисуем текст САМИ — один раз и строго относительно окна.
        renderFixedText(guiGraphics);

        // Подсказка предмета должна быть поверх текста/GUI.
        this.renderTooltip(
                guiGraphics,
                mouseX,
                mouseY
        );
    }

    private void renderFixedText(
            GuiGraphics guiGraphics
    ) {
        // Заголовок всегда центрируется относительно 176 px окна.
        int titleX =
                this.leftPos
                        + (this.imageWidth
                        - this.font.width(this.title)) / 2;

        guiGraphics.drawString(
                this.font,
                this.title,
                titleX,
                this.topPos + TITLE_Y,
                0x404040,
                false
        );

        // "Инвентарь" имеет фиксированную точку внутри GUI.
        guiGraphics.drawString(
                this.font,
                this.playerInventoryTitle,
                this.leftPos + INVENTORY_X,
                this.topPos + INVENTORY_Y,
                0x404040,
                false
        );
    }

    @Override
    protected void renderBg(
            GuiGraphics guiGraphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        guiGraphics.blit(
                TEXTURE,
                this.leftPos,
                this.topPos,
                0.0F,
                0.0F,
                this.imageWidth,
                this.imageHeight,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }

    @Override
    protected void renderLabels(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {
        // Намеренно пусто.
        //
        // Стандартные labels AbstractContainerScreen отключены,
        // чтобы ни заголовок, ни "Инвентарь" не могли
        // отрисоваться второй раз.
    }
}
