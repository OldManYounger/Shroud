package net.oldmanyounger.shroud.block.entity;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.oldmanyounger.shroud.ritual.recipe.RitualRecipe;
import org.jetbrains.annotations.Nullable;

/**
 * Core persistent state holder for the Corrupted Reliquary block.
 *
 * <p>This block entity owns the reliquary's internal one-item-slot inventory model and exposes
 * focused methods for manual insertion, dropped-item insertion, LIFO removal, and automation-facing
 * insertion-only container behavior. It intentionally avoids ritual execution logic so the block can
 * be implemented and validated independently first.
 *
 * <p>In the broader context of the project, this class is the foundation of ritual item intake,
 * serving as the canonical source of reliquary contents that future ritual validation and activation
 * systems will consume.
 */
public class ModCorruptedReliquaryBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity implements WorldlyContainer {

    // ==================================
    //  FIELDS
    // ==================================

    // Total number of one-item slots supported by the reliquary
    public static final int MAX_SLOTS = 32;

    // NBT key for stored slot items
    private static final String TAG_ITEMS = "Items";

    // NBT key for insertion order tracking
    private static final String TAG_INSERTION_ORDER = "InsertionOrder";

    // NBT key for ritual lock state
    private static final String TAG_RITUAL_LOCKED = "RitualLocked";

    // All slot indexes exposed for insertion checks by automation
    private static final int[] AUTOMATION_SLOTS = buildAutomationSlots();

