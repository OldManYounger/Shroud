package net.oldmanyounger.shroud.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Implements the Shroud portal block and its cross-dimension travel behavior.
 *
 * <p>This block defines portal state and collision shape, tracks entity portal entry,
 * validates frame integrity, and computes destination transitions between Overworld and
 * the custom Shroud dimension.
 *
 * <p>In the broader context of the project, this class is part of Shroud's dimension
 * travel framework that connects world interaction, portal construction, and teleport
 * routing into a consistent runtime system.
 */
public class ShroudPortalBlock extends Block implements Portal {

    // ==================================
    //  FIELDS
    // ==================================

    // Horizontal portal axis property
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    // Collision shape for X-axis portal planes
    protected static final VoxelShape X_AXIS_SHAPE = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);

    // Collision shape for Z-axis portal planes
    protected static final VoxelShape Z_AXIS_SHAPE = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    // ==================================
    //  CONSTRUCTOR
    // ==================================

    // Creates the portal block and initializes default axis state
    public ShroudPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    // ==================================
    //  BLOCK STATE / SHAPE
    // ==================================

    // Registers supported block state properties
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    // Returns collision shape based on stored portal axis
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction.Axis axis = state.getValue(AXIS);
        return axis == Direction.Axis.Z ? Z_AXIS_SHAPE : X_AXIS_SHAPE;
    }

    // ==================================
    //  PORTAL ENTRY / TIMING
    // ==================================

    // Marks valid entities as being inside this portal block
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!entity.isPassenger() && !entity.isVehicle() && entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    // Returns portal warmup duration before teleport for this entity type
    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        if (entity instanceof net.minecraft.server.level.ServerPlayer) {
            return 80;
        }
        return 300;
    }

    // ==================================
    //  FRAME VALIDATION
    // ==================================

    // Removes portal block when surrounding frame is no longer valid
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);

        if (level.isClientSide()) {
            return;
        }

        if (ShroudPortalForcer.isBuildingPortal()) {
            return;
        }

        BlockState neighborState = level.getBlockState(fromPos);

        boolean neighborIsFrame =
                neighborState.is(Blocks.DEEPSLATE_BRICKS)
                        || neighborState.is(Blocks.REINFORCED_DEEPSLATE);

        if (neighborIsFrame) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        Direction.Axis axis = state.getValue(AXIS);

        if (!ShroudPortalHelper.isValidExistingPortal(serverLevel, pos, axis)) {
            level.removeBlock(pos, false);
        }
    }

    // ==================================
    //  DESTINATION ROUTING
    // ==================================

    // Computes destination transition when an entity uses this portal
    @Nullable
    @Override
    public DimensionTransition getPortalDestination(ServerLevel currentLevel, Entity entity, BlockPos portalPos) {
        ResourceKey<Level> currentDimension = currentLevel.dimension();
        ResourceKey<Level> targetDimensionKey = ShroudDimensions.getTargetDimension(currentDimension);

        if (targetDimensionKey == null) {
            return null;
        }

        ServerLevel targetLevel = currentLevel.getServer().getLevel(targetDimensionKey);
        if (targetLevel == null) {
            return null;
        }

        WorldBorder worldBorder = targetLevel.getWorldBorder();

        DimensionType currentType = currentLevel.dimensionType();
        DimensionType targetType = targetLevel.dimensionType();

        double scale = DimensionType.getTeleportationScale(currentType, targetType);

        Vec3 entityPos = entity.position();
        double scaledX = clampToBorder(worldBorder, entityPos.x * scale);
        double scaledZ = clampToBorder(worldBorder, entityPos.z * scale);

        BlockPos targetBasePos = new BlockPos(
                (int) Math.floor(scaledX),
                portalPos.getY(),
                (int) Math.floor(scaledZ)
        );

        Direction.Axis axis = currentLevel
                .getBlockState(portalPos)
                .getOptionalValue(AXIS)
                .orElse(Direction.Axis.X);

        BlockPos exitPortalPos = ShroudPortalForcer.createOrFindPortal(targetLevel, targetBasePos, axis);

        Vec3 exitPosition = Vec3.atBottomCenterOf(exitPortalPos).add(0.0D, 0.5D, 0.0D);
        Vec3 exitVelocity = entity.getDeltaMovement();

        return new DimensionTransition(
                targetLevel,
                exitPosition,
                exitVelocity,
                entity.getYRot(),
                entity.getXRot(),
                ShroudPortalForcer.PLAY_TRAVEL_SOUND
        );
    }

    // ==================================
    //  HELPERS
    // ==================================

    // Clamps horizontal coordinates away from world border edges
    private static double clampToBorder(WorldBorder border, double coordinate) {
        double min = border.getMinX() + 16.0D;
        double max = border.getMaxX() - 16.0D;
        return Math.max(min, Math.min(max, coordinate));
    }
}