package net.oldmanyounger.shroud.entity.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.oldmanyounger.shroud.entity.custom.UmbralHowlerEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Client-side GeckoLib renderer for {@link UmbralHowlerEntity}.
 *
 * <p>This renderer binds {@link UmbralHowlerModel} and applies renderer-level
 * visual settings such as shadow radius while delegating animated geometry and
 * texture handling to GeckoLib.
 *
 * <p>In the broader context of the project, this class is part of the entity
 * client-render registration layer that makes custom Shroud mobs visible and
 * correctly animated in-game.
 */
public class UmbralHowlerRenderer extends GeoEntityRenderer<UmbralHowlerEntity> {

    // Creates the renderer with the Umbral Howler model and shadow configuration
    public UmbralHowlerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new UmbralHowlerModel());

        // Matches shadow size to the entity footprint
        this.shadowRadius = 0.7f;
    }
}