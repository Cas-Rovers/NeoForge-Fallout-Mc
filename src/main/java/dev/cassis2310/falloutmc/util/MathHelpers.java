package dev.cassis2310.falloutmc.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class MathHelpers
{
    // Some are duplicated from Mth, but kept here as they might have slightly different parameter order or names.

    private static final int PRIME_X = 501125321;
    private static final int PRIME_Y = 1136930381;

    /*
     * Linear interpolation methods.
     */

    /**
     * Linearly interpolates between two values.
     *
     * @param delta  the interpolation factor (0 = min, 1 = max)
     * @param min    the minimum value
     * @param max    the maximum value
     * @return       the interpolated value
     */
    public static float lerp(float delta, float min, float max)
    {
        return min + (max - min) * delta;
    }

    /**
     * Linearly interpolates between two values.
     *
     * @param delta  the interpolation factor (0 = min, 1 = max)
     * @param min    the minimum value
     * @param max    the maximum value
     * @return       the interpolated value
     */
    public static double lerp(double delta, double min, double max)
    {
        return min + (max - min) * delta;
    }

    /**
     * Linearly interpolates between four values on a unit square.
     *
     * @param value00 the value at (0, 0)
     * @param value01 the value at (0, 1)
     * @param value10 the value at (1, 0)
     * @param value11 the value at (1, 1)
     * @param delta0  the interpolation factor for the x-axis (0 = 0, 1 = 1)
     * @param delta1  the interpolation factor for the y-axis (0 = 0, 1 = 1)
     * @return        the interpolated value
     */
    public static float lerp4(float value00, float value01, float value10, float value11, float delta0, float delta1)
    {
        final float value0 = lerp(delta1, value00, value01);
        final float value1 = lerp(delta1, value10, value11);
        return lerp(delta0, value0, value1);
    }

    /**
     * Computes the inverse of the linear interpolation function.
     *
     * @param value  the value to find the interpolation factor for
     * @param min    the minimum value
     * @param max    the maximum value
     * @return       the interpolation factor (0 = min, 1 = max)
     */
    public static float inverseLerp(float value, float min, float max)
    {
        return (value - min) / (max - min);
    }

    /*
     * Hashing Methods.
     */

    /**
     * Computes a hash code for a block position.
     *
     * @param salt  the salt value
     * @param pos   the block position
     * @return      the hash code
     */
    public static int hash(long salt, BlockPos pos)
    {
        return hash(salt, pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Computes a hash code for a block position.
     *
     * @param salt  the salt value
     * @param x     the x-coordinate
     * @param y     the y-coordinate
     * @param z     the z-coordinate
     * @return      the hash code
     */
    public static int hash(long salt, int x, int y, int z)
    {
        long hash = salt ^ ((long) x * PRIME_X) ^ ((long) y * PRIME_Y) ^ z;
        hash *= 0x27d4eb2d;
        return (int) hash;
    }

    /*
     * Random Number Generation Methods.
     */

    /**
     * Creates a new random number generator forked from the given one.
     *
     * @param random  the random number generator to fork from
     * @return        the new random number generator
     */
    public static RandomSource fork(RandomSource random)
    {
        return new XoroshiroRandomSource(random.nextLong(), random.nextLong());
    }

    /**
     * Generates a random integer uniformly distributed in the range [min, max).
     *
     * @param random  the random number generator
     * @param min     the minimum value (inclusive)
     * @param max     the maximum value (exclusive)
     * @return        the random integer, uniformly distributed in the range [min, max)
     */
    public static int uniform(RandomSource random, int min, int max)
    {
        return min == max ? min : min + random.nextInt(max - min);
    }

    /**
     * Generates a random integer uniformly distributed in the range [min, max).
     *
     * @param random  the random number generator
     * @param min     the minimum value (inclusive)
     * @param max     the maximum value (exclusive)
     * @return        the random integer, uniformly distributed in the range [min, max)
     */
    public static int uniform(Random random, int min, int max)
    {
        return min == max ? min : min + random.nextInt(max - min);
    }

    /**
     * Generates a random float uniformly distributed in the range [min, max).
     *
     * @param random  the random number generator
     * @param min     the minimum value (inclusive)
     * @param max     the maximum value (exclusive)
     * @return        the random float, uniformly distributed in the range [min, max)
     */
    public static float uniform(RandomSource random, float min, float max)
    {
        return random.nextFloat() * (max - min) + min;
    }

    /**
     * Generates a random double uniformly distributed in the range [min, max).
     *
     * @param random  the random number generator
     * @param min     the minimum value (inclusive)
     * @param max     the maximum value (exclusive)
     * @return        A random double, uniformly distributed in the range [min, max).
     */
    public static double uniform(RandomSource random, double min, double max)
    {
        return random.nextDouble() * (max - min) + min;
    }

    /**
     * Generates a random float distributed around [-1, 1] in a triangle distribution.
     *
     * @param random  the random number generator
     * @return        A random float, distributed around [-1, 1] in a triangle distribution X ~ pdf(t) = 1 – |t|.
     */
    public static float triangle(RandomSource random)
    {
        return random.nextFloat() - random.nextFloat() * 0.5f;
    }

    /**
     * Generates a random integer distributed around (-range, range) in a triangle distribution.
     *
     * @param random  the random number generator
     * @param range   the range
     * @return        A random integer, distributed around (-range, range) in a triangle distribution X ~ pmf(t) ~= (1 – |t|)
     */
    public static int triangle(RandomSource random, int range)
    {
        return random.nextInt(range) - random.nextInt(range);
    }

    /*
     * Triangle methods.
     */

    /**
     * Returns a triangle function with input {@code value} and parameters {@code amplitude, midpoint, frequency}.
     * The triangle function has a sinusoidal shape with a period T = 1 / frequency.
     * The first peak is at +/-1 / (4 * frequency).
     * The function returns the value at {@code value} in the triangle function.
     *
     * @param amplitude The amplitude of the triangle function.
     * @param midpoint  The midpoint of the triangle function.
     * @param frequency The frequency of the triangle function.
     * @param value     The value at which to evaluate the triangle function.
     * @return          The value of the triangle function at {@code value}.
     */
    public static float triangle(float amplitude, float midpoint, float frequency, float value)
    {
        return midpoint + amplitude * (Math.abs( 4f * frequency * value + 1f - 4f * Mth.floor(frequency * value + 0.75f)) - 1f);
    }

    /**
     * Returns a random float distributed around [-delta, delta] in a triangle distribution.
     * The distribution is X ~ pdf(t) ~= (1 – |t|).
     *
     * @param random The random number generator.
     * @param delta  The range of the triangle distribution.
     * @return       A random float distributed around [-delta, delta] in a triangle distribution.
     */
    public static float triangle(RandomSource random, float delta)
    {
        return (random.nextFloat() - random.nextFloat()) * delta;
    }

    /**
     * Returns a random double distributed around [-delta, delta] in a triangle distribution.
     * The distribution is X ~ pdf(t) ~= (1 – |t|).
     *
     * @param random The random number generator.
     * @param delta  The range of the triangle distribution.
     * @return       A random double distributed around [-delta, delta] in a triangle distribution.
     */
    public static double triangle(RandomSource random, double delta)
    {
        return (random.nextDouble() - random.nextDouble()) * delta;
    }

    /*
     * Cubic methods.
     */

    /**
     * Returns an ease-in-out cubic function with input {@code x}.
     * The function returns the value at {@code x} in the ease-in-out cubic function.
     *
     * @param x The value at which to evaluate the ease-in-out cubic function.
     * @return  The value of the ease-in-out cubic function at {@code x}.
     */
    public static float easeInOutCubic(float x)
    {
        return x < 0.5f ? 4 * x * x * x : 1 - cube(-2 * x + 2) / 2;
    }

    /**
     * Returns the cube of the input {@code x}.
     *
     * @param x The input to be cubed.
     * @return  The cube of {@code x}.
     */
    private static float cube(float x)
    {
        return x * x * x;
    }

    /*
     * Other methods.
     */

    /**
     * Returns the ceiling division of {@code num} by {@code div}.
     *
     * @param num The numerator.
     * @param div The denominator.
     * @return    The ceiling division of {@code num} by {@code div}.
     * @see Math#floorDiv(int, int)
     */
    public static int ceilDiv(int num, int div)
    {
        return (num + div - 1) / div;
    }

    /**
     * Checks the existence of a <a href="https://en.wikipedia.org/wiki/Perfect_matching">perfect matching</a> of a <a href="https://en.wikipedia.org/wiki/Bipartite_graph">bipartite graph</a>.
     * The graph is interpreted as the matches between the set of inputs, and the set of tests.
     * This algorithm computes the <a href="https://en.wikipedia.org/wiki/Edmonds_matrix">Edmonds Matrix</a> of the graph, which has the property that the determinant is identically zero iff the graph does not admit a perfect matching.
     *
     * @param inputs The set of inputs.
     * @param tests  The set of tests.
     * @return       {@code true} if a perfect matching exists in the bipartite graph, {@code false} otherwise.
     */
    public static <T> boolean perfectMatchExists(List<T> inputs, List<? extends Predicate<T>> tests)
    {
        if (inputs.size() != tests.size())
        {
            return false;
        }
        final int size = inputs.size();
        final boolean[][] matrices  = new boolean[size][];
        for (int i = 0; i < size; i++)
        {
            matrices[i] = new boolean[(size + i) * (size + 1)];
        }
        final boolean[] matrix = matrices[size - 1];
        for (int i = 0; i < size; i++)
        {
            for (int j = 0; j < size; j++)
            {
                matrix[i + size * j] = tests.get(i).test(inputs.get(j));
            }
        }

        return perfectMatchDet(matrices, size);
    }

    /**
     * Computes a symbolic determinant for a perfect matching in a bipartite graph.
     * Used by {@link #perfectMatchExists(List, List)}
     *
     * @param matrices The matrices representing the bipartite graph.
     * @param size     The size of the bipartite graph.
     * @return         {@code true} if a perfect matching exists in the bipartite graph, {@code false} otherwise.
     */
    private static boolean perfectMatchDet(boolean[][] matrices, int size)
    {
        // matrix true = nonzero = matches
        final boolean[] matrix = matrices[size - 1];
        return switch (size)
        {
            case 1 -> matrix[0];
            case 2 -> (matrix[0] && matrix[3]) || (matrix[1] && matrix[2]);
            default ->
            {
                for (int c = 0; c < size; c++)
                {
                    if (matrix[c])
                    {
                        perfectMatchSub(matrices, size, c);
                        if (perfectMatchDet(matrices, size - 1))
                        {
                            yield true;
                        }
                    }
                }
                yield false;
            }
        };
    }

    /**
     * Computes the symbolic minor of a matrix by removing an arbitrary column.
     * Used by {@link #perfectMatchExists(List, List)}
     *
     * @param matrices The matrices representing the bipartite graph.
     * @param size     The size of the bipartite graph.
     * @param dc       The index of the column to be removed.
     */
    private static void perfectMatchSub(boolean[][] matrices, int size, int dc)
    {
        final int subSize = size - 1;
        final boolean[] matrix = matrices[subSize], sub = matrices[subSize - 1];
        for (int c = 0; c < subSize; c++)
        {
            final int c0 = c + (c >= dc ? 1 : 0);
            for (int r = 0; r < subSize; r++)
            {
                sub[c + subSize * r] = matrix[c0 + size * (r + 1)];
            }
        }
    }
}
