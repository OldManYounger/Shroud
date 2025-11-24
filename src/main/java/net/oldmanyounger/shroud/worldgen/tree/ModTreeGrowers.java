package net.oldmanyounger.shroud.worldgen.tree;

import net.minecraft.world.level.block.grower.TreeGrower;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.worldgen.ModConfiguredFeatures;

import java.util.Optional;

/** Defines all custom TreeGrower instances used by the Shroud mod */
public class ModTreeGrowers {

    /** TreeGrower for the Sculk tree, linking it to the SCULK_TREE configured feature */
    public static final TreeGrower SCULK =
            new TreeGrower(Shroud.MOD_ID + ":sculk", Optional.empty(),
                    Optional.of(ModConfiguredFeatures.SCULK_TREE), Optional.empty());
}
