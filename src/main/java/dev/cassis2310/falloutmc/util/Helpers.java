package dev.cassis2310.falloutmc.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.machinezoo.noexception.throwing.ThrowingRunnable;
import com.machinezoo.noexception.throwing.ThrowingSupplier;
import com.mojang.logging.LogUtils;
import dev.cassis2310.falloutmc.FalloutMc;
import dev.cassis2310.falloutmc.client.ClientHelpers;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This class provides a collection of utility methods that help with various common tasks in the FalloutMC mod.
 * It includes methods for handling capabilities, generating resource locations, managing enums, and other general-purpose
 * tasks that can be reused across the mod. This helps keep the code clean and avoids repetition.
 * <p>
 * The utilities in this class are designed to work seamlessly with the Minecraft and NeoForge modding environments.
 */
public class Helpers
{
    /**
     * An array containing all possible directions in Minecraft, useful for iterating through all directions.
     */
    public static final Direction[] DIRECTIONS = Direction.values();

    /**
     * An array containing all directions except DOWN, useful for operations that need to exclude the downward direction.
     */
    public static final Direction[] DIRECTIONS_NOT_DOWN = new Direction[] { Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST };

    /**
     * An array of all dye colors available in Minecraft.
     */
    public static final DyeColor[] DYE_COLORS = DyeColor.values();

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Stores a reference to the recipe manager (initially null).
     */
    @Nullable private static RecipeManager CACHED_RECIPE_MANAGER = null;

    /**
     * Returns a {@link ResourceLocation} with the "falloutmc" namespace.
     * This method is commonly used to create resource paths specific to the FalloutMC mod.
     *
     * @param name The path of the resource.
     * @return     A {@link ResourceLocation} with the "falloutmc" namespace.
     */
    public static ResourceLocation identifier(String name)
    {
        return resourceLocation(FalloutMc.MOD_ID, name);
    }

    /**
     * Returns a {@link ResourceLocation} with the "minecraft" namespace.
     * This method is useful when referencing vanilla Minecraft resources.
     *
     * @param name The path of the resource.
     * @return     A {@link ResourceLocation} with the "minecraft" namespace.
     */
    public static ResourceLocation identifierMc(String name)
    {
        return resourceLocation("minecraft", name);
    }

    /**
     * Returns a {@link ResourceLocation} by inferring the namespace from the provided name.
     * If no namespace is specified, "minecraft" will be used by default.
     *
     * @param name The full name of the resource, including optional namespace.
     * @return     A {@link ResourceLocation} with the inferred namespace.
     */
    public static ResourceLocation resourceLocation(String name)
    {
        return ResourceLocation.parse(name);
    }

