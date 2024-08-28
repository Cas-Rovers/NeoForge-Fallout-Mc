package dev.cassis2310.falloutmc.util;

import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

public class SelfTests
{
    public static final boolean THROW_ON_FAILURE = false;
    public static final boolean ENABLED = Boolean.getBoolean("falloutmc.enableDebugSelfTests");

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean EXTERNAL_ERROR = false;

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    public static void runWorldVersionTest()
    {
        assert SharedConstants.WORLD_VERSION == 3953 : "If this fails, you need to update the world version here.";
    }

    public static void runClientSelfTests()
    {
        if (ENABLED)
        {
//            NeoForge.EVENT_BUS.post();
            final Stopwatch tick = Stopwatch.createStarted();
            // TODO: Add client self tests
            LOGGER.info("client self tests passed in {}", tick.stop());
        }
    }

    public static void runServerSelfTests(MinecraftServer server)
    {
        if (ENABLED)
        {
            final Stopwatch tick = Stopwatch.createStarted();
            // TODO: Add server self tests
            LOGGER.info("server self tests passed in {}", tick.stop());
        }
    }
}
