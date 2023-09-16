package com.xiang.scoreborad;

import com.xiang.navigate.Navigator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class BetterScoreboard {

    public final ScoreObjects scoreObjects = new ScoreObjects(new ScoreObjects.ScoreObjectsCallback() {
        @Override
        public void removeScoreObject(String displayName) {
            //移除分数
            Navigator.playerManager.sendToAll(
                    new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.REMOVE, scoreboardObjective.getName(), displayName, 0)
            );
        }

        @Override
        public void changeScoreObject(String displayName, int score) {
            Navigator.playerManager.sendToAll(
                    new ScoreboardPlayerUpdateS2CPacket(
                            ServerScoreboard.UpdateMode.CHANGE,
                            scoreboardObjective.getName(),
                            displayName,
                            score)
            );
        }
    });
    private final ScoreboardObjective scoreboardObjective;//计分板目标

    public BetterScoreboard(String scoreboardName, String displayName) {
        scoreboardObjective = new ScoreboardObjective(new Scoreboard(), scoreboardName,
                ScoreboardCriterion.DUMMY,
                Text.of((displayName == null ? "" : displayName)),
                ScoreboardCriterion.RenderType.INTEGER);

    }

    /**
     * 修改计分板显示名
     *
     * @param name 名字
     */
    public void changeScoreboardShowName(@NotNull String name) {
        if (name.equals(scoreboardObjective.getDisplayName().getString()))
            return;
        scoreboardObjective.setDisplayName(Text.of(name));
        Navigator.playerManager.sendToAll(new ScoreboardObjectiveUpdateS2CPacket(scoreboardObjective, 2));//更新名字
    }


    /**
     * 给玩家添加计分板
     *
     * @param player 玩家
     */
    public void addScoreboardToPlayer(PlayerEntity player) {
        //LifeGardenMod.playerManager.sendToAll(new ScoreboardObjectiveUpdateS2CPacket(scoreboardObjective, 0));
        Navigator.playerManager.sendToAround(
                player,
                player.getX(),
                player.getY(),
                player.getZ(),
                0,
                player.getWorld().getRegistryKey(),
                new ScoreboardObjectiveUpdateS2CPacket(scoreboardObjective, 0));

        //show();
        //player.getScoreboard().setObjectiveSlot(Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, scoreboardObjective);
    }


    /**
     * 显示给玩家
     */
    public void show() {
        //让玩家显示
        Navigator.playerManager.getPlayerList().forEach(player -> player.getScoreboard().setObjectiveSlot(Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, scoreboardObjective));
    }
    /**
     * 显示给玩家
     */
    public void showHide() {
        //让玩家显示
        Navigator.playerManager.getPlayerList().forEach(player -> player.getScoreboard().setObjectiveSlot(Scoreboard.MAX_SIDEBAR_TEAM_DISPLAY_SLOT_ID, scoreboardObjective));
    }
    /*
     *//**
     * 动态修改分数对象
     *
     * @param scoreName 分数名
     * @param showName  显示名 为null不修改
     * @param score     分数 为null不修改
     *//*
    public void changeScoreObject(String scoreName, String showName, Integer score) {
        ScoreObjects scoreObject = scoreObjects.get(scoreName);
        boolean isChange = false;//是否修改

        if (scoreObject == null) {//如果没有 则创建新对象
            scoreObject = new ScoreObjects(showName, score);
            scoreObjects.put(scoreName, scoreObject);
            isChange = true;
        } else {
            if (scoreName != null)
                if (!scoreName.equals(scoreObject.displayName)) {
                    //移除分数
                    LifeGardenMod.playerManager.sendToAll(
                            new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.REMOVE, scoreboardObjective.getName(), scoreObject.displayName, 0)
                    );
                    scoreObject.displayName = showName;
                    isChange = true;
                }
            if (score != null)
                if (score != scoreObject.score) {
                    scoreObject.score = score;
                    isChange = true;
                }
        }


        //添加修改分数
        if (isChange)
            LifeGardenMod.playerManager.sendToAll(
                    new ScoreboardPlayerUpdateS2CPacket(
                            ServerScoreboard.UpdateMode.CHANGE,
                            scoreboardObjective.getName(),
                            scoreObject.displayName,
                            scoreObject.score)
            );
    }

    *//**
     * 移除计分板对象
     *
     * @param scoreName 要移除的对象名
     *//*
    public void removeScoreObject(String scoreName) {
        ScoreObjects scoreObject = scoreObjects.remove(scoreName);
        if (scoreObject == null)
            return;
        //移除分数
        LifeGardenMod.playerManager.sendToAll(
                new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.REMOVE, scoreboardObjective.getName(), scoreObject.displayName, 0)
        );
    }

    *//**
     * 移除所有分数对象
     *//*
    public void removeAllScoreObject() {
        Set<String> keySet = scoreObjects.keySet();
        ArrayList<String> keys = new ArrayList<>();
        keys.addAll(keySet);
        for (String key : keys) {
            removeScoreObject(key);
        }

    }*/
}
