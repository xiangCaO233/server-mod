package com.xiang.mixin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xiang.Trajectory;
import com.xiang.alona.AlonaThread;
import com.xiang.navigate.Navigator;
import com.xiang.scoreborad.AllObjective;
import com.xiang.scoreborad.BetterObjective;
import com.xiang.scoreborad.ObjectiveHandler;
import com.xiang.util.Info;
import com.xiang.util.ServerUtil;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
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
        LOGGER.info("指令设置超过1/3睡觉跳过黑夜");
        getGameRules().get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(34, playerManager.getServer());
        ServerUtil.executeCommand("carpet setDefault commandPlayer true");
        ServerUtil.executeCommand("carpet setDefault flippinCactus true");
        ServerUtil.executeCommand("carpet setDefault fogOff true");
        ServerUtil.executeCommand("carpet setDefault rotatorBlock true");
        Info.server = getPlayerManager().getServer();
        AllObjective.initialize();
        AlonaThread.sendGroupMessage("[IH]: 服务器启动。");
    }

    @Unique
    boolean skip = false;

    long lastTime = 0;

    @Inject(at = @At("TAIL"), method = "tick")
    private void onServerTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
/*
        if (getTicks()%40==0)
            System.gc();*/

        ObjectiveHandler objectiveHandler = AllObjective.autoLoops.peek();


        if (lastTime < System.currentTimeMillis() - (AllObjective.serverInfoHandler.equals(objectiveHandler) ? 15000 : 6000)) {
            objectiveHandler = AllObjective.autoLoops.poll();

            if (objectiveHandler != null) {
                AllObjective.autoLoops.add(objectiveHandler);
                ArrayList<ObjectiveHandler> handlers = AllObjective.autoLoopObjective.getHeaderList();
                handlers.remove(handlers.size() - 1);
                handlers.add(objectiveHandler);
            }
            lastTime = System.currentTimeMillis();
        }

        Info.setServerMotd();
        //更新所有的计分项
        skip = !skip;
        if (skip) {
            AllObjective.getObjectives().iterator().forEachRemaining(BetterObjective::handlerAndShowScore);
        }


        for (Trajectory trajectory : playerTrajectoryMap.values()) {
            trajectory.serverTickHandler();
        }

    }

    /**
     * 开始计分板刷新计时器
     */

    @Inject(at = @At("TAIL"), method = "stop")
    private void onServerStop(boolean waitForShutdown, CallbackInfo ci) {
        saveScoreData();
        //关闭计时器
        stopBackupTimer();
        stopNavThread = true;
        AlonaThread.sendGroupMessage("[IH]: 服务器已关闭。");
        AlonaThread.shutdown = true;
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.exit(0);
        }).start();
    }

}