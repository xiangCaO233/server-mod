package com.xiang.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.xiang.ServerUtility.healthObj;
import static com.xiang.ServerUtility.scoreboardObjectives;

/**
 * @author xiang2333
 */
@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(at=@At("TAIL"),method = "onPlayerConnect")
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci){
        Scoreboard scoreboard = player.getScoreboard();
        String playerName = player.getEntityName();
        //显示生命值
        scoreboard.setObjectiveSlot(Scoreboard.LIST_DISPLAY_SLOT_ID,healthObj);
        //初始化分数
        if (!scoreboard.getKnownPlayers().contains(playerName)){
            for(ScoreboardObjective objective : scoreboardObjectives){
                scoreboard.getPlayerScore(playerName,objective).setScore(0);
            }
        }
    }
}
