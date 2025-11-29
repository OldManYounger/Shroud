package net.oldmanyounger.shroud.entity.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.oldmanyounger.shroud.entity.custom.LivingSculkEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LivingSculkRenderer extends GeoEntityRenderer<LivingSculkEntity> {

    public LivingSculkRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new LivingSculkModel());
        this.shadowRadius = 0.6f;
    }
}
