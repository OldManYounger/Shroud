package net.oldmanyounger.shroud.worldgen.structure;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;

/**
 * Registers {@link StructureType} entries for this mod's custom worldgen structures
 *
 * <p>Each structure type provides a codec (via the structure class) so structures can be referenced from datapacks
 * and correctly serialized/deserialized during worldgen and world loads</p>
 */
public class ModStructures {

    // Deferred register for structure types
    public static final DeferredRegister<StructureType<?>> STRUCTURES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, Shroud.MOD_ID);

    // Template-backed Limbo structure type referencing ModLimboTemplateStructure.MAP_CODEC
    public static final DeferredHolder<StructureType<?>, StructureType<ModLimboTemplateStructure>> LIMBO_TEMPLATE =
            STRUCTURES.register("limbo_template", () -> () -> ModLimboTemplateStructure.MAP_CODEC);
}