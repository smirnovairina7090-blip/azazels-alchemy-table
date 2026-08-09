package ru.azazel.alchemytable.menu;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import ru.azazel.alchemytable.AzazelSAlchemyTable;

public final class ModMenuTypes {

    public static final MenuType<AlchemyTableMenu> ALCHEMY_TABLE_MENU =
            register("alchemy_table", AlchemyTableMenu::new);

    private static <T extends AbstractContainerMenu> MenuType<T> register(
            String name,
            MenuType.MenuSupplier<T> constructor
    ) {
        return Registry.register(
                BuiltInRegistries.MENU,
                ResourceLocation.fromNamespaceAndPath(
                        AzazelSAlchemyTable.MOD_ID,
                        name
                ),
                new MenuType<>(constructor, FeatureFlagSet.of())
        );
    }

    public static void registerModMenuTypes() {
        AzazelSAlchemyTable.LOGGER.info(
                "Registering menu types for {}",
                AzazelSAlchemyTable.MOD_ID
        );
    }

    private ModMenuTypes() {
    }
}
