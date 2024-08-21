package dev.cassis2310.falloutmc.init;

import dev.cassis2310.falloutmc.FalloutMc;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FalloutMcItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FalloutMc.MOD_ID);

    // Register all items here.

    public static final DeferredItem<Item> NUKA_COLA = ITEMS.register("nuka_cola",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
