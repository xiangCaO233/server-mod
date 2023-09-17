package com.xiang.mixin;

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

import static com.xiang.ServerUtility.*;

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
        String playerName = player.getName().getString();
        if (!connection.getAddress().toString().contains("127.0.0.1")) {
            onlinePlayers.add(player);
        }

        AllObjective.objectiveMap.get("mainInfo").addDisplayPlayer(player);

        playerNameMapping.put(player.getUuid(), playerName);
        checkStatistic(player);

        Scoreboard scoreboard = player.getScoreboard();

        usedPlayers.add(playerName);
        //显示生命值
        scoreboard.setObjectiveSlot(Scoreboard.LIST_DISPLAY_SLOT_ID, healthObj);
        broadcast(Text.of("§6玩家" + " §b§n" + playerName + "§r §6加入了游戏！"), false);

    }

    @Inject(at = @At("TAIL"), method = "remove")
    private void onPlayerRemove(ServerPlayerEntity player, CallbackInfo ci) {
        broadcast(Text.of("§6玩家" + " §b§n" + player.getName().getString() + "§r §6退出了游戏。"), false);
        onlinePlayers.remove(player);
    }

}
