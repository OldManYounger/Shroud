package net.oldmanyounger.shroud.entity.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.oldmanyounger.shroud.entity.custom.ResonantHulkEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ResonantHulkRenderer extends GeoEntityRenderer<ResonantHulkEntity> {

    public ResonantHulkRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ResonantHulkModel());
        this.shadowRadius = 1.0f;
    }
}
