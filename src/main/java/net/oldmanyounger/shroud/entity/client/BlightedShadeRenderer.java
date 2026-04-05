package net.oldmanyounger.shroud.entity.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.oldmanyounger.shroud.entity.custom.BlightedShadeEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Client-side renderer implementation for the Blighted Shade entity.
 *
 * <p>This renderer binds the Blighted Shade GeckoLib model and configures entity
 * presentation details such as shadow radius.
 *
 * <p>In the broader context of the project, this class is part of the entity
 * rendering registration layer that turns custom mob assets into visible in-world
 * representations on the client.
 */
public class BlightedShadeRenderer extends GeoEntityRenderer<BlightedShadeEntity> {

    // Creates the renderer with the Blighted Shade model and shadow configuration
    public BlightedShadeRenderer(EntityRendererProvider.Context context) {
        super(context, new BlightedShadeModel());
        this.shadowRadius = 0.7f;
    }
}