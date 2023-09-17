package com.xiang.mixin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xiang.navigate.Navigator;
import com.xiang.scoreborad.AllObjective;
import com.xiang.scoreborad.BetterObjective;
import com.xiang.util.Info;
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
        LOGGER.info("指令设置不显示死亡消息");
        getGameRules().get(GameRules.SHOW_DEATH_MESSAGES).set(false, playerManager.getServer());
        Info.server = getPlayerManager().getServer();
        AllObjective.initialize();
    }

    @Unique
    boolean skip = false;

    @Inject(at = @At("TAIL"), method = "tick")
    private void onServerTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        Info.setServerMotd();
        //更新所有的计分项
        skip = !skip;
        if (skip) {
            AllObjective.getObjectives().iterator().forEachRemaining(BetterObjective::handlerScore);
        }

        /*

        if (playerManager.getPlayerList().size() > 0) {
            betterObjective.setScore(3, "你好 NMSL hhh " + Info.getWorldTime() + " !", BetterObjective.LEFT);
            betterObjective.setScore(2, "你好 NMSL hhh " + Info.getRunTime() + " !", BetterObjective.LEFT);
            betterObjective.setScore(1, "你好 NMSL hhh " + Info.timeUnitConversion(600) + " !", BetterObjective.LEFT);
            betterObjective.setScore(1, "你好 NMSL hhh " + Info.formatPercentage(0.1) + " !", BetterObjective.LEFT);
            betterObjective.setScore(6, "你好 NMSL hhh " + Info.formatPercentage(0.4) + " !", BetterObjective.LEFT);
            betterObjective.setScore(7, "你好 NMSL hhh " + Info.formatPercentage(0.6) + " !", BetterObjective.LEFT);
            betterObjective.setScore(8, "你好 NMSL hhh " + Info.formatPercentage(0.9) + " !", BetterObjective.LEFT);
            betterObjective.setScore(9, "你好 NMSL hhh " + Info.formatPercentage(1.0) + " !", BetterObjective.LEFT);

        }*/

    }

    /**
     * 开始计分板刷新计时器
     */

    @Inject(at = @At("TAIL"), method = "stop")
    private void onServerStop(boolean waitForShutdown, CallbackInfo ci) {

        Gson gson = new GsonBuilder().setLenient().serializeSpecialFloatingPointValues().create();
        JsonParser jsonParser = new JsonParser();
        JsonObject deathsJson = jsonParser.parse(gson.toJson(deathsStatisticMap)).getAsJsonObject();
        JsonObject minedCountJson = jsonParser.parse(gson.toJson(minedCountStatisticMap)).getAsJsonObject();
        JsonObject placedCountJson = jsonParser.parse(gson.toJson(placedCountStatisticMap)).getAsJsonObject();
        JsonObject tradeCountJson = jsonParser.parse(gson.toJson(tradeCountStatisticMap)).getAsJsonObject();
        JsonObject moveJson = jsonParser.parse(gson.toJson(moveStatisticMap)).getAsJsonObject();
        JsonObject expGetCountJson = jsonParser.parse(gson.toJson(expGetCountStatisticMap)).getAsJsonObject();
        JsonObject killCountJson = jsonParser.parse(gson.toJson(killCountStatisticMap)).getAsJsonObject();
        JsonObject damageJson = jsonParser.parse(gson.toJson(damageStatisticMap)).getAsJsonObject();
        JsonObject takeDamageJson = jsonParser.parse(gson.toJson(takeDamageStatisticMap)).getAsJsonObject();
        JsonObject onlineJson = jsonParser.parse(gson.toJson(onlineStatisticMap)).getAsJsonObject();

        JsonObject config = new JsonObject();
        config.add("deaths",deathsJson);
        config.add("minedCount",minedCountJson);
        config.add("placedCount",placedCountJson);
        config.add("tradeCount",tradeCountJson);
        config.add("move",moveJson);
        config.add("expGetCount",expGetCountJson);
        config.add("killCount",killCountJson);
        config.add("damage",damageJson);
        config.add("takeDamage",takeDamageJson);
        config.add("online",onlineJson);

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(configfile));
            bw.write(gson.toJson(config));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            if (bw != null) {
                try {
                    bw.flush();
                    bw.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        //关闭计时器
        stopBackupT = true;
        stopNavThread = true;

    }


}