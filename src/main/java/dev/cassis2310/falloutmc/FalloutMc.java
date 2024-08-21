package dev.cassis2310.falloutmc;

import dev.cassis2310.falloutmc.item.FalloutMcItems;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(FalloutMc.MOD_ID)
public class FalloutMc
{
    public static final String MOD_ID = "falloutmc";
    public static final String MOD_NAME = "FalloutMc";
    private static final Logger LOGGER = LogUtils.getLogger();

    public FalloutMc(IEventBus bus, ModContainer container) {
        bus.addListener(this::commonSetup);

        FalloutMcItems.register(bus);

        NeoForge.EVENT_BUS.register(this);
        bus.addListener(this::addCreative);
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        // Some creative mode tabs.
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Some server starting code
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
        }
    }
}
