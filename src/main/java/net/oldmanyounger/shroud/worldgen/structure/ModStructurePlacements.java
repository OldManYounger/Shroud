package net.oldmanyounger.shroud.worldgen.structure;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.worldgen.structure.placement.ModGridStructurePlacement;

/**
 * Registers custom {@link StructurePlacementType} instances for the mod
 *
 * <p>Placement types allow datapacks and registries to reference custom placement logic (e.g., grid-snapped spawning)
 * by name and ensure the correct codec is used for serialization</p>
 */
public class ModStructurePlacements {

    // Deferred register for structure placement types
    public static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENTS =
            DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, Shroud.MOD_ID);

    // Placement type for the grid-snapped placement implementation
    public static final DeferredHolder<StructurePlacementType<?>, StructurePlacementType<ModGridStructurePlacement>> GRID =
            STRUCTURE_PLACEMENTS.register("grid", () -> () -> ModGridStructurePlacement.MAP_CODEC);
}