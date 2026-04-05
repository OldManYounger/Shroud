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
 * Defines a template-backed Limbo structure with anchor-based placement.
 *
 * <p>This structure keeps a stable in-chunk anchor position and delegates
 * rotation-aware origin offset handling to the template piece implementation.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * structure generation layer that drives deterministic placement of Limbo
 * template rooms through data-driven structure settings.
 */
public class ModLimboTemplateStructure extends Structure {

    // Codec used for datapack and registry deserialization
    public static final MapCodec<ModLimboTemplateStructure> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Structure.settingsCodec(instance),
                    ResourceLocation.CODEC.fieldOf("template").forGetter(s -> s.templateId),
                    Codec.INT.fieldOf("y").orElse(64).forGetter(s -> s.originY),
                    Codec.BOOL.fieldOf("random_rotation").orElse(false).forGetter(s -> s.randomRotation)
            ).apply(instance, ModLimboTemplateStructure::new)
    );

    // Template resource id to place
    private final ResourceLocation templateId;

    // Desired template origin Y level
    private final int originY;

    // Enables random rotation selection when true
    private final boolean randomRotation;

    // Creates the structure with template and placement settings
    public ModLimboTemplateStructure(StructureSettings settings,
                                     ResourceLocation templateId,
                                     int originY,
                                     boolean randomRotation) {
        super(settings);
        this.templateId = templateId;
        this.originY = originY;
        this.randomRotation = randomRotation;
    }

    // Resolves generation point and emits one template piece for this chunk placement
    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();

        // Uses stable anchor inside chunk to mirror structure-block testing coordinates
        int anchorX = chunkPos.getMinBlockX() + 7;
        int anchorZ = chunkPos.getMinBlockZ() + 7;

        // Chooses fixed or random rotation based on structure config
        Rotation rotation = Rotation.NONE;
        if (this.randomRotation) {
            RandomSource random = context.random();
            Rotation[] rotations = Rotation.values();
            rotation = rotations[random.nextInt(rotations.length)];
        }

        // Converts configured origin Y to anchor Y expected by piece offsets
        int anchorY = this.originY - 1;

        BlockPos anchorWorld = new BlockPos(anchorX, anchorY, anchorZ);
        Rotation finalRotation = rotation;

        return Optional.of(new GenerationStub(anchorWorld, builder ->
                builder.addPiece(new ModLimboTemplatePiece(
                        context.structureTemplateManager(),
                        this.templateId,
                        anchorWorld,
                        finalRotation
                ))
        ));
    }

    // Returns registered structure type for this structure class
    @Override
    public StructureType<?> type() {
        return ModStructures.LIMBO_TEMPLATE.get();
    }
}