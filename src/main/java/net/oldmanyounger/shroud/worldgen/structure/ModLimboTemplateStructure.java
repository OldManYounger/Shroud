package net.oldmanyounger.shroud.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.oldmanyounger.shroud.worldgen.structure.piece.ModLimboTemplatePiece;

import java.util.Optional;

/**
 * Template-backed structure intended to be placed on a chunk-grid (via a custom {@code StructurePlacement}).
 *
 * <p>This structure uses a world "anchor" concept that matches Structure Block placement behavior:</p>
 * <ul>
 *   <li>The anchor is the in-world position of the Structure Block (kept at a stable {@code (7,7)} inside the placement chunk).</li>
 *   <li>The template origin (NW/min corner) is computed as {@code anchorWorld + relativeOffset(rotation)}.</li>
 *   <li>Rotation-specific origin offsets are handled by {@link ModLimboTemplatePiece}.</li>
 * </ul>
 *
 * <p>This avoids template-size pivot math and instead mirrors offsets verified in-game using a Structure Block.</p>
 */
public class ModLimboTemplateStructure extends Structure {

    // Codec used by datapacks/registries to deserialize this structure + its custom fields
    public static final MapCodec<ModLimboTemplateStructure> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Structure.settingsCodec(instance),
                    ResourceLocation.CODEC.fieldOf("template").forGetter(s -> s.templateId),
                    Codec.INT.fieldOf("y").orElse(64).forGetter(s -> s.originY),
                    Codec.BOOL.fieldOf("random_rotation").orElse(false).forGetter(s -> s.randomRotation)
            ).apply(instance, ModLimboTemplateStructure::new)
    );

    // Template resource that this structure places (structure NBT id)
    private final ResourceLocation templateId;

    // Desired template origin Y (i.e., where the template's NW/min corner should land)
    private final int originY;

    // Whether to choose a random rotation at placement time
    private final boolean randomRotation;

    public ModLimboTemplateStructure(StructureSettings settings,
                                     ResourceLocation templateId,
                                     int originY,
                                     boolean randomRotation) {
        super(settings);
        this.templateId = templateId;
        this.originY = originY;
        this.randomRotation = randomRotation;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();

        // Anchor is fixed at (7,7) inside the placement chunk to match Structure Block testing
        int anchorX = chunkPos.getMinBlockX() + 7;
        int anchorZ = chunkPos.getMinBlockZ() + 7;

        // Pick rotation (either fixed NONE or random across all rotations)
        Rotation rotation = Rotation.NONE;
        if (this.randomRotation) {
            RandomSource random = context.random();
            Rotation[] rotations = Rotation.values();
            rotation = rotations[random.nextInt(rotations.length)];
        }

        // Structure Block-relative Y was verified as +1 from anchor -> template origin, so anchorY = originY - 1
        int anchorY = this.originY - 1;

        BlockPos anchorWorld = new BlockPos(anchorX, anchorY, anchorZ);
        Rotation finalRotation = rotation;

        // Use the anchor as the generation point; the piece computes the actual template origin from it
        return Optional.of(new GenerationStub(anchorWorld, builder ->
                builder.addPiece(new ModLimboTemplatePiece(
                        context.structureTemplateManager(),
                        this.templateId,
                        anchorWorld,
                        finalRotation
                ))
        ));
    }

    @Override
    public StructureType<?> type() {
        // Links this structure to its registered structure type
        return ModStructures.LIMBO_TEMPLATE.get();
    }
}