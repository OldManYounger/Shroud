package net.oldmanyounger.shroud.worldgen.structure.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.oldmanyounger.shroud.worldgen.structure.ModStructurePlacements;

import java.util.Optional;

/**
 * Implements structure placement snapped to a fixed chunk grid.
 *
 * <p>This placement allows structure attempts only at chunk coordinates that match
 * configured spacing and offset modulo conditions on both X and Z axes.
 *
 * <p>In the broader context of the project, this class is part of Shroud's structure
 * distribution layer that controls deterministic large-scale placement patterns for
 * custom structures.
 */
public class ModGridStructurePlacement extends StructurePlacement {

    // Codec used by datapack and registry deserialization for this placement type
    public static final MapCodec<ModGridStructurePlacement> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            StructurePlacement.placementCodec(instance).and(instance.group(
                    Codec.INT.fieldOf("spacing").forGetter(p -> p.spacing),
                    Codec.INT.fieldOf("offset").forGetter(p -> p.offset)
            )).apply(instance, ModGridStructurePlacement::new)
    );

    // Convenience full codec wrapper
    public static final Codec<ModGridStructurePlacement> CODEC = MAP_CODEC.codec();

    // Grid cell size in chunks
    private final int spacing;

    // Eligible chunk index within each grid cell
    private final int offset;

    // Full constructor used by codec-driven deserialization
    public ModGridStructurePlacement(
            Vec3i locateOffset,
            FrequencyReductionMethod frequencyReductionMethod,
            float frequency,
            int salt,
            Optional<ExclusionZone> exclusionZone,
            int spacing,
            int offset
    ) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
        this.spacing = spacing;
        this.offset = offset;
    }

    // Convenience constructor for simple code usage with default base placement settings
    public ModGridStructurePlacement(int spacing, int offset) {
        this(Vec3i.ZERO, FrequencyReductionMethod.DEFAULT, 1.0F, 0, Optional.empty(), spacing, offset);
    }

    // Returns true when chunk matches configured grid offset on both axes
    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        return Math.floorMod(chunkX, this.spacing) == this.offset
                && Math.floorMod(chunkZ, this.spacing) == this.offset;
    }

    // Returns registered placement type for this custom placement implementation
    @Override
    public StructurePlacementType<?> type() {
        return ModStructurePlacements.GRID.get();
    }
}