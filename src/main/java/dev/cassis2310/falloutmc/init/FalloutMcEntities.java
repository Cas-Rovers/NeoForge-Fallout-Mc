package dev.cassis2310.falloutmc.init;

import dev.cassis2310.falloutmc.FalloutMc;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FalloutMcEntities
{
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
            BuiltInRegistries.ENTITY_TYPE,
            FalloutMc.MOD_ID
    );

    // Register all entities here.

    public static void register(IEventBus bus)
    {
        ENTITY_TYPES.register(bus);
    }
}
