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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import ru.azazel.alchemytable.menu.AlchemyTableMenu;
import net.minecraft.world.inventory.ContainerData;


public class AlchemyTableBlockEntity
        extends BlockEntity
        implements Container, MenuProvider {


    // =========================================================
    // СЛОТЫ
    // =========================================================

    // Первое зелье.
    public static final int POTIONSLOT1 = 0;

    // Второе зелье.
    public static final int POTIONSLOT2 = 1;

    // Пустая бутылочка,
    // которая потом превращается в результат.
    public static final int POTIONSLOT3 = 2;

    // Порошок ифрита.
    public static final int POTIONSLOT4 = 3;

    // Всего четыре внутренних слота.
    public static final int CONTAINER_SIZE = 4;


    // =========================================================
    // ВРЕМЯ ПРИГОТОВЛЕНИЯ
    // =========================================================

    /**
     * Minecraft работает примерно с частотой
     * 20 игровых тиков в секунду.
     *
     * 80 тиков = примерно 4 секунды.
     */
    public static final int MAX_BREW_PROGRESS = 80;


    /**
     * Сколько тиков текущее зелье
     * уже находится в процессе приготовления.
     */
    private int brewProgress = 0;

        private final ContainerData data =
                new ContainerData() {
                        @Override
            public int get(int index) {

                return switch (index) {

                    // Текущий прогресс.
                    case 0 -> brewProgress;

                    // Максимальный прогресс.
                    case 1 -> MAX_BREW_PROGRESS;

                    default -> 0;
                };
            }

            @Override
            public void set(
                    int index,
                    int value
            ) {

                if (index == 0) {

                    brewProgress = value;
                }
            }

            @Override
            public int getCount() {

                return 2;
            }
                };


    // =========================================================
    // ВНУТРЕННИЙ ИНВЕНТАРЬ
    // =========================================================

    private final NonNullList<ItemStack> items =
            NonNullList.withSize(
                    CONTAINER_SIZE,
                    ItemStack.EMPTY
            );


    // =========================================================
    // КОНСТРУКТОР
    // =========================================================

    public AlchemyTableBlockEntity(
            BlockPos pos,
            BlockState state
    ) {

        super(
                ModBlockEntities.ALCHEMY_TABLE_BLOCK_ENTITY,
                pos,
                state
        );
    }


    // =========================================================
    // НАЗВАНИЕ ИНТЕРФЕЙСА
    // =========================================================

    @Override
    public Component getDisplayName() {

        return Component.translatable(
                "container.azazels-alchemy-table.alchemy_table"
        );
    }


    // =========================================================
    // СОЗДАНИЕ MENU
    // =========================================================

    @Override
    public AbstractContainerMenu createMenu(
            int containerId,
            Inventory playerInventory,
            Player player
    ) {

        return new AlchemyTableMenu(
                containerId,
                playerInventory,
                this,
                this.data
        );
    }


    // =========================================================
    // CONTAINER
    // =========================================================

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
    public ItemStack getItem(
            int slot
    ) {

        return this.items.get(
                slot
        );
    }


    @Override
    public ItemStack removeItem(
            int slot,
            int amount
    ) {

        ItemStack result =
                ContainerHelper.removeItem(
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
    public ItemStack removeItemNoUpdate(
            int slot
    ) {

        return ContainerHelper.takeItem(
                this.items,
                slot
        );
    }


    @Override
    public void setItem(
            int slot,
            ItemStack stack
    ) {

        // Просто кладём предмет в слот.
        this.items.set(
                slot,
                stack
        );


        // Ограничиваем размер стопки
        // стандартным лимитом предмета.
        stack.limitSize(
                this.getMaxStackSize(stack)
        );


        // Сообщаем Minecraft,
        // что BlockEntity изменилась.
        this.setChanged();


        /**
         * ВАЖНО:
         *
         * Раньше здесь сразу вызывался
         * tryCraftPotion().
         *
         * Теперь этого НЕТ.
         *
         * Иначе зелье создавалось бы
         * мгновенно без ожидания.
         */
    }


    @Override
    public boolean stillValid(
            Player player
    ) {

        return Container.stillValidBlockEntity(
                this,
                player
        );
    }


    @Override
    public void clearContent() {

        this.items.clear();

        // Если очистили стол,
        // процесс приготовления тоже сбрасываем.
        this.brewProgress = 0;

        this.setChanged();
    }


    // =========================================================
    // ПРАВИЛА СЛОТОВ
    // =========================================================

    @Override
    public boolean canPlaceItem(
            int slot,
            ItemStack stack
    ) {

        return switch (slot) {

            // Первые два слота —
            // обычные питьевые зелья.
            case POTIONSLOT1,
                 POTIONSLOT2 ->

                    stack.is(
                            Items.POTION
                    );


            // Третий слот —
            // пустая бутылочка.
            case POTIONSLOT3 ->

                    stack.is(
                            Items.GLASS_BOTTLE
                    );


            // Четвёртый слот —
            // порошок ифрита.
            case POTIONSLOT4 ->

                    stack.is(
                            Items.BLAZE_POWDER
                    );


            default -> false;
        };
    }


    // =========================================================
    // МОЖНО ЛИ НАЧАТЬ ПРИГОТОВЛЕНИЕ
    // =========================================================

    /**
     * Проверяет, собран ли правильный рецепт.
     *
     * Здесь мы НИЧЕГО ещё не создаём.
     *
     * Метод только отвечает:
     *
     * true  -> можно готовить;
     * false -> нельзя.
     */
    private boolean canBrew() {

        ItemStack potion1 =
                this.items.get(
                        POTIONSLOT1
                );

        ItemStack potion2 =
                this.items.get(
                        POTIONSLOT2
                );

        ItemStack bottle =
                this.items.get(
                        POTIONSLOT3
                );

        ItemStack fuel =
                this.items.get(
                        POTIONSLOT4
                );


        // -----------------------------------------
        // ПРОВЕРЯЕМ ТИПЫ ПРЕДМЕТОВ
        // -----------------------------------------

        if (!potion1.is(Items.POTION)) {

            return false;
        }


        if (!potion2.is(Items.POTION)) {

            return false;
        }


        if (!bottle.is(Items.GLASS_BOTTLE)) {

            return false;
        }


        if (!fuel.is(Items.BLAZE_POWDER)) {

            return false;
        }


        // -----------------------------------------
        // ПРОВЕРЯЕМ ЭФФЕКТЫ ЗЕЛИЙ
        // -----------------------------------------

        PotionContents contents1 =
                potion1.getOrDefault(
                        DataComponents.POTION_CONTENTS,
                        PotionContents.EMPTY
                );


        PotionContents contents2 =
                potion2.getOrDefault(
                        DataComponents.POTION_CONTENTS,
                        PotionContents.EMPTY
                );


        /**
         * Например, обычная бутылка воды
         * технически тоже может быть Items.POTION.
         *
         * Поэтому дополнительно проверяем,
         * что у обоих зелий есть эффекты.
         */
        return contents1.hasEffects()
                && contents2.hasEffects();
    }


    // =========================================================
    // TICK — ПРОЦЕСС ПРИГОТОВЛЕНИЯ
    // =========================================================

    /**
     * Этот метод должен вызываться Minecraft
     * каждый игровой тик.
     *
     * Если рецепт собран:
     *
     * 0
     * ↓
     * 1
     * ↓
     * 2
     * ↓
     * ...
     * ↓
     * 80
     * ↓
     * создаём зелье.
     */
    public static void tick(
            Level level,
            BlockPos pos,
            BlockState state,
            AlchemyTableBlockEntity blockEntity
    ) {

        /**
         * Игровую логику выполняем
         * только на серверной стороне.
         */
        if (level.isClientSide()) {

            return;
        }


        // -----------------------------------------
        // ЕСЛИ РЕЦЕПТ СОБРАН
        // -----------------------------------------

        if (blockEntity.canBrew()) {

            // Каждый тик увеличиваем прогресс.
            blockEntity.brewProgress++;


            // -------------------------------------
            // ПРОШЛО 80 ТИКОВ
            // -------------------------------------

            if (
                    blockEntity.brewProgress
                            >= MAX_BREW_PROGRESS
            ) {

                /**
                 * Только теперь создаём
                 * готовое двойное зелье.
                 */
                blockEntity.tryCraftPotion();


                /**
                 * После приготовления
                 * возвращаем таймер к нулю.
                 */
                blockEntity.brewProgress = 0;
            }


            // Сохраняем изменение прогресса.
            blockEntity.setChanged();
        }


        // -----------------------------------------
        // ЕСЛИ РЕЦЕПТ БОЛЬШЕ НЕ СОБРАН
        // -----------------------------------------

        else {

            /**
             * Например:
             *
             * процесс уже начался,
             * но игрок вытащил одно зелье.
             *
             * Тогда приготовление начинается
             * заново при следующем полном рецепте.
             */
            if (blockEntity.brewProgress != 0) {

                blockEntity.brewProgress = 0;

                blockEntity.setChanged();
            }
        }
    }


    // =========================================================
    // СОЗДАНИЕ ДВОЙНОГО ЗЕЛЬЯ
    // =========================================================

    /**
     * Два питьевых зелья
     * +
     * пустая бутылочка
     * +
     * порошок ифрита
     *
     * превращаются в одно питьевое зелье
     * с эффектами обоих исходных зелий.
     */
    private void tryCraftPotion() {

        ItemStack potion1 =
                this.items.get(
                        POTIONSLOT1
                );

        ItemStack potion2 =
                this.items.get(
                        POTIONSLOT2
                );

        ItemStack bottle =
                this.items.get(
                        POTIONSLOT3
                );

        ItemStack fuel =
                this.items.get(
                        POTIONSLOT4
                );


        // -----------------------------------------
        // ЕЩЁ РАЗ ПРОВЕРЯЕМ ИНГРЕДИЕНТЫ
        // -----------------------------------------

        if (!potion1.is(Items.POTION)
                || !potion2.is(Items.POTION)
                || !bottle.is(Items.GLASS_BOTTLE)
                || !fuel.is(Items.BLAZE_POWDER)) {

            return;
        }


        // -----------------------------------------
        // ЧИТАЕМ ЭФФЕКТЫ
        // -----------------------------------------

        PotionContents contents1 =
                potion1.getOrDefault(
                        DataComponents.POTION_CONTENTS,
                        PotionContents.EMPTY
                );


        PotionContents contents2 =
                potion2.getOrDefault(
                        DataComponents.POTION_CONTENTS,
                        PotionContents.EMPTY
                );


        // Оба зелья должны содержать эффекты.
        if (!contents1.hasEffects()
                || !contents2.hasEffects()) {

            return;
        }


        // =====================================================
        // СОЗДАЁМ РЕЗУЛЬТАТ
        // =====================================================

        ItemStack result =
                new ItemStack(
                        Items.POTION
                );


        PotionContents resultContents =
                PotionContents.EMPTY;


        // -----------------------------------------
        // ЭФФЕКТЫ ПЕРВОГО ЗЕЛЬЯ
        // -----------------------------------------

        for (
                MobEffectInstance effect :
                contents1.getAllEffects()
        ) {

            resultContents =
                    resultContents.withEffectAdded(
                            new MobEffectInstance(
                                    effect
                            )
                    );
        }


        // -----------------------------------------
        // ЭФФЕКТЫ ВТОРОГО ЗЕЛЬЯ
        // -----------------------------------------

        for (
                MobEffectInstance effect :
                contents2.getAllEffects()
        ) {

            resultContents =
                    resultContents.withEffectAdded(
                            new MobEffectInstance(
                                    effect
                            )
                    );
        }


        // -----------------------------------------
        // ЗАПИСЫВАЕМ ЭФФЕКТЫ
        // -----------------------------------------

        result.set(
                DataComponents.POTION_CONTENTS,
                resultContents
        );


        // -----------------------------------------
        // НАЗВАНИЕ РЕЗУЛЬТАТА
        // -----------------------------------------

        result.set(
                DataComponents.CUSTOM_NAME,
                Component.translatable(
                        "item.azazels-alchemy-table.double_potion"
                )
        );


        // =====================================================
        // РАСХОДУЕМ ИНГРЕДИЕНТЫ
        // =====================================================

        potion1.shrink(
                1
        );

        potion2.shrink(
                1
        );

        fuel.shrink(
                1
        );


        /**
         * Пустая бутылочка не выдаётся отдельно.
         *
         * Она становится новым зельем.
         */
        this.items.set(
                POTIONSLOT3,
                result
        );


        // -----------------------------------------
        // ОЧИЩАЕМ ПУСТЫЕ СТОПКИ
        // -----------------------------------------

        if (potion1.isEmpty()) {

            this.items.set(
                    POTIONSLOT1,
                    ItemStack.EMPTY
            );
        }


        if (potion2.isEmpty()) {

            this.items.set(
                    POTIONSLOT2,
                    ItemStack.EMPTY
            );
        }


        if (fuel.isEmpty()) {

            this.items.set(
                    POTIONSLOT4,
                    ItemStack.EMPTY
            );
        }


        // Minecraft должен сохранить изменения.
        this.setChanged();
    }


    // =========================================================
    // СОХРАНЕНИЕ
    // =========================================================

    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {

        super.saveAdditional(
                tag,
                registries
        );


        // Сохраняем предметы.
        ContainerHelper.saveAllItems(
                tag,
                this.items,
                registries
        );


        // Сохраняем текущий таймер приготовления.
        tag.putInt(
                "BrewProgress",
                this.brewProgress
        );
    }


    // =========================================================
    // ЗАГРУЗКА
    // =========================================================

    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {

        super.loadAdditional(
                tag,
                registries
        );


        // Загружаем предметы.
        this.items.clear();

        ContainerHelper.loadAllItems(
                tag,
                this.items,
                registries
        );


        // Восстанавливаем таймер приготовления.
        this.brewProgress =
                tag.getInt(
                        "BrewProgress"
                );
    }
}

