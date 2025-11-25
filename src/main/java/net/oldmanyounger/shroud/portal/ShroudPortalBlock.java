package net.oldmanyounger.shroud.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/** Core portal block for the Shroud dimension that mirrors Nether portal behaviour and routes to the Shroud dimension. */
public class ShroudPortalBlock extends Block implements Portal {

    /** Horizontal axis for the portal plane, matching NetherPortalBlock behaviour */
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    /** Collision shape for X-axis aligned portals */
    protected static final VoxelShape X_AXIS_SHAPE = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);

    /** Collision shape for Z-axis aligned portals */
    protected static final VoxelShape Z_AXIS_SHAPE = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    /** Creates a new Shroud portal block with the given properties */
    public ShroudPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        // Initialize default state so placed blocks have a consistent portal axis
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    /** Exposes the horizontal AXIS property on the block state container */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // Register the AXIS property so it can be stored and read from BlockState
        builder.add(AXIS);
    }

    /** Provides the appropriate collision shape based on the active axis */
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Read the current axis from the block state
        Direction.Axis axis = state.getValue(AXIS);
        // Choose between the X-aligned or Z-aligned portal shape
        return axis == Direction.Axis.Z ? Z_AXIS_SHAPE : X_AXIS_SHAPE;
    }

    /** Marks entities as being inside the portal so the Portal system can handle countdown and teleport */
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        // Disallow passengers, vehicles, and entities not permitted to use portals
        if (!entity.isPassenger() && !entity.isVehicle() && entity.canUsePortal(false)) {
            // Notify the entity that it is inside this portal at this position
            entity.setAsInsidePortal(this, pos);
        }
    }

    /** Computes how long an entity must stand in the Shroud portal before teleporting */
    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        // Use a shorter delay for server players to keep the portal snappy
        if (entity instanceof net.minecraft.server.level.ServerPlayer) {
            return 80;
        }
        // Use a longer delay for non-player entities to reduce accidental transfers
        return 300;
    }

    /** Breaks the portal when its supporting frame is no longer valid */
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        // Preserve default behaviour
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);

        if (level.isClientSide()) {
            return;
        }

        // Skip validation entirely while a portal is being constructed by ShroudPortalForcer
        if (ShroudPortalForcer.isBuildingPortal()) {
            return;
        }

        // Only care when a *frame* block near this portal has been removed or changed
        BlockState neighborState = level.getBlockState(fromPos);

        boolean neighborIsFrame =
                neighborState.is(Blocks.DEEPSLATE_BRICKS)
                        || neighborState.is(Blocks.REINFORCED_DEEPSLATE);

        // If the neighbor is still a frame block (we're building / updating the frame),
        // don't run expensive validation or delete the portal.
        if (neighborIsFrame) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        Direction.Axis axis = state.getValue(AXIS);

        // Now we know some neighbor is no longer a frame block (air, something else, etc.),
        // so check if this portal is still part of a valid frame.
        if (!ShroudPortalHelper.isValidExistingPortal(serverLevel, pos, axis)) {
            level.removeBlock(pos, false);
        }
    }



    /** Returns the dimension transition for entities using this portal, including Overworld <-> Shroud routing,
        coordinate scaling, and world border clamping. */
    @Nullable
    @Override
    public DimensionTransition getPortalDestination(ServerLevel currentLevel, Entity entity, BlockPos portalPos) {
        // Identify the current dimension key
        ResourceKey<Level> currentDimension = currentLevel.dimension();
        // Compute which target dimension this portal should lead to
        ResourceKey<Level> targetDimensionKey = ShroudDimensions.getTargetDimension(currentDimension);

        // Abort if there is no configured target for this dimension
        if (targetDimensionKey == null) {
            return null;
        }

        // Resolve the target ServerLevel from the server instance
        ServerLevel targetLevel = currentLevel.getServer().getLevel(targetDimensionKey);
        // Abort safely if the target dimension is not loaded or available
        if (targetLevel == null) {
            return null;
        }

        // Use the target dimension's world border to clamp horizontal coordinates
        WorldBorder worldBorder = targetLevel.getWorldBorder();

        // Read dimension types to compute coordinate scaling
        DimensionType currentType = currentLevel.dimensionType();
        DimensionType targetType = targetLevel.dimensionType();

        // Calculate the Nether-style teleportation scale between dimension types
        double scale = DimensionType.getTeleportationScale(currentType, targetType);

        // Read the entity's current position for scaling and clamping
        Vec3 entityPos = entity.position();
        // Scale the X/Z coordinates by the dimension scale
        double scaledX = clampToBorder(worldBorder, entityPos.x * scale);
        double scaledZ = clampToBorder(worldBorder, entityPos.z * scale);

        // Use the scaled X/Z and the portal's Y as the base target position
        BlockPos targetBasePos = new BlockPos(
                (int) Math.floor(scaledX),
                portalPos.getY(),
                (int) Math.floor(scaledZ)
        );

        // Determine the portal axis at the current position, defaulting to X if missing
        Direction.Axis axis = currentLevel
                .getBlockState(portalPos)
                .getOptionalValue(AXIS)
                .orElse(Direction.Axis.X);

        // Create or locate a corresponding portal at the scaled coordinates in the target dimension
        BlockPos exitPortalPos = ShroudPortalForcer.createOrFindPortal(targetLevel, targetBasePos, axis);

        // Center the exit position on the portal block and raise slightly above ground
        Vec3 exitPosition = Vec3.atBottomCenterOf(exitPortalPos).add(0.0D, 0.5D, 0.0D);
        // Preserve the entity's current motion when transitioning
        Vec3 exitVelocity = entity.getDeltaMovement();

        // Build the final dimension transition object that the portal system will execute
        return new DimensionTransition(
                targetLevel,
                exitPosition,
                exitVelocity,
                entity.getYRot(),
                entity.getXRot(),
                ShroudPortalForcer.PLAY_TRAVEL_SOUND
        );
    }

    /** Clamps a horizontal coordinate to the world border for safe teleportation */
    private static double clampToBorder(WorldBorder border, double coordinate) {
        // Add an inner margin to avoid teleporting right at the world border
        double min = border.getMinX() + 16.0D;
        double max = border.getMaxX() - 16.0D;
        // Clamp the coordinate between the inner border limits
        return Math.max(min, Math.min(max, coordinate));
    }
}
