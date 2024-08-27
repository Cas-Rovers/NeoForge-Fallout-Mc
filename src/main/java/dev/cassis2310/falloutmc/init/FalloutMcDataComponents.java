package dev.cassis2310.falloutmc.init;

import dev.cassis2310.falloutmc.FalloutMc;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FalloutMcDataComponents
{
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            FalloutMc.MOD_ID
    );

    // register all data components here.

    public static void register(IEventBus bus)
    {
        DATA_COMPONENTS.register(bus);
    }
}
