package net.oldmanyounger.shroud.entity.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.oldmanyounger.shroud.entity.custom.LivingSculkEntity;
import net.oldmanyounger.shroud.entity.custom.UmbralHowlerEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Client-side renderer for the {@link net.oldmanyounger.shroud.entity.custom.UmbralHowlerEntity}
 * <p>
 * This renderer uses GeckoLib's {@link GeoEntityRenderer} to provide:
 * <ul>
 *   <li>Model-based rendering driven by the {@code LivingSculkModel}</li>
 *   <li>Automatic animation integration with the entity's GeckoLib controllers</li>
 *   <li>Shadow sizing appropriate for the entity's physical footprint</li>
 * </ul>
 * The renderer is registered on the client and is responsible for drawing the entity
 * each frame, including its animated geometry, textures, and shadow. No custom render
 * layers or behaviors are implemented here, making this a clean and minimal render
 * definition that delegates animation and geometry to the GeckoLib model system
 */
public class UmbralHowlerRenderer extends GeoEntityRenderer<UmbralHowlerEntity> {

    // Creates the renderer, supplying the model and configuring visual properties
    public UmbralHowlerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new UmbralHowlerModel());

        // Adjust shadow size to match entity proportions
        this.shadowRadius = 0.7f;
    }
}
