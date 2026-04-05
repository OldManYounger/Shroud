package net.oldmanyounger.shroud.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.custom.BlightedShadeEntity;
import net.oldmanyounger.shroud.entity.custom.LivingSculkEntity;
import net.oldmanyounger.shroud.entity.custom.UmbralHowlerEntity;

/**
 * Assigns sculk-aligned entities to a shared no-friendly-fire scoreboard team.
 *
 * <p>This event handler ensures Wardens and Shroud's sculk-aligned mobs are placed
 * onto a common team when they join a server level, preventing allied infighting.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * runtime faction-coordination layer that keeps custom entity relationships
 * consistent through scoreboard-backed team rules.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID)
public final class ModEntityTeamEvents {

    // Shared team identifier for sculk-allied entities
    private static final String TEAM_NAME = "sculk_allies";

    // Prevents instantiation of this static event handler class
    private ModEntityTeamEvents() {}

    // Adds eligible entities to the shared team on server join
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity entity = event.getEntity();

        if (!(entity instanceof Warden)
                && !(entity instanceof LivingSculkEntity)
                && !(entity instanceof UmbralHowlerEntity)
                && !(entity instanceof BlightedShadeEntity)) {
            return;
        }

        Scoreboard scoreboard = serverLevel.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(TEAM_NAME);

        if (team == null) {
            team = scoreboard.addPlayerTeam(TEAM_NAME);
            team.setAllowFriendlyFire(false);
        }

        String key = entity.getScoreboardName();
        if (scoreboard.getPlayersTeam(key) != team) {
            scoreboard.addPlayerToTeam(key, team);
        }
    }
}