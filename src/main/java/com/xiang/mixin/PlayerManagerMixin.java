package com.xiang.mixin;

import com.xiang.Trajectory;
import com.xiang.alona.AlonaThread;
import com.xiang.scoreborad.AllObjective;
import com.xiang.scoreborad.BetterObjective;
import net.minecraft.network.ClientConnection;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.xiang.ServerUtility.*;
import static com.xiang.navigate.Navigator.playerManager;

/**
 * @author xiang2333
 */
@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow
    public abstract void broadcast(Text message, boolean overlay);

    //拦截加入消息注入
    @Redirect(method = "onPlayerConnect",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"))
    public void replaceBroadcast(PlayerManager instance, Text message, boolean overlay) {
    }

    /**
     * 玩家连接注入
     *
     * @param connection
     * @param player
     * @param ci
     */
    @Inject(at = @At("TAIL"), method = "onPlayerConnect")
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {


        String playerName = player.getEntityName();
        boolean isBot = connection.getAddress() == null;
        if (!isBot) {
            if (!DEBUG)
                playerTrajectoryMap.put(player.getUuid(), new Trajectory(player));

            //如果不是假人
            onlinePlayers.add(player);
            AllObjective.setPlayerObjective(player, playerUsedObjectiveMap.get(player.getUuid()));

            playerNameMapping.put(player.getUuid(), playerName);


            checkStatistic(player);

            Integer level = levelMap.get(player.getUuid());
            if (level != null) {
                levelMap.put(player.getUuid(), player.experienceLevel);
            }

            Scoreboard scoreboard = player.getScoreboard();

            usedPlayers.add(player);
            //显示生命值
            scoreboard.setObjectiveSlot(Scoreboard.LIST_DISPLAY_SLOT_ID, healthObj);
        }

        broadcast(Text.of("§6玩家" + " §b§n" + (isBot ? "[bot]" : "") + playerName + "§r §6加入了游戏！"), false);
        AlonaThread.sendGroupMessage("[IH]: " + (isBot ? "[bot]" : "") + playerName + " 加入了游戏");

    }

    @Inject(at = @At("TAIL"), method = "remove")
    private void onPlayerRemove(ServerPlayerEntity player, CallbackInfo ci) {
        try {
            Trajectory trajectory = playerTrajectoryMap.get(player.getUuid());
            if (trajectory != null)
                if (trajectory.getPlayer() == null)
                    playerTrajectoryMap.get(player.getUuid()).save();
        } catch (IOException e) {
        }
        playerTrajectoryMap.remove(player.getUuid());

        boolean isBot = !playerNameMapping.containsKey(player.getUuid());
        broadcast(Text.of("§6玩家" + " §b§n" + (isBot ? "[bot]" : "") + player.getEntityName() + "§r §6退出了游戏。"), false);
        AlonaThread.sendGroupMessage("[IH]: " + (isBot ? "[bot]" : "") + player.getEntityName() + " 退出了游戏");

        onlinePlayers.remove(player);
    }

}
