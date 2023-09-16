package com.xiang.mixin;

import com.xiang.ServerUtility;
import net.minecraft.network.ClientConnection;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.xiang.ServerUtility.*;

/**
 * @author xiang2333
 */
@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin implements SetAutoCallback {
    @Shadow public abstract void broadcast(Text message, boolean overlay);
    //拦截加入消息
    @Redirect(method = "onPlayerConnect",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"))
    public void replaceBroadcast(PlayerManager instance, Text message, boolean overlay) {
    }
    @Inject(at = @At("TAIL"), method = "onPlayerConnect")
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {

        betterScoreboard.addScoreboardToPlayer(player);
        betterScoreboard.show();

        Scoreboard scoreboard = player.getScoreboard();
        String playerName = player.getEntityName();
        usedPlayers.add(playerName);
        autoScoreBoardPlayers.add(playerName);
        ServerUtility.newAuto(this);
        //显示生命值
        scoreboard.setObjectiveSlot(Scoreboard.LIST_DISPLAY_SLOT_ID, healthObj);
        //初始化分数
        if (!scoreboard.getKnownPlayers().contains(playerName)) {
            for (ScoreboardObjective objective : scoreboardObjectives) {
                scoreboard.getPlayerScore(playerName, objective).setScore(0);
            }
        }
        broadcast(Text.of( "§6玩家" + " §b§n" + player.getName().getString() + "§r §6加入了游戏！"),false);
    }
    @Inject(at = @At("TAIL"), method = "remove")
    private void onPlayerRemove(ServerPlayerEntity player, CallbackInfo ci) {
        broadcast(Text.of( "§6玩家"+ " §b§n" + player.getName().getString() + "§r §6退出了游戏。"), false);
    }

    @Override
    public void server_mod$updateTimer() {
        if (autoScoreBoardPlayers.size() > 0) {
            if (stopScoreboardT){
//                如果是停止状态
                stopScoreboardT = false;
                ServerUtility.startScoreBoardTimer();
            }

        } else {
            stopScoreboardT = true;
        }
    }
}
