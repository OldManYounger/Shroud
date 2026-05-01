package net.oldmanyounger.shroud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.oldmanyounger.shroud.block.entity.ModBlockEntities;
import net.oldmanyounger.shroud.compat.ModCompat;
import net.oldmanyounger.shroud.effect.ModMobEffects;
import net.oldmanyounger.shroud.entity.ModEntities;
import net.oldmanyounger.shroud.entity.client.*;
import net.oldmanyounger.shroud.item.ModCreativeModeTabs;
import net.oldmanyounger.shroud.item.ModItems;
import net.oldmanyounger.shroud.ritual.recipe.RitualRecipeRegistries;
import net.oldmanyounger.shroud.sound.ModSounds;
import net.oldmanyounger.shroud.util.ModItemProperties;
import net.oldmanyounger.shroud.worldgen.ModFeatures;
import net.oldmanyounger.shroud.worldgen.structure.ModStructurePieces;
import net.oldmanyounger.shroud.worldgen.structure.ModStructurePlacements;
import net.oldmanyounger.shroud.worldgen.structure.ModStructures;
import org.slf4j.Logger;

/**
 * Main mod entrypoint that wires registration and lifecycle hooks for Shroud.
 *
 * <p>This class registers deferred content registries, subscribes setup callbacks,
 * binds client renderer setup, and installs custom spawn placement rules.
 *
 * <p>In the broader context of the project, this class is part of Shroud's core
 * bootstrap layer that coordinates top-level initialization across gameplay,
 * rendering, worldgen, and registry systems.
 */
@Mod(Shroud.MOD_ID)
public class Shroud {

    // ==================================
    //  FIELDS
    // ==================================

    // Unique namespace id for all mod resources and registries
    public static final String MOD_ID = "shroud";

    // Shared logger used for diagnostics
    public static final Logger LOGGER = LogUtils.getLogger();

    // Shared Gson parser for datapack json loaders and other json parsing tasks
    public static final Gson GSON = new GsonBuilder().create();

    // ==================================
    //  CONSTRUCTOR / REGISTRATION
    // ==================================

    // Registers listeners and deferred registries on mod load
    public Shroud(IEventBus modEventBus) {

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerSpawnPlacements);

        ModCreativeModeTabs.register(modEventBus);

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);

        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);

        ModStructures.STRUCTURES.register(modEventBus);
        ModStructurePieces.STRUCTURE_PIECES.register(modEventBus);
        ModStructurePlacements.STRUCTURE_PLACEMENTS.register(modEventBus);

        ModMobEffects.MOB_EFFECTS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        RitualRecipeRegistries.register(modEventBus);
    }

    // ==================================
    //  CLIENT EVENT SUBSCRIBER
    // ==================================

    // Client-only event subscriber for renderer and item property setup
    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {

        // Registers entity renderers and client item property predicates
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.LIVING_SCULK.get(), LivingSculkRenderer::new);
            EntityRenderers.register(ModEntities.UMBRAL_HOWLER.get(), UmbralHowlerRenderer::new);
            EntityRenderers.register(ModEntities.BLIGHTED_SHADE.get(), BlightedShadeRenderer::new);
            EntityRenderers.register(ModEntities.GLOAM_EYED_AMALGAM.get(), GloamEyedAmalgamRenderer::new);
            EntityRenderers.register(ModEntities.GLOAM_EYED_AMALGAM_SCULK_SHOT.get(), GloamEyedAmalgamSculkShotRenderer::new);
            ModItemProperties.addCustomItemProperties();
        }
    }

    // ==================================
    //  LIFECYCLE HOOKS
    // ==================================

    // Runs common setup logic for both logical sides
    private void commonSetup(FMLCommonSetupEvent event) {
        ModCompat.initialize();
    }

    // Registers spawn placement rules for custom entities
    private void registerSpawnPlacements(final RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.LIVING_SCULK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.UMBRAL_HOWLER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.BLIGHTED_SHADE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.GLOAM_EYED_AMALGAM.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}