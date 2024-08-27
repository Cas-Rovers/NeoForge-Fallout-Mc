package dev.cassis2310.falloutmc.init;

import dev.cassis2310.falloutmc.FalloutMc;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FalloutMcBlocks
{
    public static final  DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FalloutMc.MOD_ID);

    // Register all blocks here.

    public static void register(IEventBus bus)
    {
        BLOCKS.register(bus);
    }
}
