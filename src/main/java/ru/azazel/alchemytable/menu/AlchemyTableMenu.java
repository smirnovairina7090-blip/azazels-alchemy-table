package ru.azazel.alchemytable.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import ru.azazel.alchemytable.block.entity.AlchemyTableBlockEntity;

public class AlchemyTableMenu extends AbstractContainerMenu {

    private static final int TABLE_SLOT_COUNT =
            AlchemyTableBlockEntity.CONTAINER_SIZE;
    private static final int TABLE_END = TABLE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = TABLE_END;
    private static final int PLAYER_INVENTORY_END =
            PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    // Эти координаты совпадают с GUI 176x166.
    private static final int POTION_1_X = 55;
    private static final int POTION_1_Y = 30;
    private static final int POTION_2_X = 105;
    private static final int POTION_2_Y = 30;
    private static final int BOTTLE_RESULT_X = 80;
    private static final int BOTTLE_RESULT_Y = 55;
    private static final int FUEL_X = 13;
    private static final int FUEL_Y = 14;
    private static final int PLAYER_INVENTORY_X = 8;
    private static final int PLAYER_INVENTORY_Y = 87;
    private static final int HOTBAR_X = 8;
    private static final int HOTBAR_Y = 145;
    private static final int SLOT_DISTANCE = 18;

    private final Container container;

    // Клиентский конструктор.
    public AlchemyTableMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(AlchemyTableBlockEntity.CONTAINER_SIZE)
        );
    }

    // Серверный конструктор.
    public AlchemyTableMenu(
            int containerId,
            Inventory playerInventory,
            Container container
    ) {
        super(ModMenuTypes.ALCHEMY_TABLE_MENU, containerId);

        checkContainerSize(
                container,
                AlchemyTableBlockEntity.CONTAINER_SIZE
        );

        this.container = container;
        this.container.startOpen(playerInventory.player);

        addAlchemyTableSlots();
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    private void addAlchemyTableSlots() {
        addPotionSlot(
                AlchemyTableBlockEntity.POTIONSLOT1,
                POTION_1_X,
                POTION_1_Y
        );
        addPotionSlot(
                AlchemyTableBlockEntity.POTIONSLOT2,
                POTION_2_X,
                POTION_2_Y
        );
        addBottleResultSlot(
                AlchemyTableBlockEntity.POTIONSLOT3,
                BOTTLE_RESULT_X,
                BOTTLE_RESULT_Y
        );
        addFuelSlot(
                AlchemyTableBlockEntity.POTIONSLOT4,
                FUEL_X,
                FUEL_Y
        );
    }

    private void addPotionSlot(int containerSlot, int x, int y) {
        this.addSlot(new Slot(this.container, containerSlot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.POTION);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
    }

    private void addBottleResultSlot(int containerSlot, int x, int y) {
        this.addSlot(new Slot(this.container, containerSlot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.GLASS_BOTTLE);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
    }

    private void addFuelSlot(int containerSlot, int x, int y) {
        this.addSlot(new Slot(this.container, containerSlot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.BLAZE_POWDER);
            }
        });
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int inventorySlot = column + row * 9 + 9;
                int x = PLAYER_INVENTORY_X + column * SLOT_DISTANCE;
                int y = PLAYER_INVENTORY_Y + row * SLOT_DISTANCE;

                this.addSlot(new Slot(
                        playerInventory,
                        inventorySlot,
                        x,
                        y
                ));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int column = 0; column < 9; column++) {
            int x = HOTBAR_X + column * SLOT_DISTANCE;
            this.addSlot(new Slot(
                    playerInventory,
                    column,
                    x,
                    HOTBAR_Y
            ));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = this.slots.get(index);

        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack originalStack = sourceStack.copy();

        if (index < TABLE_END) {
            if (!this.moveItemStackTo(
                    sourceStack,
                    PLAYER_INVENTORY_START,
                    HOTBAR_END,
                    true
            )) {
                return ItemStack.EMPTY;
            }
        } else if (sourceStack.is(Items.POTION)) {
            if (!this.moveItemStackTo(
                    sourceStack,
                    AlchemyTableBlockEntity.POTIONSLOT1,
                    AlchemyTableBlockEntity.POTIONSLOT3,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else if (sourceStack.is(Items.GLASS_BOTTLE)) {
            if (!this.moveItemStackTo(
                    sourceStack,
                    AlchemyTableBlockEntity.POTIONSLOT3,
                    AlchemyTableBlockEntity.POTIONSLOT4,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else if (sourceStack.is(Items.BLAZE_POWDER)) {
            if (!this.moveItemStackTo(
                    sourceStack,
                    AlchemyTableBlockEntity.POTIONSLOT4,
                    TABLE_END,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else if (
                index >= PLAYER_INVENTORY_START
                        && index < PLAYER_INVENTORY_END
        ) {
            if (!this.moveItemStackTo(
                    sourceStack,
                    HOTBAR_START,
                    HOTBAR_END,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else if (index >= HOTBAR_START && index < HOTBAR_END) {
            if (!this.moveItemStackTo(
                    sourceStack,
                    PLAYER_INVENTORY_START,
                    PLAYER_INVENTORY_END,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        if (sourceStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        sourceSlot.onTake(player, sourceStack);
        return originalStack;
    }
}
