package com.xiang.mixin;

import com.xiang.ServerUtility;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author xiang2333
 */
@Mixin(MinecraftServer.class)
public abstract class ServerMixin {
    @Shadow
    private PlayerManager playerManager;

    @Shadow
    @Final
    private ServerScoreboard scoreboard;

    @Shadow
    public abstract CommandManager getCommandManager();

    @Shadow
    public abstract ServerCommandSource getCommandSource();

    @Shadow
    public abstract ServerScoreboard getScoreboard();

    @Shadow public abstract GameRules getGameRules();

    @Shadow public abstract PlayerManager getPlayerManager();

    @Inject(at = @At("TAIL"), method = "loadWorld")
    private void init(CallbackInfo info) {

        ServerUtility.LOGGER.info("server mixin loading");

        ServerUtility.deathCountObj = scoreboard.getObjective("deathCount");
        if (ServerUtility.deathCountObj == null) {
            ServerUtility.LOGGER.info("创建deathCountScoreboardObj");
            ServerUtility.deathCountObj = scoreboard.addObjective(
                    "deathCount", ScoreboardCriterion.DEATH_COUNT, Text.of("死亡榜"), ScoreboardCriterion.RenderType.INTEGER
            );
            ServerUtility.LOGGER.info("创建deathCountScoreboardObj完成");
        }
        ServerUtility.minedCountObj = scoreboard.getObjective("minedCount");
        if (ServerUtility.minedCountObj == null) {
            ServerUtility.LOGGER.info("创建deathCountScoreboardObj");
            ServerUtility.deathCountObj = scoreboard.addObjective(
                    "minedCount", ScoreboardCriterion.DUMMY, Text.of("挖掘榜"), ScoreboardCriterion.RenderType.INTEGER
            );
            ServerUtility.LOGGER.info("创建deathCountScoreboardObj完成");
        }

        ServerUtility.LOGGER.info("指令设置不显示命令回显");
        getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(false,getPlayerManager().getServer());
        ServerUtility.LOGGER.info("指令设置不显示死亡消息");
        getGameRules().get(GameRules.SHOW_DEATH_MESSAGES).set(false,getPlayerManager().getServer());


//        ServerUtility.LOGGER.info("指令设置scoreboardObj显示");
//        cmd.executeWithPrefix(getCommandSource(), "/scoreboard objectives setdisplay sidebar deathCount");
//        ServerUtility.LOGGER.info("Slot->"+getScoreboard().getSlot(ServerUtility.deathCountObj));




        /*ServerUtility.minedBlocksObj = getScoreboard().getObjective("minedBlocksObj");
        if (ServerUtility.minedBlocksObj == null) {
            ServerUtility.minedBlocksObj = scoreboard.addObjective(
                    "minedBlocksObj", ScoreboardCriterion.DUMMY, Text.of("挖掘数"), ScoreboardCriterion.RenderType.INTEGER
            );
        }
        getCommandManager().executeWithPrefix(getCommandSource(), "/scoreboard objectives setdisplay sidebar minedBlocksObj");*/

        // This code is injected into the start of MinecraftServer.loadWorld()V
    }


    /*@Inject(at = @At("HEAD"), method = "tick")
    private void serverTick(CallbackInfo info) {
        List<ServerPlayerEntity> playerList = playerManager.getPlayerList();
        *//*if (playerList.size()>0) {
            //ServerUtility.LOGGER.info("players:" + playerList.get(0).getName().getString());

        }*//*
    }*/

}