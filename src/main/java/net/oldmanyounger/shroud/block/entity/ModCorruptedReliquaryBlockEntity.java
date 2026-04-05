package net.oldmanyounger.shroud.block.entity;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Core persistent state holder for the Corrupted Reliquary block.
 *
 * <p>This block entity owns the reliquary's internal one-item-slot inventory model and exposes
 * focused methods for manual insertion, LIFO removal, and read-only slot inspection. It intentionally
 * avoids ritual execution logic so the block can be implemented and validated independently first.
 *
 * <p>In the broader context of the project, this class is the foundation of ritual item intake,
 * serving as the canonical source of reliquary contents that future ritual validation and activation
 * systems will consume.
 */
public class ModCorruptedReliquaryBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity {

    // ==================================
    //  FIELDS
    // ==================================

    // Total number of one-item slots supported by the reliquary
    public static final int MAX_SLOTS = 64;

    // Backing slot list where each slot holds at most one item
    private final NonNullList<ItemStack> items = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);

    // Insertion order stack used for shift-right-click LIFO removal
    private final IntArrayList insertionOrder = new IntArrayList();

    // Runtime lock state placeholder for future ritual integration
    private boolean ritualLocked = false;

    // NBT key for stored slot items
    private static final String TAG_ITEMS = "Items";

    // NBT key for insertion order tracking
    private static final String TAG_INSERTION_ORDER = "InsertionOrder";

    // NBT key for ritual lock state
    private static final String TAG_RITUAL_LOCKED = "RitualLocked";

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
        setChanged();
        return true;
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
                setChanged();
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    // ==================================
    //  READ API
    // ==================================

    // Returns the item stack currently stored in the given slot
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

    // Returns true if every slot is empty
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
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
        setChanged();
    }

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
    //  INTERNAL HELPERS
    // ==================================

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