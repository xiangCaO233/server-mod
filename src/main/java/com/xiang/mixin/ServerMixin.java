package com.xiang.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import static com.xiang.ServerUtility.*;

/**
 * 服务端注入
 *
 * @author xiang2333
 */
@Mixin(MinecraftServer.class)
public abstract class ServerMixin {
    @Unique
    Thread timer;
    @Unique
    boolean stop;
    //static MinecraftServer server;
    @Shadow
    private PlayerManager playerManager;

    @Shadow
    @Final
    private ServerScoreboard scoreboard;

    @Shadow
    public abstract GameRules getGameRules();

    @Shadow public abstract float getTickTime();

    @Inject(at = @At("TAIL"), method = "loadWorld")
    private void init(CallbackInfo info) {

        LOGGER.info("server mixin loading");

        //检查配置文件
        if (!config.exists()) {
            try {
                config.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        //加载配置文件
        FileInputStream fis;
        try {
            fis = new FileInputStream(config);
            prop.load(fis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        //初始化积分榜

        //生命值
        healthObj = scoreboard.getObjective("health");
        if (healthObj == null) {
            LOGGER.info("创建healthObjScoreboardObj");
            healthObj = scoreboard.addObjective(
                    "health", ScoreboardCriterion.HEALTH, Text.of("生命值"), ScoreboardCriterion.RenderType.HEARTS
            );
            LOGGER.info("创建healthObjScoreboardObj完成");
        }

        //死亡数
        deathCountObj = scoreboard.getObjective("deathCount");
        if (deathCountObj == null) {
            LOGGER.info("创建deathCountScoreboardObj");
            deathCountObj = scoreboard.addObjective(
                    "deathCount", ScoreboardCriterion.DEATH_COUNT, Text.of("死亡榜"), ScoreboardCriterion.RenderType.INTEGER
            );
            LOGGER.info("创建deathCountScoreboardObj完成");
        }
        scoreboardObjectives.add(deathCountObj);

        //等级
        levelObj = scoreboard.getObjective("level");
        if (levelObj == null) {
            LOGGER.info("创建levelObj");
            levelObj = scoreboard.addObjective(
                    "level", ScoreboardCriterion.LEVEL, Text.of("等级榜"), ScoreboardCriterion.RenderType.INTEGER
            );
            LOGGER.info("创建levelObj完成");
        }
        scoreboardObjectives.add(levelObj);

        //挖掘数
        minedCountObj = scoreboard.getObjective("minedCount");
        if (minedCountObj == null) {
            LOGGER.info("创建minedCountScoreboardObj");
            minedCountObj = scoreboard.addObjective(
                    "minedCount", ScoreboardCriterion.DUMMY, Text.of("挖掘榜"), ScoreboardCriterion.RenderType.INTEGER
            );
            LOGGER.info("创建minedCountScoreboardObj完成");
        }
        scoreboardObjectives.add(minedCountObj);

        //放置数
        placedCountObj = scoreboard.getObjective("placedCount");
        if (placedCountObj == null) {
            LOGGER.info("创建placedCountScoreboardObj");
            placedCountObj = scoreboard.addObjective(
                    "placedCount", ScoreboardCriterion.DUMMY, Text.of("放置榜"), ScoreboardCriterion.RenderType.INTEGER
            );
            LOGGER.info("创建placedCountScoreboardObj完成");
        }
        scoreboardObjectives.add(placedCountObj);

        //交易数
        tradeCountObj = scoreboard.getObjective("tradeCount");
        if (tradeCountObj == null) {
            LOGGER.info("创建tradeCountObj");
            tradeCountObj = scoreboard.addObjective(
                    "tradeCount", ScoreboardCriterion.DUMMY, Text.of("交易榜"), ScoreboardCriterion.RenderType.INTEGER
            );
            LOGGER.info("创建tradeCountObj完成");
        }
        scoreboardObjectives.add(tradeCountObj);

        //移动距离
        moveDistanceObj = scoreboard.getObjective("moveDistance");
        if (moveDistanceObj == null) {
            LOGGER.info("创建moveDistanceScoreboardObj");
            moveDistanceObj = scoreboard.addObjective(
                    "moveDistance", ScoreboardCriterion.DUMMY, Text.of("移动榜"), ScoreboardCriterion.RenderType.INTEGER
            );
            LOGGER.info("创建moveDistanceScoreboardObj完成");
        }
        scoreboardObjectives.add(moveDistanceObj);
        //复原移动距离缓存map
        for (String playerName : scoreboard.getKnownPlayers()) {
            String data = (String) prop.get(playerName);
            if (data == null) {
                moveStatisticMap.put(playerName, (double) scoreboard.getPlayerScore(playerName, moveDistanceObj).getScore());
            }else {
                moveStatisticMap.put(playerName,  Double.valueOf((data.split("\\|")[0])));
            }
        }

        //经验获取数
        expGetCountObj = scoreboard.getObjective("expGetCount");
        if (expGetCountObj == null) {
            LOGGER.info("创建expGetCountObj");
            expGetCountObj = scoreboard.addObjective(
                    "expGetCount", ScoreboardCriterion.DUMMY, Text.of("经验榜"), ScoreboardCriterion.RenderType.INTEGER
            );
            LOGGER.info("创建expGetCountObj完成");
        }
        scoreboardObjectives.add(expGetCountObj);

        //造成伤害数
        damageObj = scoreboard.getObjective("damage");
        if (damageObj == null) {
            LOGGER.info("创建damageObj");
            damageObj = scoreboard.addObjective(
                    "damage", ScoreboardCriterion.DUMMY, Text.of("伤害榜"), ScoreboardCriterion.RenderType.INTEGER
            );
            LOGGER.info("创建damageObj完成");
        }
        scoreboardObjectives.add(damageObj);
        //复原伤害缓存map
        for (String playerName : scoreboard.getKnownPlayers()) {
            String data = (String) prop.get(playerName);
            if (data == null) {
                damageStatisticMap.put(playerName, (float) scoreboard.getPlayerScore(playerName, damageObj).getScore());
            }else {
                damageStatisticMap.put(playerName,  Float.valueOf((data.split("\\|")[1])));
            }
        }

        //受到伤害数
        takeDamageObj = scoreboard.getObjective("takeDamage");
        if (takeDamageObj == null) {
            LOGGER.info("创建takeDamageObj");
            takeDamageObj = scoreboard.addObjective(
                    "takeDamage", ScoreboardCriterion.DUMMY, Text.of("受伤榜"), ScoreboardCriterion.RenderType.INTEGER
            );
            LOGGER.info("创建takeDamageObj完成");
        }
        scoreboardObjectives.add(takeDamageObj);

        //复原受伤缓存map
        for (String playerName : scoreboard.getKnownPlayers()) {
            String data = (String) prop.get(playerName);
            if (data == null) {
                takeDamageStatisticMap.put(playerName, (float) scoreboard.getPlayerScore(playerName, takeDamageObj).getScore());
            }else {
                takeDamageStatisticMap.put(playerName,  Float.valueOf((data.split("\\|")[2])));
            }
        }

        //击杀数(任何有生命实体)
        killCountObj = scoreboard.getObjective("killCount");
        if (killCountObj == null) {
            LOGGER.info("创建killCountObj");
            killCountObj = scoreboard.addObjective(
                    "killCount", ScoreboardCriterion.DUMMY, Text.of("击杀榜"), ScoreboardCriterion.RenderType.INTEGER
            );
            LOGGER.info("创建killCountObj完成");
        }
        scoreboardObjectives.add(killCountObj);

        startScoreBoardTimer();

        LOGGER.info("指令设置不显示命令回显");
        getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(false, playerManager.getServer());
        LOGGER.info("指令设置不显示死亡消息");
        getGameRules().get(GameRules.SHOW_DEATH_MESSAGES).set(false, playerManager.getServer());

    }

    /**
     * 开始计分板刷新计时器
     */
    @Unique
    private void startScoreBoardTimer() {

        timer = new Thread(()->{
            while (!stop){
                for (ServerPlayerEntity player : playerManager.getPlayerList()) {
                    ScoreboardObjective objective = scoreboardObjectives.get(scoreboardObjectiveIndex);
                    if (objective != null) {
                        player.getScoreboard().setObjectiveSlot(
                                Scoreboard.getDisplaySlotId("sidebar"), objective
                        );
                    }
                }
                if (scoreboardObjectiveIndex >= scoreboardObjectives.size() - 1) {
                    scoreboardObjectiveIndex = 0;
                } else {
                    scoreboardObjectiveIndex++;
                }
                try {
                    //随机5~12.5s
                    Thread.sleep(
                            new Random().nextInt(5000,12500)
                    );
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        timer.start();
    }

    @Inject(at = @At("TAIL"), method = "stop")
    private void onServerStop(boolean waitForShutdown, CallbackInfo ci) {
        //保存浮点数据
        for (PlayerEntity player : playerManager.getPlayerList()) {
            String playerName = player.getEntityName();
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
        stop = true;
    }

}