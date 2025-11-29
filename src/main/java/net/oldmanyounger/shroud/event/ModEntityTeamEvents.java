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

@EventBusSubscriber(modid = Shroud.MOD_ID)
public class ModEntityTeamEvents {

    private static final String TEAM_NAME = "sculk_allies";

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity entity = event.getEntity();

        // Only Wardens and Living Sculk
        if (!(entity instanceof Warden) && !(entity instanceof LivingSculkEntity)) {
            return;
        }

        Scoreboard scoreboard = serverLevel.getScoreboard();

        // Create team on first use
        PlayerTeam team = scoreboard.getPlayerTeam(TEAM_NAME);
        if (team == null) {
            team = scoreboard.addPlayerTeam(TEAM_NAME);
            team.setAllowFriendlyFire(false); // being on the same team makes them "allied"
        }

        // Add this entity to the team
        scoreboard.addPlayerToTeam(entity.getScoreboardName(), team);
    }
}
