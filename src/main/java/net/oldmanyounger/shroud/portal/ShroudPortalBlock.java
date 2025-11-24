package net.oldmanyounger.shroud.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Core portal block for the Shroud dimension. This mirrors vanilla Nether portal behaviour
 * but defers activation to custom logic and routes to the shroud:shroud dimension.
 */
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
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    /** Exposes the horizontal AXIS property on the block state container */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    /** Provides the appropriate collision shape based on the active axis */
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction.Axis axis = state.getValue(AXIS);
        return axis == Direction.Axis.Z ? Z_AXIS_SHAPE : X_AXIS_SHAPE;
    }

    /** Marks entities as being inside the portal so the Portal system can handle countdown and teleport */
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!entity.isPassenger() && !entity.isVehicle() && entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    /** Computes how long an entity must stand in the Shroud portal before teleporting */
    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        if (entity instanceof net.minecraft.server.level.ServerPlayer) {
            return 80;
        }
        return 300;
    }

    /**
     * Returns the dimension transition for entities using this portal.
     * This uses simple Overworld <-> Shroud routing with coordinate scaling and world border clamping.
     */
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

    /** Clamps a horizontal coordinate to the world border for safe teleportation */
    private static double clampToBorder(WorldBorder border, double coordinate) {
        double min = border.getMinX() + 16.0D;
        double max = border.getMaxX() - 16.0D;
        return Math.max(min, Math.min(max, coordinate));
    }
}
