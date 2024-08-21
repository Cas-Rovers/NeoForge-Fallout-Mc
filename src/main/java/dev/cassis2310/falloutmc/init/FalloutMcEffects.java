package dev.cassis2310.falloutmc.init;

import dev.cassis2310.falloutmc.FalloutMc;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FalloutMcEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(
            BuiltInRegistries.MOB_EFFECT,
            FalloutMc.MOD_ID
    );

    // Register all effects here.

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}
