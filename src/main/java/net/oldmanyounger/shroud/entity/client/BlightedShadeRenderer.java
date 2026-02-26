package net.oldmanyounger.shroud.entity.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.oldmanyounger.shroud.entity.custom.BlightedShadeEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BlightedShadeRenderer extends GeoEntityRenderer<BlightedShadeEntity> {

    public BlightedShadeRenderer(EntityRendererProvider.Context context) {
        super(context, new BlightedShadeModel());
        this.shadowRadius = 0.7f;
    }
}
