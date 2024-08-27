package dev.cassis2310.falloutmc.init;

import dev.cassis2310.falloutmc.FalloutMc;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FalloutMcParticles
{
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(
            BuiltInRegistries.PARTICLE_TYPE,
            FalloutMc.MOD_ID
    );

    // Register all particles here.

    public static void register(IEventBus bus)
    {
        PARTICLE_TYPES.register(bus);
    }
}
