package net.oldmanyounger.shroud.worldgen.structure;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;

/**
 * Registers custom structure types declared by Shroud.
 *
 * <p>Each structure type points at a codec supplier used to decode structure
 * settings from datapacks and to support worldgen serialization behavior.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * structure bootstrap layer that connects custom structure classes to the
 * global world generation registry.
 */
public class ModStructures {

    // Deferred register for structure type entries
    public static final DeferredRegister<StructureType<?>> STRUCTURES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, Shroud.MOD_ID);

    // Structure type for Limbo template structure
    public static final DeferredHolder<StructureType<?>, StructureType<ModLimboTemplateStructure>> LIMBO_TEMPLATE =
            STRUCTURES.register("limbo_template", () -> () -> ModLimboTemplateStructure.MAP_CODEC);
}