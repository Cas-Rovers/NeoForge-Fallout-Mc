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
    public static final Direction[] DIRECTIONS_AND_NULL = new Direction[] { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP, null };

    @Nullable
    @SuppressWarnings("ConstantValue")
    public static RecipeManager tryGetSafeRecipeManager()
    {
        final @Nullable Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.level != null ? mc.level.getRecipeManager() : null;
    }

    @Nullable
    public static Level getLevel()
    {
        return Minecraft.getInstance().level;
    }

    public static Level getLevelOrThrow()
    {
        return Objects.requireNonNull(getLevel());
    }

    @Nullable
    public static Player getPlayer()
    {
        return Minecraft.getInstance().player;
    }

    public static Player getPlayerOrThrow()
    {
        return Objects.requireNonNull(getPlayer());
    }

    public static boolean useFancyGraphics()
    {
        return Minecraft.useFancyGraphics();
    }

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

    public static boolean hasShiftDown()
    {
        return Screen.hasShiftDown();
    }
}
