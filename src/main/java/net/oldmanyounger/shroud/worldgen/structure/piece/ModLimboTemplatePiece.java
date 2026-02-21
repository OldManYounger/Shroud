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
 * Template-backed structure piece that places a single {@code .nbt} structure using a fixed anchor point.
 *
 * <p><b>Core idea</b></p>
 * <ul>
 *   <li>{@code anchorWorld} is the in-world position that represents where the Structure Block anchor was during testing.</li>
 *   <li>The template placement origin (the template's NW/min corner) is computed by applying a rotation-specific offset:</li>
 * </ul>
 *
 * <pre>{@code
 * originWorld = anchorWorld + relativePosForRotation(rotation)
 * }</pre>
 *
 * <p><b>Rotation offsets</b></p>
 * <p>
 * Offsets are derived from Structure Block "relative position" values recorded during testing:
 * </p>
 * <ul>
 *   <li>{@link Rotation#NONE}: {@code (-23, 1, -23)}</li>
 *   <li>{@link Rotation#CLOCKWISE_90}: {@code (24, 1, -23)}</li>
 *   <li>{@link Rotation#CLOCKWISE_180} / {@link Rotation#COUNTERCLOCKWISE_90}: inferred for a {@code 48x48} footprint
 *       to keep the anchor stationary.</li>
 * </ul>
 *
 * <p>
 * This approach avoids template-size pivot math and relies on vanilla template origin semantics.
 * </p>
 */
public class ModLimboTemplatePiece extends TemplateStructurePiece {

    // NBT keys used to persist the placed template and rotation.
    private static final String TAG_TEMPLATE = "Template";
    private static final String TAG_ROTATION = "Rotation";

    // Template id used for saving/loading (and for debugging in NBT)
    private final ResourceLocation templateId;

    // Rotation used when placing the template
    private final Rotation rotation;

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

    // Constructor used when loading this piece from disk (position is restored by TemplateStructurePiece)
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

    // Builds the placement settings for this template (no mirroring, rotation applied)
    private static StructurePlaceSettings makeSettings(Rotation rotation) {
        return new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(rotation);
    }

    // Returns the Structure Block-tested relative origin offset for a given rotation
    private static BlockPos relativePosForRotation(Rotation rotation) {
        return switch (rotation) {
            case NONE -> new BlockPos(-23, 1, -23);
            case CLOCKWISE_90 -> new BlockPos(24, 1, -23);
            case CLOCKWISE_180 -> new BlockPos(24, 1, 24);
            case COUNTERCLOCKWISE_90 -> new BlockPos(-23, 1, 24);
        };
    }

    // Converts the stable world anchor into the template origin used by vanilla template placement
    private static BlockPos computeOriginFromAnchor(BlockPos anchorWorld, Rotation rotation) {
        return anchorWorld.offset(relativePosForRotation(rotation));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);

        // Persist enough info to reconstruct this piece on world load.
        tag.putString(TAG_TEMPLATE, this.templateId.toString());
        tag.putString(TAG_ROTATION, this.rotation.name());
    }

    @Override
    protected void handleDataMarker(
            String marker,
            BlockPos pos,
            ServerLevelAccessor level,
            RandomSource random,
            BoundingBox box
    ) {
        // Optional hook for structure block data markers (currently unused)
    }
}