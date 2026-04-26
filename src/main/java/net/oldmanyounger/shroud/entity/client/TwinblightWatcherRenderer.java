package net.oldmanyounger.shroud.entity.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.oldmanyounger.shroud.entity.custom.TwinblightWatcherEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Client-side GeckoLib renderer for {@link TwinblightWatcherEntity}
 *
 * <p>This renderer binds the Twinblight Watcher model and applies renderer-level settings while GeckoLib handles animated geometry rendering.
 *
 * <p>In the broader context of the project, this class is part of Shroud's entity client-render registration layer that makes custom mobs visible with correct animation playback.
 */
public class TwinblightWatcherRenderer extends GeoEntityRenderer<TwinblightWatcherEntity> {

    // Creates the renderer with model and shadow configuration
    public TwinblightWatcherRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TwinblightWatcherModel());

        // Scales the rendered model to 2x on both axes
        this.scaleWidth = 2.0f;
        this.scaleHeight = 2.0f;

        // Increases shadow size to match larger body
        this.shadowRadius = 1.2f;
    }
}