package ru.azazel.alchemytable.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import ru.azazel.alchemytable.menu.AlchemyTableMenu;

public class AlchemyTableBlockEntity
        extends BlockEntity
        implements Container, MenuProvider {

    // Схема слотов Льва сохраняется без изменений.
    public static final int POTIONSLOT1 = 0; // первое зелье
    public static final int POTIONSLOT2 = 1; // второе зелье
    public static final int POTIONSLOT3 = 2; // бутылочка -> результат
    public static final int POTIONSLOT4 = 3; // порошок ифрита
    public static final int CONTAINER_SIZE = 4;

    private final NonNullList<ItemStack> items =
            NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

    public AlchemyTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALCHEMY_TABLE_BLOCK_ENTITY, pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(
                "container.azazels-alchemy-table.alchemy_table"
        );
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId,
            Inventory playerInventory,
            Player player
    ) {
        return new AlchemyTableMenu(
                containerId,
                playerInventory,
                this
        );
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(
                this.items,
                slot,
                amount
        );

        if (!result.isEmpty()) {
            this.setChanged();
        }

        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        this.setChanged();

        // Смешивание выполняется только на сервере.
        if (this.level != null && !this.level.isClientSide()) {
            tryCraftPotion();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case POTIONSLOT1, POTIONSLOT2 -> stack.is(Items.POTION);
            case POTIONSLOT3 -> stack.is(Items.GLASS_BOTTLE);
            case POTIONSLOT4 -> stack.is(Items.BLAZE_POWDER);
            default -> false;
        };
    }

    /**
     * Два питьевых зелья + пустая бутылочка + порошок ифрита
     * превращаются в одно питьевое зелье с эффектами обоих входов.
     */
    private void tryCraftPotion() {
        ItemStack potion1 = this.items.get(POTIONSLOT1);
        ItemStack potion2 = this.items.get(POTIONSLOT2);
        ItemStack bottle = this.items.get(POTIONSLOT3);
        ItemStack fuel = this.items.get(POTIONSLOT4);

        if (!potion1.is(Items.POTION)
                || !potion2.is(Items.POTION)
                || !bottle.is(Items.GLASS_BOTTLE)
                || !fuel.is(Items.BLAZE_POWDER)) {
            return;
        }

        PotionContents contents1 = potion1.getOrDefault(
                DataComponents.POTION_CONTENTS,
                PotionContents.EMPTY
        );
        PotionContents contents2 = potion2.getOrDefault(
                DataComponents.POTION_CONTENTS,
                PotionContents.EMPTY
        );

        if (!contents1.hasEffects() || !contents2.hasEffects()) {
            return;
        }

        ItemStack result = new ItemStack(Items.POTION);
        PotionContents resultContents = PotionContents.EMPTY;

        for (MobEffectInstance effect : contents1.getAllEffects()) {
            resultContents = resultContents.withEffectAdded(
                    new MobEffectInstance(effect)
            );
        }

        for (MobEffectInstance effect : contents2.getAllEffects()) {
            resultContents = resultContents.withEffectAdded(
                    new MobEffectInstance(effect)
            );
        }

        result.set(DataComponents.POTION_CONTENTS, resultContents);
        result.set(
                DataComponents.CUSTOM_NAME,
                Component.translatable(
                        "item.azazels-alchemy-table.double_potion"
                )
        );

        potion1.shrink(1);
        potion2.shrink(1);
        fuel.shrink(1);

        // Пустая бутылочка не выдаётся отдельно: она становится результатом.
        this.items.set(POTIONSLOT3, result);

        if (potion1.isEmpty()) {
            this.items.set(POTIONSLOT1, ItemStack.EMPTY);
        }
        if (potion2.isEmpty()) {
            this.items.set(POTIONSLOT2, ItemStack.EMPTY);
        }
        if (fuel.isEmpty()) {
            this.items.set(POTIONSLOT4, ItemStack.EMPTY);
        }

        this.setChanged();
    }

    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);
        this.items.clear();
        ContainerHelper.loadAllItems(tag, this.items, registries);
    }
}
