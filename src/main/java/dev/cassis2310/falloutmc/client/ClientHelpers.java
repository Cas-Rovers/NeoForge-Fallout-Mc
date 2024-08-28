package dev.cassis2310.falloutmc.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Client side methods for proxy use
 */
public class ClientHelpers
{
    /**
     * An array containing all possible directions, including null.
     */
    public static final Direction[] DIRECTIONS_AND_NULL = new Direction[] { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP, null };

    /**
     * Safely retrieves the {@link RecipeManager} from the client-side Minecraft instance.
     *
     * @return The {@link RecipeManager} if available, otherwise null.
     */
    @Nullable
    @SuppressWarnings("ConstantValue")
    public static RecipeManager tryGetSafeRecipeManager()
    {
        final @Nullable Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.level != null ? mc.level.getRecipeManager() : null;
    }

    /**
     * Retrieves the current client-side {@link Level} (world) instance.
     *
     * @return The current {@link Level}, or null if not available.
     */
    @Nullable
    public static Level getLevel()
    {
        return Minecraft.getInstance().level;
    }

    /**
     * Retrieves the current client-side {@link Level} (world) instance, throwing an exception if it's not available.
     *
     * @return The current {@link Level}.
     * @throws NullPointerException if the {@link Level} is not available.
     */
    public static Level getLevelOrThrow()
    {
        return Objects.requireNonNull(getLevel());
    }

    /**
     * Retrieves the current client-side {@link Player} instance.
     *
     * @return The current {@link Player}, or null if not available.
     */
    @Nullable
    public static Player getPlayer()
    {
        return Minecraft.getInstance().player;
    }

    /**
     * Retrieves the current client-side {@link Player} instance, throwing an exception if it's not available.
     *
     * @return The current {@link Player}.
     * @throws NullPointerException if the {@link Player} is not available.
     */
    public static Player getPlayerOrThrow()
    {
        return Objects.requireNonNull(getPlayer());
    }

    /**
     * Checks whether the client is using fancy graphics settings.
     *
     * @return true if fancy graphics are enabled, otherwise false.
     */
    public static boolean useFancyGraphics()
    {
        return Minecraft.useFancyGraphics();
    }

    /**
     * Retrieves the position of the block the player is currently looking at (the targeted block).
     *
     * @return The {@link BlockPos} of the targeted block, or null if no block is targeted.
     */
    @Nullable
    public static BlockPos getTargetPos()
    {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.hitResult instanceof BlockHitResult block)
        {
            return block.getBlockPos();
        }
        return null;
    }

    /**
     * Checks whether the Shift key is currently held down.
     *
     * @return true if the Shift key is pressed, otherwise false.
     */
    public static boolean hasShiftDown()
    {
        return Screen.hasShiftDown();
    }
}
