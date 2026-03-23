package net.oldmanyounger.shroud.block.entity;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * Block entity that manages a chain of invisible projected light blocks beneath
 * a visible Limbo fluorescent light source.
 *
 * <p>Rather than relying only on a single block's built-in light level, this
 * block entity periodically scans downward from its own position and places
 * helper light blocks at fixed intervals through open space. This allows a
 * single visible fixture to extend illumination farther into tall rooms, shafts,
 * or atmospheric spaces without requiring multiple visible light sources.
 *
 * <p>In the broader context of the project, this class supports Shroud's custom
 * environmental lighting system by letting decorative fixtures control hidden
 * technical light placement dynamically, while also cleaning up those helper
 * blocks when the source is removed or when the surrounding area changes.
 */
public class ModProjectingLightBlockEntity extends BlockEntity {

    // Number of vertical blocks between projected helper lights
    private static final int SPACING = 5;

    // How often the block entity performs a periodic validation rebuild
    private static final int RECHECK_INTERVAL_TICKS = 40;

    // Delay before a requested rebuild is actually allowed to run
    private static final int REBUILD_DEBOUNCE_TICKS = 10;

    // Minimum spacing between completed rebuilds to avoid excessive updates
    private static final int REBUILD_MIN_INTERVAL_TICKS = 40;

    // Packed positions of helper light blocks currently owned by this source
    private final LongSet projected = new LongOpenHashSet();

    // Tracks whether a full rebuild should happen on the next eligible tick
    private boolean rebuildQueued = true;

    // Simple tick counter used for periodic validation passes
    private int tickCounter = 0;

    // Game-time tick when a debounced rebuild should occur
    private long scheduledRebuildTick = -1L;

    // Earliest game-time tick when another rebuild is allowed to run
    private long nextAllowedRebuildTick = 0L;

    // Creates the projecting light block entity for the Limbo fluorescent light block
    public ModProjectingLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LIMBO_FLUORESCENT_LIGHT.get(), pos, state);
    }

    // Marks this block entity so its helper light projection will be rebuilt
    public void markForRebuild() {
        this.rebuildQueued = true;
        setChanged();
    }

    // Requests a delayed rebuild so bursts of neighbor updates collapse into a single projection refresh
    public void requestRebuildDebounced() {
        if (level == null || level.isClientSide) return;

        long now = level.getGameTime();
        long target = now + REBUILD_DEBOUNCE_TICKS;

        // Keep pushing the rebuild outward slightly so clustered updates merge together
        if (scheduledRebuildTick < target) {
            scheduledRebuildTick = target;
            setChanged();
        }
    }

    // Server-side tick entry point used to handle periodic checks and scheduled rebuilds
    public void serverTick() {
        if (level == null || level.isClientSide) return;

        tickCounter++;

        // Rebuild immediately if flagged, or periodically revalidate on a fixed interval
        if (rebuildQueued || (tickCounter % RECHECK_INTERVAL_TICKS == 0)) {
            rebuildQueued = false;
            rebuildProjection(level);
        }

        long now = level.getGameTime();

        // Run the delayed rebuild once its debounce and minimum interval requirements are satisfied
        if (scheduledRebuildTick != -1L && now >= scheduledRebuildTick && now >= nextAllowedRebuildTick) {
            scheduledRebuildTick = -1L;
            nextAllowedRebuildTick = now + REBUILD_MIN_INTERVAL_TICKS;
            rebuildProjection(level);
        }
    }

    // Removes all helper light blocks currently tracked by this block entity
    public void clearProjectedLights() {
        if (level == null || level.isClientSide) return;

        // Remove only helper lights that still match the projected-light block type
        for (long packed : projected) {
            BlockPos p = BlockPos.of(packed);
            BlockState st = level.getBlockState(p);

            if (st.is(ModBlocks.PROJECTED_LIGHT.get())) {
                level.removeBlock(p, false);
            }
        }

        projected.clear();
        setChanged();
    }

    // Rebuilds the downward light projection by clearing old helper lights and placing new ones in open space
    private void rebuildProjection(Level lvl) {
        clearProjectedLights();

        int minY = lvl.getMinBuildHeight();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());

        int offset = 0;
        while (true) {
            offset++;

            int nextY = worldPosition.getY() - offset;
            if (nextY < minY) break;

            cursor.set(worldPosition.getX(), nextY, worldPosition.getZ());
            BlockState stateAt = lvl.getBlockState(cursor);

            // Stop projecting downward once a non-replaceable block is encountered
            if (!stateAt.canBeReplaced()) {
                break;
            }

            // Place a helper light every configured spacing interval below the source
            if (offset % SPACING == 0) {
                if (!stateAt.is(ModBlocks.PROJECTED_LIGHT.get())) {
                    lvl.setBlock(cursor, ModBlocks.PROJECTED_LIGHT.get().defaultBlockState(), 3);
                }

                projected.add(cursor.asLong());
            }
        }

        setChanged();
    }
}