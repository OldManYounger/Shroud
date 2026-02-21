package net.oldmanyounger.shroud.block.entity;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.oldmanyounger.shroud.block.ModBlocks;

public class ModProjectingLightBlockEntity extends BlockEntity {

    private static final int SPACING = 5;
    private static final int RECHECK_INTERVAL_TICKS = 40;

    private final LongSet projected = new LongOpenHashSet();
    private boolean rebuildQueued = true;
    private int tickCounter = 0;

    public ModProjectingLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LIMBO_FLUORESCENT_LIGHT.get(), pos, state);
    }

    public void markForRebuild() {
        this.rebuildQueued = true;
        setChanged();
    }

    public void serverTick() {
        if (level == null || level.isClientSide) return;

        tickCounter++;
        if (rebuildQueued || (tickCounter % RECHECK_INTERVAL_TICKS == 0)) {
            rebuildQueued = false;
            rebuildProjection(level);
        }

        long now = level.getGameTime();

        if (scheduledRebuildTick != -1L && now >= scheduledRebuildTick && now >= nextAllowedRebuildTick) {
            scheduledRebuildTick = -1L;
            nextAllowedRebuildTick = now + REBUILD_MIN_INTERVAL_TICKS;
            rebuildProjection(level);
        }
    }

    public void clearProjectedLights() {
        if (level == null || level.isClientSide) return;

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

            // Examine every block on the way down; stop at the first non-replaceable block.
            if (!stateAt.canBeReplaced()) {
                break;
            }

            // Place one helper light every 5 blocks downward (Y-5, Y-10, ...)
            if (offset % SPACING == 0) {
                if (!stateAt.is(ModBlocks.PROJECTED_LIGHT.get())) {
                    lvl.setBlock(cursor, ModBlocks.PROJECTED_LIGHT.get().defaultBlockState(), 3);
                }
                projected.add(cursor.asLong());
            }
        }

        setChanged();
    }

    private static final int REBUILD_DEBOUNCE_TICKS = 10;   // wait 0.5s after a change
    private static final int REBUILD_MIN_INTERVAL_TICKS = 40; // never rebuild more than once every 2s
    private long scheduledRebuildTick = -1L;
    private long nextAllowedRebuildTick = 0L;

    public void requestRebuildDebounced() {
        if (level == null || level.isClientSide) return;

        long now = level.getGameTime();
        long target = now + REBUILD_DEBOUNCE_TICKS;

        // keep pushing it out slightly so bursts coalesce into one rebuild
        if (scheduledRebuildTick < target) {
            scheduledRebuildTick = target;
            setChanged();
        }
    }
}