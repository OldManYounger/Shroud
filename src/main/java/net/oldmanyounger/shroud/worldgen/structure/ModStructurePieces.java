package net.oldmanyounger.shroud.worldgen.structure;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.worldgen.structure.piece.ModLimboTemplatePiece;

/**
 * Holds registrations for this mod's {@link StructurePieceType} instances.
 *
 * <p>Piece types must be registered so structure pieces can be serialized/deserialized correctly during world saves/loads.</p>
 */
public class ModStructurePieces {

    // Deferred register for structure piece types
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, Shroud.MOD_ID);

    // Piece type for the Limbo template-based structure piece
    public static final DeferredHolder<StructurePieceType, StructurePieceType> LIMBO_TEMPLATE_PIECE =
            STRUCTURE_PIECES.register("limbo_template_piece",
                    () -> (StructurePieceType.StructureTemplateType) (StructureTemplateManager templateManager, net.minecraft.nbt.CompoundTag tag) ->
                            new ModLimboTemplatePiece(templateManager, tag));
}