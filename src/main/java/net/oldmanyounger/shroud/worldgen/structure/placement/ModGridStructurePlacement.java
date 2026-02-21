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
 * Structure placement that snaps generation to a fixed chunk grid.
 *
 * <p>Parameters:</p>
 * <ul>
 *   <li>{@code spacing}: size of the grid cell in chunks (e.g., {@code 3} means a 3x3 chunk cell).</li>
 *   <li>{@code offset}: which chunk within that cell is eligible (e.g., {@code 1} is the center when {@code spacing=3}).</li>
 * </ul>
 *
 * <p>A chunk is a valid placement chunk when both X and Z satisfy:</p>
 * <pre>{@code
 * floorMod(chunkCoord, spacing) == offset
 * }</pre>
 */
public class ModGridStructurePlacement extends StructurePlacement {

    // Primary codec used by the structure placement registry/datapack system
    public static final MapCodec<ModGridStructurePlacement> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            StructurePlacement.placementCodec(instance).and(instance.group(
                    Codec.INT.fieldOf("spacing").forGetter(p -> p.spacing),
                    Codec.INT.fieldOf("offset").forGetter(p -> p.offset)
            )).apply(instance, ModGridStructurePlacement::new)
    );

    // Convenience wrapper codec
    public static final Codec<ModGridStructurePlacement> CODEC = MAP_CODEC.codec();

    // Grid parameters in chunk units
    private final int spacing;
    private final int offset;

    // Full constructor used by MAP_CODEC/CODEC (includes the base StructurePlacement fields)
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

    // Convenience ctor for simple code-driven construction (defaults to "always attempt" placement)
    public ModGridStructurePlacement(int spacing, int offset) {
        this(Vec3i.ZERO, FrequencyReductionMethod.DEFAULT, 1.0F, 0, Optional.empty(), spacing, offset);
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        // Only allow generation on the chosen chunk inside each spacing x spacing grid cell
        return Math.floorMod(chunkX, this.spacing) == this.offset
                && Math.floorMod(chunkZ, this.spacing) == this.offset;
    }

    @Override
    public StructurePlacementType<?> type() {
        // Links this placement to its registered placement type
        return ModStructurePlacements.GRID.get();
    }
}