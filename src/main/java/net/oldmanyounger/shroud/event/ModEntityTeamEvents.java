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
import net.oldmanyounger.shroud.entity.custom.LivingSculkEntity;
import net.oldmanyounger.shroud.entity.custom.ResonantHulkEntity;
import net.oldmanyounger.shroud.entity.custom.UmbralHowlerEntity;

@EventBusSubscriber(modid = Shroud.MOD_ID)
public final class ModEntityTeamEvents {

    private static final String TEAM_NAME = "sculk_allies";

    private ModEntityTeamEvents() {}

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity entity = event.getEntity();

        if (!(entity instanceof Warden)
                && !(entity instanceof LivingSculkEntity)
                && !(entity instanceof UmbralHowlerEntity)
                && !(entity instanceof ResonantHulkEntity)) {
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
