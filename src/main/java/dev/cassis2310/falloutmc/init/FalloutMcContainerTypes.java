package dev.cassis2310.falloutmc.init;

import dev.cassis2310.falloutmc.FalloutMc;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FalloutMcContainerTypes
{
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(
            BuiltInRegistries.MENU,
            FalloutMc.MOD_ID
    );

    public static void register(IEventBus bus)
    {
        CONTAINERS.register(bus);
    }
}
