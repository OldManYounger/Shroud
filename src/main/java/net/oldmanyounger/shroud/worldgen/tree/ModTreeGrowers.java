package net.oldmanyounger.shroud.worldgen.tree;

import net.minecraft.world.level.block.grower.TreeGrower;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.worldgen.ModConfiguredFeatures;

import java.util.Optional;

/** Defines all custom TreeGrower instances used by the Shroud mod */
public class ModTreeGrowers {

    /** TreeGrower for the Sculk tree, linking it to the SCULK_TREE configured feature */
    public static final TreeGrower VIRELITH =
            new TreeGrower(Shroud.MOD_ID + ":sculk", Optional.empty(),
                    Optional.of(ModConfiguredFeatures.VIRELITH_TREE), Optional.empty());

    /** TreeGrower for the Umber tree, linking it to the UMBER_TREE configured feature */
    public static final TreeGrower UMBER =
            new TreeGrower(Shroud.MOD_ID + ":umber", Optional.empty(),
                    Optional.of(ModConfiguredFeatures.UMBER_TREE), Optional.empty());

}