    // Backing slot list where each slot holds at most one item
    private final NonNullList<ItemStack> items = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);

    // Insertion order stack used for shift-right-click LIFO removal
    private final IntArrayList insertionOrder = new IntArrayList();

    // Runtime lock state placeholder for future ritual integration
    private boolean ritualLocked = false;

    // ==================================
    //  CONSTRUCTOR
    // ==================================

    // Creates a new corrupted reliquary block entity
    public ModCorruptedReliquaryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CORRUPTED_RELIQUARY.get(), pos, state);
    }

    // ==================================
    //  INSERTION API
    // ==================================

    // Tries to insert exactly one item from the given stack
    public boolean tryInsertSingle(ItemStack sourceStack) {
        if (sourceStack.isEmpty()) return false;
        if (ritualLocked) return false;

        int slot = findFirstEmptySlot();
        if (slot < 0) return false;

        ItemStack inserted = sourceStack.copyWithCount(1);
        items.set(slot, inserted);
        insertionOrder.add(slot);
        markChangedAndSync();
        return true;
    }

    // Tries to insert as many items as possible from the given stack
    public int tryInsertAsMany(ItemStack sourceStack) {
        if (sourceStack.isEmpty()) return 0;
        if (ritualLocked) return 0;

        int inserted = 0;
        while (!sourceStack.isEmpty()) {
            boolean ok = tryInsertSingle(sourceStack);
            if (!ok) {
                break;
            }

            sourceStack.shrink(1);
            inserted++;
        }

        return inserted;
    }

    // Returns true if an insert is currently allowed
    public boolean canAcceptInsert() {
        if (ritualLocked) return false;
        return findFirstEmptySlot() >= 0;
    }

    // ==================================
    //  REMOVAL API
    // ==================================

    // Removes and returns the most recently inserted item
    public ItemStack popMostRecentItem() {
        if (ritualLocked) return ItemStack.EMPTY;

        while (!insertionOrder.isEmpty()) {
            int slot = insertionOrder.removeInt(insertionOrder.size() - 1);
            ItemStack stack = items.get(slot);
            if (!stack.isEmpty()) {
                items.set(slot, ItemStack.EMPTY);
                markChangedAndSync();
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    // ==================================
    //  READ API
    // ==================================

    // Returns the item stack currently stored in the given slot
    @Override
    public ItemStack getItem(int slot) {
        if (!isValidSlot(slot)) return ItemStack.EMPTY;
        return items.get(slot);
    }

    // Returns an immutable copy of all reliquary slots for rendering or matching
    public NonNullList<ItemStack> copyItems() {
        NonNullList<ItemStack> copy = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < MAX_SLOTS; i++) {
            ItemStack stack = items.get(i);
            copy.set(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        }
        return copy;
    }

    // Returns the number of occupied slots
    public int getOccupiedSlotCount() {
        int count = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    // ==================================
    //  LOCK STATE
    // ==================================

    // Returns true when reliquary interactions should be blocked
    public boolean isRitualLocked() {
        return ritualLocked;
    }

    // Sets the ritual lock state
    public void setRitualLocked(boolean ritualLocked) {
        if (this.ritualLocked == ritualLocked) return;
        this.ritualLocked = ritualLocked;
        markChangedAndSync();
    }

    // ==================================
    //  NETWORK SYNC
    // ==================================

    // Sends block entity update packets to clients for renderer-visible state updates
    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // Provides update tag data for client-side block entity synchronization
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, registries);
        return tag;
    }

    // Applies update tag data on client so renderer sees current inventory state
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        this.loadAdditional(tag, registries);
    }

    // ==================================
    //  PERSISTENCE
    // ==================================

    // Saves reliquary inventory and state to NBT
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
        tag.putIntArray(TAG_INSERTION_ORDER, this.insertionOrder.toIntArray());
        tag.putBoolean(TAG_RITUAL_LOCKED, this.ritualLocked);
    }

    // Loads reliquary inventory and state from NBT
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, this.items, registries);

        this.insertionOrder.clear();
        int[] order = tag.getIntArray(TAG_INSERTION_ORDER);
        for (int slot : order) {
            if (slot >= 0 && slot < MAX_SLOTS && !this.items.get(slot).isEmpty()) {
                this.insertionOrder.add(slot);
            }
        }

        this.ritualLocked = tag.getBoolean(TAG_RITUAL_LOCKED);
    }

    // ==================================
    //  CONTAINER API
    // ==================================

    // Returns total number of container slots
    @Override
    public int getContainerSize() {
        return MAX_SLOTS;
    }

    // Returns true if every slot is empty
    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // Removes up to the requested amount from a slot
    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (!isValidSlot(slot)) return ItemStack.EMPTY;
        if (amount <= 0) return ItemStack.EMPTY;
        if (ritualLocked) return ItemStack.EMPTY;

        ItemStack existing = items.get(slot);
        if (existing.isEmpty()) return ItemStack.EMPTY;

        ItemStack taken = existing.split(Math.min(amount, existing.getCount()));
        if (existing.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
        }

        markChangedAndSync();
        return taken;
    }

    // Removes the whole stack from a slot without additional validation
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (!isValidSlot(slot)) return ItemStack.EMPTY;
        ItemStack existing = items.get(slot);
        if (existing.isEmpty()) return ItemStack.EMPTY;

        items.set(slot, ItemStack.EMPTY);
        return existing;
    }

    // Sets the stack in a slot with one-item slot enforcement
    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!isValidSlot(slot)) return;
        if (ritualLocked) return;

        if (stack.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
            markChangedAndSync();
            return;
        }

        ItemStack single = stack.copyWithCount(1);
        items.set(slot, single);
        insertionOrder.add(slot);
        markChangedAndSync();
    }

    // Returns whether a player can still use this container
    @Override
    public boolean stillValid(Player player) {
        if (level == null) return false;
        if (level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    // Clears all stored items
    @Override
    public void clearContent() {
        for (int i = 0; i < MAX_SLOTS; i++) {
            items.set(i, ItemStack.EMPTY);
        }
        insertionOrder.clear();
        markChangedAndSync();
    }

    // Returns whether a stack may be placed in the given slot
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (!isValidSlot(slot)) return false;
        if (ritualLocked) return false;
        if (stack.isEmpty()) return false;
        return items.get(slot).isEmpty();
    }

    // Consumes one item unit per expanded requirement selector and returns true on full success
    public boolean consumeRequirements(java.util.List<RitualRecipe.ItemRequirement> requirements) {
        java.util.List<RitualRecipe.ItemRequirement> expanded = new java.util.ArrayList<>();
        for (RitualRecipe.ItemRequirement req : requirements) {
            int count = Math.max(1, req.count());
            for (int i = 0; i < count; i++) {
                expanded.add(req);
            }
        }

        int[] matchedSlots = new int[expanded.size()];
        java.util.Arrays.fill(matchedSlots, -1);

        boolean[] usedSlots = new boolean[MAX_SLOTS];
        boolean ok = matchRequirementRecursive(expanded, 0, usedSlots, matchedSlots);
        if (!ok) {
            return false;
        }

        for (int slot : matchedSlots) {
            if (slot >= 0 && slot < MAX_SLOTS) {
                items.set(slot, ItemStack.EMPTY);
            }
        }

        pruneInsertionOrderToExistingItems();
        markChangedAndSync();
        return true;
    }

    // Matches expanded selectors to unique occupied reliquary slots
    private boolean matchRequirementRecursive(java.util.List<RitualRecipe.ItemRequirement> expanded,
                                              int reqIndex,
                                              boolean[] usedSlots,
                                              int[] matchedSlots) {
        if (reqIndex >= expanded.size()) {
            return true;
        }

        RitualRecipe.ItemRequirement requirement = expanded.get(reqIndex);

        for (int slot = 0; slot < MAX_SLOTS; slot++) {
            if (usedSlots[slot]) continue;

            ItemStack stack = items.get(slot);
            if (stack.isEmpty()) continue;
            if (!requirement.matches(stack)) continue;

            usedSlots[slot] = true;
            matchedSlots[reqIndex] = slot;

            if (matchRequirementRecursive(expanded, reqIndex + 1, usedSlots, matchedSlots)) {
                return true;
            }

            usedSlots[slot] = false;
            matchedSlots[reqIndex] = -1;
        }

        return false;
    }

    // Removes stale insertion-order entries that now point to empty slots
    private void pruneInsertionOrderToExistingItems() {
        it.unimi.dsi.fastutil.ints.IntArrayList kept = new it.unimi.dsi.fastutil.ints.IntArrayList();

        for (int i = 0; i < insertionOrder.size(); i++) {
            int slot = insertionOrder.getInt(i);
            if (slot >= 0 && slot < MAX_SLOTS && !items.get(slot).isEmpty()) {
                kept.add(slot);
            }
        }

        insertionOrder.clear();
        insertionOrder.addAll(kept);
    }

    // ==================================
    //  WORLDLY CONTAINER
    // ==================================

    // Exposes all slots for sided automation insertion attempts
    @Override
    public int[] getSlotsForFace(Direction side) {
        return AUTOMATION_SLOTS;
    }

    // Allows sided insertion into empty slots only
    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return canPlaceItem(slot, stack);
    }

    // Blocks sided extraction from all faces
    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }

    // ==================================
    //  INTERNAL HELPERS
    // ==================================

    // Marks data dirty and pushes a full client update so renderer state refreshes after inventory changes
    private void markChangedAndSync() {
        super.setChanged();

        if (level == null || level.isClientSide) return;

        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
    }

    // Builds the static slot index list used for automation access
    private static int[] buildAutomationSlots() {
        int[] slots = new int[MAX_SLOTS];
        for (int i = 0; i < MAX_SLOTS; i++) {
            slots[i] = i;
        }
        return slots;
    }

    // Finds the first empty slot index or -1 if full
    private int findFirstEmptySlot() {
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (items.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    // Returns true when the slot index is valid
    private boolean isValidSlot(int slot) {
        return slot >= 0 && slot < MAX_SLOTS;
    }
}