    /**
     * Returns a {@link ResourceLocation} with the specified namespace and path.
     * This method allows for explicit control over the namespace and path of a resource.
     *
     * @param namespace The namespace for the resource (for example, "minecraft" or "falloutmc").
     * @param path The path of the resource within the namespace.
     * @return     A {@link ResourceLocation} with the given namespace and path.
     */
    public static ResourceLocation resourceLocation(String namespace, String path)
    {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    /**
     * Retrieves the capability for a block at a given position in a level.
     * This is useful for interacting with blocks that have capabilities such as inventories or fluid tanks.
     *
     * @param capability The capability to retrieve.
     * @param level      The level (world) where the block is located.
     * @param pos        The position of the block.
     * @param <T>        The type of the capability.
     * @param <C>        The context for the capability (if any).
     * @return           The capability, or null if not available.
     */
    @Nullable
    public static <T, C> T getCapability(BlockCapability<T, @Nullable C> capability, Level level, BlockPos pos)
    {
        return level.getCapability(capability, pos, null);
    }

    /**
     * Retrieves the capability for a block entity.
     * This method is particularly useful when working with custom block entities that expose capabilities.
     *
     * @param capability The capability to retrieve.
     * @param entity     The block entity.
     * @param <T>        The type of the capability.
     * @param <C>        The context for the capability (if any).
     * @return           The capability, or null if not available.
     */
    @Nullable
    public static <T, C> T getCapability(BlockCapability<T, @Nullable C> capability, BlockEntity entity) {
        return getCapability(capability, entity, null);
    }

    /**
     * Gets a capability from a block entity with an additional context.
     *
     * @param capability The capability to get
     * @param entity     The block entity
     * @param context    The context
     * @param <T>        The type of the capability
     * @param <C>        The type of the context
     * @return           The capability with the context
     */
    @Nullable
    @SuppressWarnings("DataFlowIssue") // BlockEntity.level is in practice never null, and the @Nullable C is not picked up correctly w.r.t getCapability()
    public static <T, C> T getCapability(BlockCapability<T, @Nullable C> capability, BlockEntity entity, C context) {
        return entity.getLevel().getCapability(capability, entity.getBlockPos(), entity.getBlockState(), entity, context);
    }

    /**
     * Determines if an ItemStack might have a capability, either by already possessing it or by simulating a stack size of 1.
     * This is useful for containers and inventories that need to ensure an item can perform certain actions, like heating.
     *
     * @param stack      The ItemStack to check.
     * @param capability The capability to check for.
     * @param <T>        The type of the capability.
     * @return           True if the stack might have the capability, otherwise false.
     */
    public static <T> boolean mightHaveCapability(ItemStack stack, ItemCapability<T, Void> capability)
    {
        return stack.copyWithCount(1).getCapability(capability) != null;
    }

    /**
     * Creates a map from an enum class to values provided by a value mapper.
     * This method ensures consistent iteration order in the map.
     *
     * @param enumClass   The enum class to map.
     * @param valueMapper A function that maps each enum constant to a value.
     * @param <E>         The enum type.
     * @param <V>         The value type.
     * @return            A map of enum constants to values.
     */
    public static <E extends Enum<E>, V> Map<E, V> mapOf(Class<E> enumClass, Function<E, V> valueMapper)
    {
        return mapOf(enumClass, key -> true, valueMapper);
    }

    /**
     * Creates a map from an enum class to values provided by a value mapper, filtering by a predicate.
     * This method allows creating a map with only selected enum constants.
     *
     * @param enumClass    The enum class to map.
     * @param keyPredicate A predicate to filter enum constants.
     * @param valueMapper  A function that maps each enum constant to a value.
     * @param <E>          The enum type.
     * @param <V>          The value type.
     * @return             A map of selected enum constants to values.
     */
    public static <E extends Enum<E>, V> Map<E, V> mapOf(Class<E> enumClass, Predicate<E> keyPredicate, Function<E, V> valueMapper)
    {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(keyPredicate)
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                valueMapper,
                                (v, v2) -> Helpers.throwAsUnchecked(new AssertionError("Merging elements not allowed!")),
                                () -> new EnumMap<>(enumClass)));
    }

    /**
     * Transforms a map's values using a provided function and returns a new immutable map.
     * This method is useful for converting or transforming data stored in maps.
     *
     * @param map  The original map to transform.
     * @param func The function to apply to each value.
     * @param <K>  The key type.
     * @param <V1> The original value type.
     * @param <V2> The new value type.
     * @return     A new map with transformed values.
     */
    public static <K, V1, V2> Map<K, V2> mapValue(Map<K, V1> map, Function<V1, V2> func)
    {
        final ImmutableMap.Builder<K, V2> builder = ImmutableMap.builderWithExpectedSize(map.size());
        for (Map.Entry<K, V1> entry : map.entrySet())
        {
            builder.put(entry.getKey(), func.apply(entry.getValue()));
        }
        return builder.build();
    }

    /**
     * Returns a random value from a map, using the provided random source.
     * This method is useful for randomly selecting a value from a collection of mapped data.
     *
     * @param map    The map to select from.
     * @param random The random source to use.
     * @param <K>    The key type.
     * @param <V>    The value type.
     * @return       A random value from the map.
     */
    public static <K, V> V getRandomValue(Map<K, V> map, RandomSource random)
    {
        return Iterators.get(map.values().iterator(), random.nextInt(map.size()));
    }

    /**
     * Translates an enum into a localized text component.
     * This method is useful for displaying enum names in the user's language.
     *
     * @param anEnum The enum to translate.
     * @return       A localized text component representing the enum.
     */
    public static MutableComponent translateEnum(Enum<?> anEnum)
    {
        return Component.translatable(getEnumTranslationKey(anEnum));
    }

    /**
     * Translates an enum into a localized text component using a custom name.
     * This allows for custom enum translations, which may differ from the enum's class name.
     *
     * @param anEnum   The enum to translate.
     * @param enumName The custom name to use in the translation key.
     * @return         A localized text component representing the enum.
     */
    public static MutableComponent translateEnum(Enum<?> anEnum, String enumName)
    {
        return Component.translatable(getEnumTranslationKey(anEnum, enumName));
    }

    /**
     * Returns the translation key for an enum, using the enum's class name as the base.
     * This is typically used for creating localized strings for enum values.
     *
     * @param anEnum The enum to get the translation key for.
     * @return       The translation key for the enum.
     */
    public static String getEnumTranslationKey(Enum<?> anEnum)
    {
        return getEnumTranslationKey(anEnum, anEnum.getDeclaringClass().getSimpleName());
    }

    /**
     * Returns the translation key for an enum, using a custom name instead of the enum's class name.
     * This method allows for more flexibility in creating translation keys for enums.
     *
     * @param anEnum   The enum to get the translation key for.
     * @param enumName The custom name to use in the translation key.
     * @return         The translation key for the enum.
     */
    public static String getEnumTranslationKey(Enum<?> anEnum, String enumName)
    {
        return String.join(".", FalloutMc.MOD_ID, "enum", enumName, anEnum.name()).toLowerCase(Locale.ROOT);
    }

    /**
     * Safely retrieves the Level object from an unknown type.
     * This method is useful when dealing with code that may interact with Level objects in unconventional ways.
     *
     * @param maybeLevel An object that might be a Level or related type.
     * @return           The Level, or null if the object is not a Level.
     */
    @Nullable
    @SuppressWarnings("deprecation")
    public static Level getUnsafeLevel(Object maybeLevel)
    {
        if (maybeLevel instanceof Level level)
        {
            return level; // Most obvious case, if we can directly cast up to level.
        }
        if (maybeLevel instanceof WorldGenRegion level)
        {
            return level.getLevel(); // Special case for world gen, when we can access the level unsafely
        }
        return null; // A modder has done a strange ass thing
    }

    /**
     * Checks if an entity has moved since the last tick.
     * This method is particularly useful for blocks or items that react to movement, such as Powder Snow.
     *
     * @param entity The entity to check.
     * @return       True if the entity has moved, otherwise false.
     */
    public static boolean hasMoved(Entity entity)
    {
        return entity.xOld != entity.getX() && entity.zOld != entity.getZ();
    }

    /**
     * Rotates an entity around a specified origin at a given speed if the entity is on the ground.
     * This method ensures the entity's rotation and movement are handled properly both on the client and server sides.
     *
     * @param level  The level (world) where the entity is located.
     * @param entity The entity to rotate.
     * @param origin The origin point around which the entity should rotate.
     * @param speed  The speed of rotation.
     */
    public static void rotateEntity(Level level, Entity entity, Vec3 origin, float speed)
    {
        if (!entity.onGround() || entity.getDeltaMovement().y > 0 || speed == 0f) {
            return;
        }
        final float rot = (entity.getYHeadRot() + speed) % 360f;
        entity.setYRot(rot);
        if (level.isClientSide && entity instanceof Player)
        {
            final Vec3 offset = entity.position().subtract(origin).normalize();
            final Vec3 movement = new Vec3(-offset.z, 0, offset.x).scale(speed / 48f);
            entity.hurtMarked = true; // rsync movement
            return;
        }
        if (entity instanceof LivingEntity living)
        {
            entity.setYHeadRot(rot);
            entity.setYBodyRot(rot);
            entity.setOnGround(false);
            living.setNoActionTime(20);
            living.hurtMarked = true;
        }
    }

    /**
     * Copies the properties from one BlockState to another.
     * This method allows for transferring all properties from the source BlockState to the target BlockState.
     *
     * @param copyTo   The BlockState to which properties will be copied.
     * @param copyFrom The BlockState from which properties will be copied.
     * @return         The BlockState with properties copied from the source.
     */
    public static BlockState copyProperties(BlockState copyTo, BlockState copyFrom)
    {
        for (Property<?> property : copyFrom.getProperties())
        {
            copyTo = copyProperty(copyTo, copyFrom, property);
        }
        return copyTo;
    }

    /**
     * Copies a specific property from one BlockState to another.
     * This method ensures that only the specified property is copied from the source BlockState to the target BlockState.
     *
     * @param copyTo   The BlockState to which the property will be copied.
     * @param copyFrom The BlockState from which the property will be copied.
     * @param property The property to copy.
     * @param <T>      The type of the property value.
     * @return         The BlockState with the specified property copied from the source.
     */
    public static <T extends Comparable<T>> BlockState copyProperty(BlockState copyTo, BlockState copyFrom, Property<T> property)
    {
        return copyTo.hasProperty(property) ? copyTo.setValue(property, copyFrom.getValue(property)) : copyTo;
    }

    /**
     * Sets a property on a BlockState to a specified value.
     *
     * @param state    The BlockState to modify.
     * @param property The property to set.
     * @param value    The value to assign to the property.
     * @param <T>      The type of the property value.
     * @return         The modified BlockState with the updated property, or the original state if the property doesn't exist.
     */
    public static <T extends Comparable<T>> BlockState setProperty(BlockState state, Property<T> property, T value)
    {
        return state.hasProperty(property) ? state.setValue(property, value) : state;
    }

    /**
     * Retrieves the RecipeManager unsafely, which might return null.
     *
     * @return The RecipeManager instance if available.
     * @throws IllegalStateException if no RecipeManager is found.
     */
    public static RecipeManager getUnsafeRecipeManager()
    {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null)
        {
            return server.getRecipeManager();
        }

        try
        {
            final RecipeManager client = ClientHelpers.tryGetSafeRecipeManager();
            if (client != null)
            {
                return client;
            }
        }
        catch (Throwable t)
        {
            LOGGER.info("^ This is fine - No client or server recipe manager present upon initial resource reload on physical server");
        }

        if (CACHED_RECIPE_MANAGER != null)
        {
            return CACHED_RECIPE_MANAGER;
        }

        throw new IllegalStateException("No recipe manager was present - tried server, client, and captured value. This will cause problems!");
    }

    /**
     * Caches the RecipeManager instance for later use.
     * This method is useful for optimizing repeated access to the recipe manager.
     *
     * @param manager The RecipeManager to cache.
     */
    public static void setCachedRecipeManager(RecipeManager manager)
    {
        CACHED_RECIPE_MANAGER = manager;
    }

    /**
     * Damages {@code stack} by one point, when held by {@code entity} in {@code slot}
     *
     * @param stack  The item stack to damage.
     * @param entity The living entity holding the item.
     * @param slot   The equipment slot where the item is located.
     */
    public static void damageItem(ItemStack stack, LivingEntity entity, EquipmentSlot slot)
    {
        stack.hurtAndBreak(1, entity, slot);
    }

    /**
     * Damages {@code stack} by {@code amount}, when held by {@code entity} in {@code hand}
     *
     * @param stack  The item stack to damage.
     * @param amount The amount of damage to apply.
     * @param entity The living entity holding the item.
     * @param hand   The hand in which the item is held.
     */
    public static void damageItem(ItemStack stack, int amount, LivingEntity entity, InteractionHand hand)
    {
        stack.hurtAndBreak(amount, entity, LivingEntity.getSlotForHand(hand));
    }

    /**
     * Damages {@code stack} by one point, when held by {@code entity} in {@code hand}
     *
     * @param stack  The item stack to damage.
     * @param entity The living entity holding the item.
     * @param hand   The hand in which the item is held.
     */
    public static void damageItem(ItemStack stack, LivingEntity entity, InteractionHand hand)
    {
        stack.hurtAndBreak(1, entity, LivingEntity.getSlotForHand(hand));
    }

    /**
     * Damages {@code stack} without an entity present.
     *
     * @param stack The item stack to damage.
     * @param level The level in which the item exists.
     */
    public static void damageItem(ItemStack stack, Level level)
    {
        if (level instanceof ServerLevel serverLevel)
        {
            stack.hurtAndBreak(1, serverLevel, null, item -> {});
        }
    }

    /**
     * Removes a block at the specified position {@link Level#removeBlock(BlockPos, boolean)} but with all flags available.
     * Uses the fluid state at the position to create a legacy block in its place.
     *
     * @param level The level in which the block resides.
     * @param pos   The position of the block to remove.
     * @param flags The flags controlling the block removal process.
     */
    public static void removeBlock(LevelAccessor level, BlockPos pos, int flags)
    {
        level.setBlock(pos, level.getFluidState(pos).createLegacyBlock(), flags);
    }

    /**
     * Returns an iterable over the ItemStacks in the given inventory.
     *
     * @param inventory The inventory to iterate over.
     * @return          An Iterable of ItemStacks contained in the inventory.
     */
    public static Iterable<ItemStack> iterate(IItemHandler inventory)
    {
        return iterate(inventory, 0, inventory.getSlots());
    }

    /**
     * Returns an iterable over the ItemStacks in the given RecipeInput inventory.
     *
     * @param inventory The RecipeInput inventory to iterate over.
     * @return          An Iterable of ItemStacks contained in the RecipeInput inventory.
     */
    public static Iterable<ItemStack> iterate(RecipeInput inventory)
    {
        return () -> new Iterator<>() {
            private int slot = 0;

            @Override
            public boolean hasNext() {
                return slot < inventory.size();
            }

            @Override
            public ItemStack next() {
                return inventory.getItem(slot++);
            }
        };
    }

    /**
     * Returns an iterable over the ItemStacks in the given inventory,
     * limited to a specified range of slots.
     *
     * @param inventory          The inventory to iterate over.
     * @param startSlotInclusive The starting slot (inclusive).
     * @param endSlotExclusive   The ending slot (exclusive).
     * @return                   An Iterable of ItemStacks within the specified slot range.
     */
    public static Iterable<ItemStack> iterate(IItemHandler inventory, int startSlotInclusive, int endSlotExclusive)
    {
        return () -> new Iterator<>() {
            private int slot = startSlotInclusive;

            @Override
            public boolean hasNext() {
                return slot < endSlotExclusive;
            }

            @Override
            public ItemStack next() {
                return inventory.getStackInSlot(slot++);
            }

            @Override
            public void remove() {
                Helpers.removeStack(inventory, slot -1); // Remove the previous slot = previous call to next()
            }
        };
    }

    /**
     * Writes a list of ItemStacks to NBT format.
     *
     * @param provider The provider for resolving data during serialization.
     * @param stacks   The list of ItemStacks to write to NBT.
     * @return         A ListTag representing the serialized ItemStacks.
     */
    public static ListTag writeItemStacksToNbt(HolderLookup.Provider provider, List<ItemStack> stacks)
    {
        final ListTag list = new ListTag();
        for (final ItemStack stack : stacks)
        {
            list.add(stack.saveOptional(provider));
        }
        return list;
    }

    /**
     * Reads ItemStacks from NBT and populates the provided list.
     * Clears the list before adding new elements.
     *
     * @param provider The provider for resolving data during deserialization.
     * @param stacks   The list to populate with deserialized ItemStacks.
     * @param list     The ListTag containing the NBT data.
     */
    public static void readItemStacksFromNbt(HolderLookup.Provider provider, List<ItemStack> stacks, ListTag list)
    {
        stacks.clear();
        for (int i = 0; 1< list.size(); i++)
        {
            stacks.add(ItemStack.parseOptional(provider, list.getCompound(i)));
        }
    }

    /**
     * Reads ItemStacks from NBT and sets them in the provided list.
     * Assumes the list has a fixed size and replaces existing elements.
     *
     * @param provider The provider for resolving data during deserialization.
     * @param stacks   The list of ItemStacks to be updated.
     * @param list     The ListTag containing the NBT data.
     */
    public static void readFixedSizeItemStacksFromNbt(HolderLookup.Provider provider, List<ItemStack> stacks, ListTag list)
    {
        for (int i = 0; i < list.size(); i++)
        {
            stacks.set(i, ItemStack.parseOptional(provider, list.getCompound(i)));
        }
    }

    /**
     * Consumes items from a stack in increments of the stack's maximum size,
     * passing each consumed increment to a provided consumer.
     *
     * @param stack      The item stack to consume from.
     * @param totalCount The total number of items to consume.
     * @param consumer   The consumer to process each consumed increment.
     */
    public static void consumeInStackSizeIncrements(ItemStack stack, int totalCount, Consumer<ItemStack> consumer)
    {
        while (totalCount > 0)
        {
            final ItemStack splitStack = stack.copy();
            final int splitCount = Math.min(splitStack.getMaxStackSize(), totalCount);
            splitStack.setCount(splitCount);
            totalCount -= splitCount;
            consumer.accept(splitStack);
        }
    }

    /**
     * Gathers and consumes items within a bounding box, storing them in an inventory.
     *
     * @param level             The level in which the items reside.
     * @param bounds            The bounding box within which to gather items.
     * @param inventory         The inventory to store gathered items.
     * @param minSlotInclusive  The starting slot (inclusive) for storing items in the inventory.
     * @param maxSlotExclusive  The ending slot (exclusive) for storing items in the inventory.
     */
    public static void gatherAndConsumeItems(Level level, AABB bounds, IItemHandler inventory, int minSlotInclusive, int maxSlotExclusive)
    {
        gatherAndConsumeItems(level.getEntitiesOfClass(ItemEntity.class, bounds, EntitySelector.ENTITY_STILL_ALIVE), inventory, minSlotInclusive, maxSlotExclusive, Integer.MAX_VALUE);
    }

    /**
     * Gathers and consumes items within a bounding box, storing them in an inventory.
     * Allows specifying a maximum number of items to be gathered.
     *
     * @param level             The level in which the items reside.
     * @param bounds            The bounding box within which to gather items.
     * @param inventory         The inventory to store gathered items.
     * @param minSlotInclusive  The starting slot (inclusive) for storing items in the inventory.
     * @param maxSlotInclusive  The ending slot (inclusive) for storing items in the inventory.
     * @param maxItemsOverride  The maximum number of items to be gathered. If this limit is reached, no further items will be gathered.
     */
    public static void gatherAndConsumeItems(Level level, AABB bounds, IItemHandler inventory, int minSlotInclusive, int maxSlotInclusive, int maxItemsOverride)
    {
        gatherAndConsumeItems(level.getEntitiesOfClass(ItemEntity.class, bounds, EntitySelector.ENTITY_STILL_ALIVE), inventory, minSlotInclusive, maxSlotInclusive, maxItemsOverride);
    }

    /**
     * Gathers and consumes items from a collection of item entities, storing them in an inventory.
     * Allows specifying a maximum number of items to be gathered.
     *
     * @param items             A collection of ItemEntity objects to be gathered and consumed.
     * @param inventory         The inventory to store gathered items.
     * @param minSlotInclusive  The starting slot (inclusive) for storing items in the inventory.
     * @param maxSlotInclusive  The ending slot (inclusive) for storing items in the inventory.
     * @param maxItemsOverride  The maximum number of items to be gathered. If this limit is reached, no further items will be gathered.
     */
    public static void gatherAndConsumeItems(Collection<ItemEntity> items, IItemHandler inventory, int minSlotInclusive, int maxSlotInclusive, int maxItemsOverride)
    {
        final List<ItemEntity> availableItemEntities = new ArrayList<>();
        int availableItems = 0;
        for (ItemEntity entity : items)
        {
            if (inventory.isItemValid(maxSlotInclusive, entity.getItem()))
            {
                availableItems += entity.getItem().getCount();
                availableItemEntities.add(entity);
            }
            if (availableItems > maxItemsOverride)
            {
                availableItems = maxItemsOverride;
            }
        }
        Helpers.safelyConsumeItemsFromEntitiesIndividually(availableItemEntities, availableItems, item -> Helpers.insertSlots(inventory, item, minSlotInclusive, 1 + maxSlotInclusive).isEmpty());
    }

    /**
     * Consumes item entities from a collection, up to a specified maximum number of items.
     * Each item is passed to the provided consumer one at a time.
     * Assumes that consumption will always succeed.
     *
     * @param entities  A collection of ItemEntity objects to be consumed.
     * @param maximum   The maximum number of items to consume.
     * @param consumer  A consumer function that processes each item stack.
     */
    public static void consumeItemsFromEntitiesIndividually(Collection<ItemEntity> entities, int maximum, Consumer<ItemStack> consumer)
    {
        int consumed = 0;
        for (ItemEntity entity : entities)
        {
            final ItemStack stack = entity.getItem();
            while (consumed < maximum && !stack.isEmpty())
            {
                consumer.accept(stack.split(1));
                consumed++;
                if (stack.isEmpty())
                {
                    entity.discard();
                }
            }
        }
    }

    /**
     * Safely consumes item entities from a collection, up to a specified maximum number of items.
     * Each item is passed to the provided consumer one at a time.
     * If the consumer fails to process an item (returns {@code false}), the process stops.
     *
     * @param entities  A collection of ItemEntity objects to be consumed.
     * @param maximum   The maximum number of items to consume.
     * @param consumer  A predicate function that processes each item stack. Returns {@code true} if the stack was successfully consumed, {@code false} otherwise.
     */
    public static void safelyConsumeItemsFromEntitiesIndividually(Collection<ItemEntity> entities, int maximum, Predicate<ItemStack> consumer)
    {
        int consumed = 0;
        for (ItemEntity entity : entities)
        {
            final ItemStack stack = entity.getItem();
            while (consumed < maximum && !stack.isEmpty())
            {
                final ItemStack offer = stack.copyWithCount(1);
                if (!consumer.test(offer))
                {
                    return;
                }
                consumed++;
                stack.shrink(1);
                if (stack.isEmpty())
                {
                    entity.discard();
                }
            }
        }
    }

    /**
     * Removes and returns the ItemStack from the specified slot in the inventory.
     *
     * @param inventory The inventory from which to remove the ItemStack.
     * @param slot      The slot index from which to remove the ItemStack.
     * @return          The removed ItemStack.
     */
    public static ItemStack removeStack(IItemHandler inventory, int slot)
    {
        return inventory.extractItem(slot, Integer.MAX_VALUE, false);
    }

    /**
     * Attempts to insert a stack across all slots of an item handler
     *
     * @param stack The stack to be inserted
     * @return      The remainder after the stack is inserted, if any
     */
    public static ItemStack insertAllSlots(IItemHandler inventory, ItemStack stack)
    {
        return insertSlots(inventory, stack, 0, inventory.getSlots());
    }

    /**
     * Attempts to insert an ItemStack into a range of slots in the given inventory.
     * The method tries each slot sequentially until the ItemStack is either fully inserted
     * or no more slots are available in the specified range.
     *
     * @param inventory          The inventory into which the ItemStack should be inserted.
     * @param stack              The ItemStack to insert.
     * @param slotStartInclusive The starting slot index (inclusive).
     * @param slotEndExclusive   The ending slot index (exclusive).
     * @return                   The remaining ItemStack that couldn't be inserted,
     *                           or {@code ItemStack.EMPTY} if fully inserted.
     */
    public static ItemStack insertSlots(IItemHandler inventory, ItemStack stack, int slotStartInclusive, int slotEndExclusive)
    {
        for (int slot = slotStartInclusive; slot < slotEndExclusive; slot++)
        {
            stack = inventory.insertItem(slot, stack, false);
            if (stack.isEmpty())
            {
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    /**
     * Checks if every slot in the provided inventory is empty.
     *
     * @param inventory An iterable of ItemStacks representing the inventory.
     * @return          {@code true} if every slot in the provided inventory is empty, otherwise {@code false}.
     */
    public static boolean isEmpty(Iterable<ItemStack> inventory)
    {
        for (ItemStack stack : inventory)
            if (!stack.isEmpty())
                return false;
        return true;
    }

    /**
     * Attempts to spread fire in a random direction around a specified position in the world.
     * The fire spread is controlled by game rules and can be influenced by neighboring blocks.
     *
     * @param level  The server-level where the fire spread should occur.
     * @param pos    The starting position for fire spreading.
     * @param random A random source for determining fire spread direction and chance.
     * @param radius The radius within which the fire can spread.
     */
    public static void fireSpreaderTick(ServerLevel level, BlockPos pos, RandomSource random, int radius)
    {
        if (level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK))
        {
            for (int i = 0; i < radius; i++)
            {
                pos = pos.relative(Direction.Plane.HORIZONTAL.getRandomDirection(random));
                if (level.getRandom().nextFloat() < 0.25F)
                {
                    pos = pos.above();
                }
                final BlockState state = level.getBlockState(pos);
                if (!state.isAir())
                {
                    return;
                }
                if (hasFlammableNeighbours(level, pos))
                {
                    level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
                    return;
                }
            }
        }
    }

    /**
     * Checks if there are any flammable blocks adjacent to the specified position.
     *
     * @param level The level reader to check the blocks.
     * @param pos   The position to check for flammable neighbors.
     * @return      {@code true} if there are flammable blocks adjacent to the position, otherwise {@code false}.
     */
    private static boolean hasFlammableNeighbours(LevelReader level, BlockPos pos)
    {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (Direction direction : Helpers.DIRECTIONS)
        {
            mutable.setWithOffset(pos, direction);
            if (level.getBlockState(mutable).isFlammable(level, mutable, direction.getOpposite()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Destroys a block at the given position in the world and drops its items manually,
     * allowing customization of the loot context. This method is based on
     * {@link Level#destroyBlock(BlockPos, boolean, Entity, int)} but with additional flexibility.
     *
     * @param level   The server-level where the block is to be destroyed.
     * @param pos     The position of the block to destroy.
     * @param builder A consumer that allows modification of the loot context.
     */
    public static void destroyBlockAndDropBlocksManually(ServerLevel level, BlockPos pos, Consumer<LootParams.Builder> builder)
    {
        BlockState state = level.getBlockState(pos);
        if (!state.isAir())
        {
            FluidState fluidState = level.getFluidState(pos);
            if (!(state.getBlock() instanceof BaseFireBlock))
            {
                level.levelEvent(2001, pos, Block.getId(state));
            }
            dropWithContext(level, state, pos, builder, true);
            level.setBlock(pos, fluidState.createLegacyBlock(), 3, 512);
        }
    }

    /**
     * Drops items from a block with a customizable loot context.
     * This method supports both randomized and non-randomized drop locations.
     *
     * @param level      The server-level where the items should be dropped.
     * @param state      The block state from which the drops are generated.
     * @param pos        The position of the block being processed.
     * @param consumer   A consumer to modify the loot context before processing drops.
     * @param randomized If true, the drops will be randomized in position; otherwise, they will be centered.
     */
    public static void dropWithContext(ServerLevel level, BlockState state, BlockPos pos, Consumer<LootParams.Builder> consumer, boolean randomized)
    {
        BlockEntity tileEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;

        LootParams.Builder params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, null)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, tileEntity);
        consumer.accept(params);

        state.getDrops(params).forEach(stackToSpawn -> {
            if (randomized)
            {
                Block.popResource(level, pos, stackToSpawn);
            }
            else
            {
                spawnDropsAtExactCenter(level, pos, stackToSpawn);
            }
        });
        state.spawnAfterBreak(level, pos, ItemStack.EMPTY, false);
    }

    /**
     * Spawns an item stack at the exact center of the block's position without any randomness in velocity or position.
     * This is a deterministic version of {@link Block#popResource(Level, BlockPos, ItemStack)}.
     *
     * @param level The level where the item should be spawned.
     * @param pos   The position at which to spawn the item.
     * @param stack The ItemStack to spawn at the given position.
     */
    public static void spawnDropsAtExactCenter(Level level, BlockPos pos, ItemStack stack)
    {
        if (!level.isClientSide && !stack.isEmpty() && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !level.restoringBlockSnapshots)
        {
            ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack, 0D, 0D, 0D);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);
        }
    }

    /**
     * Plays a sound at the specified position in the given level.
     *
     * @param level The level to play the sound in.
     * @param pos   The position to play the sound at.
     * @param sound The sound event to play.
     */
    public static void playSound(Level level, BlockPos pos, SoundEvent sound)
    {
        var rand = level.getRandom();
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0f + rand.nextFloat(), rand.nextFloat() + 0.7f + 0.3f);
    }

    /**
     * Plays the placement sound for the given block state at the specified position.
     *
     * @param level The level to play the sound in.
     * @param pos   The position to play the sound at.
     * @param state The block state to play the sound for.
     */
    public static void playPlaceSound(LevelAccessor level, BlockPos pos, BlockState state)
    {
        playPlaceSound(level, pos, state.getSoundType(level, pos, null));
    }

    /**
     * Plays the placement sound for the given sound type at the specified position.
     *
     * @param level The level to play the sound in.
     * @param pos   The position to play the sound at.
     * @param st    The sound type to play the sound for.
     */
    public static void playPlaceSound(LevelAccessor level, BlockPos pos, SoundType st)
    {
        level.playSound(null, pos, st.getPlaceSound(), SoundSource.BLOCKS, (st.getVolume() + 1.0f) / 2.0f, st.getPitch() * 0.8f);
    }

    /**
     * Spawns an item entity at the specified position in the given level.
     *
     * @param level The level to spawn the item in.
     * @param pos   The position to spawn the item at.
     * @param stack The item stack to spawn.
     * @return      True if the item was spawned successfully, false otherwise.
     */
    public static boolean spawnItem(Level level, Vec3 pos, ItemStack stack)
    {
        return level.addFreshEntity(new ItemEntity(level, pos.x, pos.y, pos.z, stack));
    }

    /**
     * Spawns an item entity at the specified block position with a vertical offset in the given level.
     *
     * @param level   The level to spawn the item in.
     * @param pos     The block position to spawn the item at.
     * @param stack   The item stack to spawn.
     * @param yOffset The vertical offset to apply to the spawn position.
     * @return        True if the item was spawned successfully, false otherwise.
     */
    public static boolean spawnItem(Level level, BlockPos pos, ItemStack stack, double yOffset)
    {
        return level.addFreshEntity(
                new ItemEntity(
                        level,
                        pos.getX() + 0.5D,
                        pos.getY() + yOffset,
                        pos.getZ() + 0.5D,
                        stack
                )
        );
    }

    /**
     * Spawns an item entity at the specified block position in the given level, with a default vertical offset of 0.5D.
     *
     * @param level The level to spawn the item in.
     * @param pos   The block position to spawn the item at.
     * @param stack The item stack to spawn.
     * @return      True if the item was spawned successfully, false otherwise.
     */
    public static boolean spawnItem(Level level, BlockPos pos, ItemStack stack)
    {
        return spawnItem(level, pos, stack, 0.5D);
    }

    /**
     * Converts a quart position to a block position.
     * @see net.minecraft.core.QuartPos#toBlock(int)
     *
     * @param x the x-coordinate of the quart position.
     * @param y the y-coordinate of the quart position.
     * @param z the z-coordinate of the quart position.
     * @return  the corresponding block position.
     */
    public static BlockPos quartToBlock(int x, int y, int z)
    {
        return new BlockPos(x << 2, y << 2, z << 2);
    }

    /**
     * Rotates a VoxelShape by 90 degrees around the specified axis.
     *
     * @param direction the direction of rotation (must be NORTH, EAST, SOUTH, or WEST).
     * @param x1        the minimum x-coordinate of the shape.
     * @param y1        the minimum y-coordinate of the shape.
     * @param z1        the minimum z-coordinate of the shape.
     * @param x2        the maximum x-coordinate of the shape.
     * @param y2        the maximum y-coordinate of the shape.
     * @param z2        the maximum z-coordinate of the shape.
     * @return          the rotated shape.
     * @throws          IllegalArgumentException if the direction is not horizontal.
     */
    public static VoxelShape rotateShape(Direction direction, double x1, double y1, double z1, double x2, double y2, double z2)
    {
        return switch (direction)
        {
            case NORTH -> Block.box(x1, y1, z1, x2, y2, z2);
            case EAST -> Block.box(16 - z2, y1, x1, 16 - z1, y2, x2);
            case SOUTH -> Block.box(16 - x2, y1, 16 - z2, 16 - x1, y2, 16 - z1);
            case WEST -> Block.box(z1, y1, 16 - x2, z2, y2, 16 - x1);
            default -> throw new IllegalArgumentException("Not Horizontal!");
        };
    }

    /**
     * Computes the horizontal shapes for a given shape getter function.
     * Follows indexes for {@link Direction#get2DDataValue()}.
     *
     * @param shapeGetter a function that returns a shape for a given direction.
     * @return            an array of shapes for the four horizontal directions (SOUTH, WEST, NORTH, EAST).
     */
    public static VoxelShape[] computeHorizontalShapes(Function<Direction, VoxelShape> shapeGetter)
    {
        return new VoxelShape[] {
                shapeGetter.apply(Direction.SOUTH),
                shapeGetter.apply(Direction.WEST),
                shapeGetter.apply(Direction.NORTH),
                shapeGetter.apply(Direction.EAST)
        };
    }

    /**
     * Select N unique elements from a list, without having to shuffle the whole list.
     * This involves moving the selected elements to the end of the list. Note: this method will mutate the passed in list!
     * From <a href="https://stackoverflow.com/questions/4702036/take-n-random-elements-from-a-liste">Stack Overflow</a>
     *
     * @param list the list to sample from.
     * @param n    the number of elements to select.
     * @param r    a random source.
     * @param <T>  the type of the list.
     * @return     a list of n unique elements selected from the original list.
     * @throws     IllegalArgumentException if n is greater than the list size.
     */
    public static <T> List<T> uniqueRandomSample(List<T> list, int n, RandomSource r)
    {
        final int length = list.size();
        if (length < n)
        {
            throw new IllegalArgumentException("Cannot select n=" + n + " unique elements from a list of size " + length);
        }
        for (int i = length - 1; i >= length - n; i--)
        {
            Collections.swap(list, i, r.nextInt(i + 1));
        }
        return list.subList(length - n, length);
    }

    /**
     * Given a list containing {@code [a0, ... aN]} and an element {@code aN+1}, returns a new, immutable list containing {@code [a0, ... aN, aN+1]},
     * in the most efficient manner that we can manage (a single data copy). This takes advantage that {@link ImmutableList}, along with its
     * builder, will not create copies if the builder is sized perfectly.
     *
     * @param list    the list to append to.
     * @param element the element to append.
     * @param <T>     the type of the list.
     * @return        a new immutable list containing the original elements and the appended element.
     */
    public static <T> List<T> immutableAdd(List<T> list, T element)
    {
        return ImmutableList.<T>builderWithExpectedSize(list.size() + 1).addAll(list).add(element).build();
    }

    /**
     * Appends all elements from one list to another immutable list.
     *
     * @param list the list to append to.
     * @param others the list of elements to append.
     * @param <T> the type of the list.
     * @return a new immutable list containing the original elements and the appended elements.
     */
    public static <T> List<T> immutableAddAll(List<T> list, List<T> others)
    {
        return ImmutableList.<T>builderWithExpectedSize(list.size() + others.size())
                .addAll(list)
                .addAll(others)
                .build();
    }

    /**
     * Given a list containing {@code [a0, ... aN]} and an element {@code ai}, returns a new, immutable list containing {@code [a0, ... ai-1
     * , ai+1, ... aN]} in the most efficient manner (a single data copy).
     *
     * @param list    the list to remove from.
     * @param element the element to remove.
     * @param <T>     the type of the list.
     * @return        a new immutable list containing the original elements except the removed element.
     * @throws        IndexOutOfBoundsException if.
     */
    public static <T> List<T> immutableRemove(List<T> list, T element)
    {
        final ImmutableList.Builder<T> builder = ImmutableList.builderWithExpectedSize(list.size() - 1);
        for (final T t : list)
            if (t != element)
                builder.add(t);
        return builder.build();
    }

    /**
     * Given a list containing {@code [a0, ... ai, ... aN}, an element {@code bi}, and an index {@code i}, returns a new, immutable list
     * containing {@code [a0, ... bi, ... aN]} in the most efficient manner (a single data copy). The new list will contain the same
     * references as the original list – they're assumed to be immutable!
     *
     * @param list    the list to swap in.
     * @param element the new element to swap in.
     * @param index   the index of the element to swap out.
     * @param <T>     the type of the list.
     * @return        a new immutable list containing the original elements except the swapped element.
     */
    public static <T> List<T> immutableSwap(List<T> list, T element, int index)
    {
        final ImmutableList.Builder<T> builder = ImmutableList.builderWithExpectedSize(list.size());
        for (int i = 0; i < list.size(); i++)
            builder.add(i == index ? element : list.get(i));
        return builder.build();
    }

    /**
     * Creates a new immutable list containing {@code n} new, separate instances of {@code T} produced by the given {@code factory}. This is unlike
     * {@link Collections#nCopies(int, Object)} in that it produces separate instance, and consumes memory proportional to O(n). However, in
     * the event the underlying elements are interior mutable, this creates a safe to modify list.
     * @see Collections#nCopies(int, Object)
     *
     * @param n       the number of copies to create.
     * @param factory a supplier of the element to copy.
     * @param <T>     the type of the list.
     * @return        a list of n copies of the element.
     */
    public static <T> List<T> ImmutableCopies(int n, Supplier<T> factory)
    {
        final ImmutableList.Builder<T> builder = ImmutableList.builderWithExpectedSize(n);
        for (int i = 0; i < n; i++)
            builder.add(factory.get());
        return builder.build();
    }

    /**
     * Copies the contents of an inventory {@code inventory} into an immutable list builder.
     *
     * @param builder   the builder to copy into.
     * @param inventory the inventory to copy from.
     */
    public static void copyTo(ImmutableList.Builder<ItemStack> builder, IItemHandler inventory)
    {
        copyTo(builder, iterate(inventory));
    }

    /**
     * Copies the contents of an inventory {@code inventory} into an immutable list builder.
     *
     * @param builder the builder to copy into.
     * @param stacks  the stacks to copy from.
     */
    public static void copyTo(ImmutableList.Builder<ItemStack> builder, Iterable<ItemStack> stacks)
    {
        for (ItemStack stack : stacks)
            builder.add(stack.copy());
    }

    /**
     * Copies the contents of the inventory {@code inventory} into a list, clears the inventory, and returns the list.
     * @see #copyTo
     *
     * @param inventory the inventory to copy from.
     * @return          the copied list.
     */
    public static List<ItemStack> copyAndClear(IItemHandlerModifiable inventory)
    {
        final ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
        for (int slot = 0; slot < inventory.getSlots(); slot++)
        {
            builder.add(inventory.getStackInSlot(slot).copy());
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        }
        return builder.build();
    }

    /**
     * Copies the contents of the contents list {@code list} into the inventory {@code inventory}. This will copy the minimum of
     * the slot count of the inventory, and the content of the list. If the list is empty, nothing will be copied.
     *
     * @param list      the list of items to copy from
     * @param inventory the inventory to copy items into
     */
    public static void copyFrom(List<ItemStack> list, IItemHandlerModifiable inventory)
    {
        for (int i = 0; i < Math.min(list.size(), inventory.getSlots()); i++)
            inventory.setStackInSlot(1, list.get(i).copy());
    }

    /**
     * Executes the specified action without checking for exceptions, suppressing any Throwables that occur.
     * Use with caution, as this can lead to unexpected behavior if not properly handled.
     *
     * @param action the action to execute without checks
     * @param <T>    the return type of the action
     * @return       the result of the action, or null if an exception occurs
     */
    @SuppressWarnings("unchecked")
    public static <T> T uncheck(ThrowingSupplier<?> action)
    {
        try
        {
            return (T) action.get();
        }
        catch (Throwable e)
        {
            return throwAsUnchecked(e);
        }
    }

    /**
     * Executes the specified action without checking for exceptions, suppressing any Throwables that occur.
     * Use with caution, as this can lead to unexpected behavior if not properly handled.
     *
     * @param action the action to execute without checks
     */
    public static void uncheck(ThrowingRunnable action)
    {
        try
        {
            action.run();
        }
        catch (Throwable e)
        {
            throwAsUnchecked(e);
        }
    }

    /**
     * Checks if the given ItemStack matches the specified Item.
     *
     * @param stack the ItemStack to check
     * @param item  the Item to match
     * @return      true if the ItemStack matches the Item, false otherwise
     */
    public static boolean isItem(ItemStack stack, ItemLike item)
    {
        return stack.is(item.asItem());
    }

    /**
     * Checks if the given ItemStack matches the specified Tag.
     *
     * @param stack the ItemStack to check
     * @param tag   the Tag to match
     * @return      true if the ItemStack matches the Tag, false otherwise
     */
    public static boolean isItem(ItemStack stack, TagKey<Item> tag)
    {
        return stack.is(tag);
    }

    /**
     * Checks if the given Item matches the specified Tag.
     *
     * @param item the Item to check
     * @param tag  the Tag to match
     * @return     true if the Item matches the Tag, false otherwise
     */
    @SuppressWarnings("deprecation")
    public static boolean isItem(Item item,TagKey<Item> tag)
    {
        return item.builtInRegistryHolder().is(tag);
    }

    /**
     * Returns a random Item from the specified Tag.
     *
     * @param tag    the Tag to select from
     * @param random the RandomSource to use for selection
     * @return       a random Item from the Tag, or an empty Optional if the Tag is empty
     */
    public static Optional<Item> isItem(TagKey<Item> tag, RandomSource random)
    {
        return getRandomElement(BuiltInRegistries.ITEM, tag, random);
    }

    /**
     * Returns a Stream of all Items in the specified Tag.
     *
     * @param tag the Tag to select from
     * @return    a Stream of all Items in the Tag
     */
    public static Stream<Item> allItems(TagKey<Item> tag)
    {
        return BuiltInRegistries.ITEM.getOrCreateTag(tag).stream().map(Holder::value);
    }

    /**
     * Checks if the given BlockState matches the specified Block.
     *
     * @param block the BlockState to check
     * @param other the Block to match
     * @return      true if the BlockState matches the Block, false otherwise
     */
    public static boolean isBlock(BlockState block, Block other)
    {
        return block.is(other);
    }

    /**
     * Checks if the given BlockState matches the specified Tag.
     *
     * @param state the BlockState to check
     * @param tag   the Tag to match
     * @return      true if the BlockState matches the Tag, false otherwise
     */
    public static boolean isBlock(BlockState state, TagKey<Block> tag)
    {
        return state.is(tag);
    }

    /**
     * Checks if the given Block matches the specified Tag.
     *
     * @param block the Block to check
     * @param tag   the Tag to match
     * @return      true if the Block matches the Tag, false otherwise
     */
    @SuppressWarnings("deprecation")
    public static boolean isBlock(Block block, TagKey<Block> tag)
    {
        return block.builtInRegistryHolder().is(tag);
    }

    /**
     * Returns a random Block from the specified Tag.
     *
     * @param tag    the Tag to select from
     * @param random the RandomSource to use for selection
     * @return       a random Block from the Tag, or an empty Optional if the Tag is empty
     */
    public static Optional<Block> randomBlock(TagKey<Block> tag, RandomSource random)
    {
        return getRandomElement(BuiltInRegistries.BLOCK, tag, random);
    }

    /**
     * Returns a Stream of all Blocks in the specified Tag.
     *
     * @param tag the Tag to select from
     * @return    a Stream of all Blocks in the Tag
     */
    public static Stream<Block> allBlocks(TagKey<Block> tag)
    {
        return BuiltInRegistries.BLOCK.getOrCreateTag(tag).stream().map(Holder::value);
    }

    /**
     * Checks if the given FluidState matches the specified Tag.
     *
     * @param state the FluidState to check
     * @param tag   the Tag to match
     * @return      true if the FluidState matches the Tag, false otherwise
     */
    public static boolean isFluid(FluidState state, TagKey<Fluid> tag)
    {
        return state.is(tag);
    }

    /**
     * Checks if the given Fluid matches the specified Tag.
     *
     * @param fluid the Fluid to check
     * @param tag   the Tag to match
     * @return      true if the Fluid matches the Tag, false otherwise
     */
    @SuppressWarnings("deprecation")
    public static boolean isFluid(Fluid fluid, TagKey<Fluid> tag)
    {
        return fluid.is(tag);
    }

    /**
     * Checks if the given FluidState matches the specified Fluid.
     *
     * @param fluid the FluidState to check
     * @param other the Fluid to match
     * @return      true if the FluidState matches the Fluid, false otherwise
     */
    public static boolean isFluid(FluidState fluid, Fluid other)
    {
        return fluid.is(other);
    }

    /**
     * Returns a Stream of all Fluids in the specified Tag.
     *
     * @param tag the Tag to select from
     * @return    a Stream of all Fluids in the Tag
     */
    public static Stream<Fluid> allFluids(TagKey<Fluid> tag)
    {
        return BuiltInRegistries.FLUID.getOrCreateTag(tag).stream().map(Holder::value);
    }

    /**
     * Checks if the given Entity matches the specified Tag.
     *
     * @param entity the Entity to check
     * @param tag    the Tag to match
     * @return       true if the Entity matches the Tag, false otherwise
     */
    public static boolean isEntity(Entity entity, TagKey<EntityType<?>> tag)
    {
        return isEntity(entity.getType(), tag);
    }

    /**
     * Checks if the given EntityType matches the specified Tag.
     *
     * @param entity the EntityType to check
     * @param tag    the Tag to match
     * @return       true if the EntityType matches the Tag, false otherwise
     */
    public static boolean isEntity(EntityType<?> entity, TagKey<EntityType<?>> tag)
    {
        return entity.is(tag);
    }

    /**
     * Returns a random EntityType from the specified Tag.
     *
     * @param tag    the Tag to select from
     * @param random the RandomSource to use for selection
     * @return       a random EntityType from the Tag, or an empty Optional if the Tag is empty
     */
    public static Optional<EntityType<?>> randomEntity(TagKey<EntityType<?>> tag, RandomSource random)
    {
        return getRandomElement(BuiltInRegistries.ENTITY_TYPE, tag, random);
    }

    /**
     * Checks if the given DamageSource matches the specified Tag.
     *
     * @param source the DamageSource to check
     * @param tag    the Tag to match
     * @return       true if the DamageSource matches the Tag, false otherwise
     */
    public static boolean isDamageSource(DamageSource source, TagKey<DamageType> tag)
    {
        return source.is(tag);
    }

    /**
     * Returns a random element from the specified Registry and Tag.
     *
     * @param registry the Registry to select from
     * @param tag      the Tag to select from
     * @param random   the RandomSource to use for selection
     * @param <T>      the type of element in the Registry
     * @return         a random element from the Registry and Tag, or an empty Optional if the Tag is empty
     */
    private static <T> Optional<T> getRandomElement(Registry<T> registry, TagKey<T> tag, RandomSource random)
    {
        return registry.getTag(tag).flatMap(set -> set.getRandomElement(random)).map(Holder::value);
    }

    /**
     * This exists to fix a horrible case of vanilla seeding, which led to noticeable issues of feature clustering.
     * The key issue was that features with a chance placement, applied sequentially, would appear to generate on the same chunk much more often than was expected.
     * This was then identified as the problem by the lovely KaptainWutax <3. The following is an excerpt / paraphrase from our conversation:
     * <pre>
     * So you're running setSeed(n), setSeed(n + 1) and setSeed(n + 2) on the 3 structure respectively.
     * And n is something we can compute given a chunk and seed.
     * setSeed applies an xor on the lowest 35 bits and assigns that value internally
     * But like, since your seeds are like 1 apart
     * Even after the xor they're at worst 1 apart
     * You can convince yourself of that quite easily
     * So now nextFloat() does seed = 25214903917 * seed + 11 and returns (seed >> 24) / 2^24
     * Sooo lets see what the actual difference in seeds are between your 2 features in the worst case:
     * a = 25214903917, b = 11
     * So a * (seed + 1) + b = a * seed + b + a
     * As you can see the internal seed only varies by "a" amount
     * Now we can measure the effect that big number has no the upper bits since the seed is shifted
     * 25214903917/2^24 = 1502.92539101839
     * And that's by how much the upper 24 bits will vary
     * The effect on the next float are 1502 / 2^24 = 8.95261764526367e-5
     * Blam, so the first nextFloat() between setSeed(n) and setSeed(n + 1) is that distance apart ^
     * Which as you can see... isn't that far from 0
     * </pre>
     */
    public static void seedLargeFeatures(RandomSource random, long baseSeed, int index, int decoration)
    {
        random.setSeed(baseSeed);
        final long seed = (index * random.nextLong() * 203704237L) ^ (decoration * random.nextLong() * 758031792L) ^ baseSeed;
        random.setSeed(seed);
    }

    /**
     * Throws an exception as an unchecked exception.
     * This method is useful for rethrowing checked exceptions in places where only unchecked exceptions are allowed.
     *
     * @param exception The exception to throw.
     * @param <E>       The type of the exception.
     * @param <T>       A generic return type to facilitate its use in various contexts.
     * @throws E        The exception being thrown.
     */
    @SuppressWarnings("unchecked")
    public static <E extends Throwable, T> T throwAsUnchecked(Throwable exception) throws E
    {
        throw (E) exception;
    }
}
