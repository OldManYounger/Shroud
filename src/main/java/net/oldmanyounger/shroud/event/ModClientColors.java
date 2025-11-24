//package net.oldmanyounger.shroud.event;
//
//import net.minecraft.client.renderer.BiomeColors;
//import net.minecraft.world.level.FoliageColor;
//import net.neoforged.api.distmarker.Dist;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
//import net.oldmanyounger.shroud.Shroud;
//import net.oldmanyounger.shroud.block.ModBlocks;
//
//@EventBusSubscriber(modid = Shroud.MOD_ID, value = Dist.CLIENT)
//public class ModClientColors {
//
//    @SubscribeEvent
//    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
//        // Foliage tint in world
//        event.register(
//                (state, level, pos, tintIndex) -> {
//                    if (tintIndex != 0) return 0xFFFFFF; // only tint foliage layer
//                    if (level != null && pos != null) {
//                        return BiomeColors.getAverageFoliageColor(level, pos);
//                    }
//                    return FoliageColor.getDefaultColor();
//                },
//                ModBlocks.SCULK_LEAVES.get()
//        );
//    }
//
//    @SubscribeEvent
//    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
//        // Foliage tint in inventory
//        event.register(
//                (stack, tintIndex) -> {
//                    if (tintIndex != 0) return 0xFFFFFF;
//                    return FoliageColor.getDefaultColor();
//                },
//                ModBlocks.SCULK_LEAVES.get().asItem()
//        );
//    }
//}