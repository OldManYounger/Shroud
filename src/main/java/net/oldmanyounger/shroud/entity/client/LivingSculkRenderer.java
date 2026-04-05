package net.oldmanyounger.shroud.entity.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.oldmanyounger.shroud.entity.custom.LivingSculkEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Client-side GeckoLib renderer for {@link LivingSculkEntity}.
 *
 * <p>This renderer binds {@link LivingSculkModel} and applies renderer-level
 * visual settings such as shadow radius while delegating animated geometry and
 * texture handling to GeckoLib.
 *
 * <p>In the broader context of the project, this class is part of the entity
 * client-render registration layer that makes custom Shroud mobs visible and
 * correctly animated in-game.
 */
public class LivingSculkRenderer extends GeoEntityRenderer<LivingSculkEntity> {

    // Creates the renderer with the Living Sculk model and shadow configuration
    public LivingSculkRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new LivingSculkModel());

        // Matches shadow size to the entity footprint
        this.shadowRadius = 0.6f;
    }
}