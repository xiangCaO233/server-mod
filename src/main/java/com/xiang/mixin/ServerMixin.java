package com.xiang.mixin;

import com.xiang.navigate.Navigator;
import com.xiang.scoreborad.BetterObjective;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.slf4j.helpers.SubstituteLogger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Random;
import java.util.function.BooleanSupplier;

import static com.xiang.ServerUtility.*;
import static com.xiang.navigate.Navigator.stopNavThread;

/**
 * 服务端注入
 *
 * @author xiang2333
 */
@Mixin(MinecraftServer.class)
public abstract class ServerMixin {
    //static MinecraftServer server;
    @Shadow
    private PlayerManager playerManager;

    @Shadow
    @Final
    private ServerScoreboard scoreboard;

    @Shadow
    public abstract GameRules getGameRules();

    @Shadow
    public abstract float getTickTime();

    @Shadow
    public abstract PlayerManager getPlayerManager();

    @Shadow
    public abstract int getTicks();

    @Shadow
    public abstract Optional<Path> getIconFile();

    @Shadow
    public abstract void setMotd(String motd);

    @Inject(at = @At("TAIL"), method = "loadWorld")
    private void init(CallbackInfo info) {

        Navigator.playerManager = getPlayerManager();
        LOGGER.info("server mixin loading");
        LOGGER.info("backup thread starting");
        startBackupTimer();

        //new ScoreboardThread().start();

        healthObj = scoreboard.getObjective("health");
        if (healthObj == null) {
            LOGGER.info("创建healthObjScoreboardObj");
            healthObj = scoreboard.addObjective(
                    "health", ScoreboardCriterion.HEALTH, Text.of("生命值"), ScoreboardCriterion.RenderType.HEARTS
            );
            LOGGER.info("创建healthObjScoreboardObj完成");
        }

        LOGGER.info("指令设置不显示命令回显");
        getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(false, playerManager.getServer());
        LOGGER.info("指令设置不显示死亡消息");
        getGameRules().get(GameRules.SHOW_DEATH_MESSAGES).set(false, playerManager.getServer());

    }


    @Inject(at = @At("TAIL"), method = "tick")
    private void onServerTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        setMotd(new Random().nextInt(2000) + "");

        if (playerManager.getPlayerList().size() > 0) {
            betterObjective.setScore(3, "你好 " + playerManager.getPlayerList().get(0).getEntityName() + " !", BetterObjective.LEFT);
            betterObjective.setScore(4, "ping: " + playerManager.getPlayerList().get(0).pingMilliseconds+" ms", BetterObjective.LEFT);
            betterObjective.setScore(5, "x: " + BetterObjective.format(String.format("%.2f",playerManager.getPlayerList().get(0).getX()),8,BetterObjective.RIGHT) +
                    " y: " + BetterObjective.format(String.format("%.2f",playerManager.getPlayerList().get(0).getY()),8,BetterObjective.RIGHT)  +
                    " z: " +BetterObjective.format(String.format("%.2f",playerManager.getPlayerList().get(0).getZ()),8,BetterObjective.RIGHT) , BetterObjective.LEFT);
            betterObjective.setScore(6, "IP: " + playerManager.getPlayerList().get(0).getIp(), BetterObjective.LEFT);
        }

    }

    /**
     * 开始计分板刷新计时器
     */

    @Inject(at = @At("TAIL"), method = "stop")
    private void onServerStop(boolean waitForShutdown, CallbackInfo ci) {
        //保存浮点数据
        for (String playerName : usedPlayers) {
            String data =
                    moveStatisticMap.get(playerName) + "|" +
                            damageStatisticMap.get(playerName) + "|" +
                            takeDamageStatisticMap.get(playerName);
            prop.put(playerName, data);
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(config);
            prop.store(fos, "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        //关闭计时器
        stopBackupT = true;
        stopNavThread = true;

    }
}