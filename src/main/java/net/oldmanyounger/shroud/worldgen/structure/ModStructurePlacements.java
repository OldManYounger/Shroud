package net.oldmanyounger.shroud.worldgen.structure;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.worldgen.structure.placement.ModGridStructurePlacement;

/**
 * Registers custom structure placement types used by Shroud worldgen.
 *
 * <p>Placement type registrations expose codec-backed placement implementations
 * to datapack configuration and structure set deserialization.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * structure distribution layer that links custom placement logic into vanilla
 * world generation registries.
 */
public class ModStructurePlacements {

    // Deferred register for structure placement type entries
    public static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENTS =
            DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, Shroud.MOD_ID);

    // Placement type for grid-snapped placement logic
    public static final DeferredHolder<StructurePlacementType<?>, StructurePlacementType<ModGridStructurePlacement>> GRID =
            STRUCTURE_PLACEMENTS.register("grid", () -> () -> ModGridStructurePlacement.MAP_CODEC);
}