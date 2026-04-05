package net.oldmanyounger.shroud.worldgen.structure.piece;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.oldmanyounger.shroud.worldgen.structure.ModStructurePieces;

/**
 * Places a Limbo template structure piece using a stable anchor-based origin strategy.
 *
 * <p>This piece computes template origin from a fixed in-world anchor plus
 * rotation-specific offsets captured from Structure Block testing, allowing
 * consistent placement without dynamic pivot-size calculations.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * structure generation system that ensures Limbo room templates place reliably
 * across rotations and save/load cycles.
 */
public class ModLimboTemplatePiece extends TemplateStructurePiece {

    // NBT key for persisted template identifier
    private static final String TAG_TEMPLATE = "Template";

    // NBT key for persisted rotation enum name
    private static final String TAG_ROTATION = "Rotation";

    // Template id persisted and used for reconstruction
    private final ResourceLocation templateId;

    // Rotation applied during placement
    private final Rotation rotation;

    // Creates a new template piece from anchor world position and rotation
    public ModLimboTemplatePiece(
            StructureTemplateManager templateManager,
            ResourceLocation templateId,
            BlockPos anchorWorld,
            Rotation rotation
    ) {
        super(
                ModStructurePieces.LIMBO_TEMPLATE_PIECE.get(),
                0,
                templateManager,
                templateId,
                templateId.toString(),
                makeSettings(rotation),
                computeOriginFromAnchor(anchorWorld, rotation)
        );

        this.templateId = templateId;
        this.rotation = rotation;
    }

    // Reconstructs this template piece from saved NBT data
    public ModLimboTemplatePiece(StructureTemplateManager templateManager, CompoundTag tag) {
        super(
                ModStructurePieces.LIMBO_TEMPLATE_PIECE.get(),
                tag,
                templateManager,
                (ResourceLocation id) -> makeSettings(Rotation.valueOf(tag.getString(TAG_ROTATION)))
        );

        this.templateId = ResourceLocation.parse(tag.getString(TAG_TEMPLATE));
        this.rotation = Rotation.valueOf(tag.getString(TAG_ROTATION));
    }

    // Builds placement settings with no mirror and selected rotation
    private static StructurePlaceSettings makeSettings(Rotation rotation) {
        return new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(rotation);
    }

    // Returns tested relative origin offsets per rotation
    private static BlockPos relativePosForRotation(Rotation rotation) {
        return switch (rotation) {
            case NONE -> new BlockPos(-23, 1, -23);
            case CLOCKWISE_90 -> new BlockPos(24, 1, -23);
            case CLOCKWISE_180 -> new BlockPos(24, 1, 24);
            case COUNTERCLOCKWISE_90 -> new BlockPos(-23, 1, 24);
        };
    }

    // Computes template origin from stable anchor and rotation-specific offset
    private static BlockPos computeOriginFromAnchor(BlockPos anchorWorld, Rotation rotation) {
        return anchorWorld.offset(relativePosForRotation(rotation));
    }

    // Persists template id and rotation for chunk save data
    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);

        tag.putString(TAG_TEMPLATE, this.templateId.toString());
        tag.putString(TAG_ROTATION, this.rotation.name());
    }

    // Handles structure data markers if present in template data
    @Override
    protected void handleDataMarker(
            String marker,
            BlockPos pos,
            ServerLevelAccessor level,
            RandomSource random,
            BoundingBox box
    ) {
        // Marker hook currently unused
    }
}