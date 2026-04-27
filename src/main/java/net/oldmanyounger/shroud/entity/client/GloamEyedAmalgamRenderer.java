package net.oldmanyounger.shroud.entity.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.oldmanyounger.shroud.entity.custom.GloamEyedAmalgamEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Client-side GeckoLib renderer for {@link GloamEyedAmalgamEntity}.
 *
 * <p>This renderer binds the Gloam Eyed Amalgam model and applies renderer-level settings while GeckoLib handles animated geometry rendering.
 *
 * <p>In the broader context of the project, this class is part of Shroud's entity client-render registration layer that makes custom mobs visible with correct animation playback.
 */
public class GloamEyedAmalgamRenderer extends GeoEntityRenderer<GloamEyedAmalgamEntity> {

    // Creates the renderer with model and shadow configuration
    public GloamEyedAmalgamRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GloamEyedAmalgamModel());

        // Uses normal render scale
        this.scaleWidth = 1.0f;
        this.scaleHeight = 1.0f;

        // Uses warden-like shadow footprint
        this.shadowRadius = 0.9f;
    }
}