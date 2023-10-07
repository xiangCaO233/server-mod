package com.xiang.mixin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xiang.ServerUtility;
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
import net.minecraft.util.Formatting;
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
import static com.xiang.util.SystemInfo.ramUsedPercentage;

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

    @Shadow public abstract void exit();

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
        getGameRules().get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(1, playerManager.getServer());
        ServerUtil.executeCommand("carpet setDefault commandPlayer true");
        ServerUtil.executeCommand("carpet setDefault flippinCactus true");
        ServerUtil.executeCommand("carpet setDefault fogOff true");
        ServerUtil.executeCommand("carpet setDefault rotatorBlock true");
        ServerUtil.executeCommand("carpet setDefault fakePlayerNamePrefix true");
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

        //查询服务器内存健康度
        if (ramUsedPercentage <= 0.85){
            overTime = 0;
        }
        if(ramUsedPercentage >= 0.85){
            if (overTime!=0){
                if (System.currentTimeMillis() - overTime >= 300000){
                    willRestart = true;
                }
            }else {
                overTime = System.currentTimeMillis();
            }
        }

        //判断重启
        if (willRestart&&!isOnBackup){
            if (restartFlag){
                restartFlag = false;
                new Thread(()->{
                    //重启前处理
                    if (!isOnBackup){
                        //并非正在创建备份中
                        //首先停止原备份线程
                        stopBackupTimer();
                        //新建单独线程进行重启前实时备份
                        new Thread(ServerUtility::createBackup).start();
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    //广播重启信息
                    playerManager.broadcast(
                            Text.of(Formatting.RED+ String.valueOf(Formatting.UNDERLINE) +" 服务器内存即将溢出 即将自动重启 "),false
                    );
                    //在备份线程备份完成前等待
                    while (isOnBackup) {
                        Thread.onSpinWait();
                    }
                    //确保关闭原备份线程
                    stopBackupTimer();

                    //倒数重启服务器
                    new Thread(()->{
                        for (int i = 11; i >= 1; i--) {
                            playerManager.broadcast(
                                    Text.of(Formatting.GOLD +"服务器将在"+i+"s后重启"),false
                            );
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ignored) {}
                        }
                        ServerUtil.executeCommand("stop");
                    }).start();

                }).start();
            }
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