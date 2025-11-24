package net.oldmanyounger.shroud.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.oldmanyounger.shroud.Shroud;

/**
 * Listens for player interactions and activates Shroud portals when an echo shard is used on a valid frame.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID)
public class PortalActivationHandler {

    /** Handles right-click interactions on blocks and attempts to form a Shroud portal frame */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.ECHO_SHARD)) {
            return;
        }

        BlockPos clickedPos = event.getPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        if (!clickedState.is(Blocks.DEEPSLATE_BRICKS)) {
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean created = ShroudPortalHelper.tryCreatePortal(serverLevel, clickedPos);

        if (created) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}
