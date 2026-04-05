package net.oldmanyounger.shroud.worldgen.tree;

import net.minecraft.world.level.block.grower.TreeGrower;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.worldgen.ModConfiguredFeatures;

import java.util.Optional;

/**
 * Declares custom sapling grower mappings for Shroud tree types.
 *
 * <p>This class provides static {@link TreeGrower} instances that map sapling
 * growth behavior to configured tree features used during natural growth.
 *
 * <p>In the broader context of the project, this class is part of Shroud's flora
 * growth integration layer that connects custom saplings to worldgen-backed tree
 * generation logic.
 */
public class ModTreeGrowers {

    // Tree grower for Virelith saplings
    public static final TreeGrower VIRELITH =
            new TreeGrower(Shroud.MOD_ID + ":sculk", Optional.empty(),
                    Optional.of(ModConfiguredFeatures.VIRELITH_TREE), Optional.empty());

    // Tree grower for Umber saplings
    public static final TreeGrower UMBER =
            new TreeGrower(Shroud.MOD_ID + ":umber", Optional.empty(),
                    Optional.of(ModConfiguredFeatures.UMBER_TREE), Optional.empty());

}