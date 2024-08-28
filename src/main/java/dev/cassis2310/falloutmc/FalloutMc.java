package dev.cassis2310.falloutmc;

import dev.cassis2310.falloutmc.init.*;
import dev.cassis2310.falloutmc.util.Helpers;
import dev.cassis2310.falloutmc.util.SelfTests;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.Nullable;
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

    private @Nullable Throwable syncLoadError;

    public FalloutMc(IEventBus bus, ModContainer container)
    {
        LOGGER.info("[{}] Initializing {} with Environment: Assertions = {}, Debug = {}, Production = {}, Dist = {}, Self Test = {} (Fatal = {})",
                MOD_NAME,
                MOD_ID,
                detectAssertionsEnabled(),
                LOGGER.isDebugEnabled(),
                FMLEnvironment.production,
                FMLEnvironment.dist,
                SelfTests.ENABLED,
                SelfTests.THROW_ON_FAILURE);

        SelfTests.runWorldVersionTest();

        bus.addListener(this::commonSetup);
        bus.addListener(this::loadComplete);

        FalloutMcItems.register(bus);
        FalloutMcSounds.register(bus);
        FalloutMcBlocks.register(bus);
        FalloutMcEffects.register(bus);
        FalloutMcEntities.register(bus);
        FalloutMcParticles.register(bus);
        FalloutMcDataComponents.register(bus);
        FalloutMcContainerTypes.register(bus);

        NeoForge.EVENT_BUS.register(this);
        bus.addListener(this::addCreative);
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        if (FMLEnvironment.dist == Dist.CLIENT)
        {
            // Some client thingies.
        }

        NeoForgeMod.enableMilkFluid();
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("{}: Performing common setup", MOD_NAME);

        // Some common setup code

        event.enqueueWork(() -> {
            // Some common setup code
        }).exceptionally(e -> {
            LOGGER.error("An unhandled exception was thrown during synchronous mod loading:", e);
            syncLoadError = e;
            return null;
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        // Some creative mode tabs.
    }

    public void loadComplete(FMLLoadCompleteEvent event)
    {
        if (syncLoadError != null)
        {
            Helpers.throwAsUnchecked(syncLoadError);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Some server starting code
        LOGGER.info("{}: Starting server", MOD_NAME);
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

    @SuppressWarnings({"AssertWithSideEffects", "ConstantConditions"})
    private boolean detectAssertionsEnabled()
    {
        boolean enabled = false;
        assert enabled = true;
        return enabled;
    }
}
