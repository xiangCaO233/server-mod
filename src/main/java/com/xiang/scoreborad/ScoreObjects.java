package com.xiang.scoreborad;

import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.scoreboard.ServerScoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.function.BiConsumer;

public class ScoreObjects {
    private static class ScoreObject {
        public String displayName;//显示名
        public int score;//分数

        public ScoreObject(String displayName, int score) {
            this.displayName = displayName;
            this.score = score;
        }
    }


    private final HashMap<String, ScoreObject> scoreObjects = new HashMap<>();//所有分数对象  分数名  显示名
    private ScoreObjectsCallback scoreObjectsCallback;//回调

    public ScoreObjects(ScoreObjectsCallback scoreObjectsCallback) {
        this.scoreObjectsCallback = scoreObjectsCallback;
    }

    /**
     * 移除计分板对象
     *
     * @param scoreName 要移除的对象名
     * @return 移除状态
     */
    public void removeScoreObject(String scoreName) {
        ScoreObject scoreObject = scoreObjects.remove(scoreName);
        if (scoreObject == null)
            return;
        scoreObjectsCallback.removeScoreObject(scoreObject.displayName);
    }

    /**
     * 移除所有分数对象
     */
    public void removeAllScoreObject() {
        Set<String> keySet = scoreObjects.keySet();
        ArrayList<String> keys = new ArrayList<>();
        keys.addAll(keySet);
        for (String key : keys) {
            removeScoreObject(key);
        }

    }

    /**
     * 动态修改分数对象
     *
     * @param scoreName 分数名
     * @param showName  显示名 为null不修改
     * @param score     分数 为null不修改
     */
    public void changeScoreObject(String scoreName, String showName, Integer score) {
        ScoreObject scoreObject = scoreObjects.get(scoreName);
        boolean isChange = false;//是否修改

        if (scoreObject == null) {//如果没有 则创建新对象
            scoreObject = new ScoreObject(showName, score);
            scoreObjects.put(scoreName, scoreObject);
            isChange = true;
        } else {
            if (scoreName != null)
                if (!scoreName.equals(scoreObject.displayName)) {
                    /*//移除分数
                    LifeGardenMod.playerManager.sendToAll(
                            new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.REMOVE, scoreboardObjective.getName(), scoreObject.displayName, 0)
                    );*/
                    scoreObjectsCallback.removeScoreObject(scoreObject.displayName);//调用回调

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
            scoreObjectsCallback.changeScoreObject(scoreObject.displayName, scoreObject.score);//调用回调
        /*
            LifeGardenMod.playerManager.sendToAll(
                    new ScoreboardPlayerUpdateS2CPacket(
                            ServerScoreboard.UpdateMode.CHANGE,
                            scoreboardObjective.getName(),
                            scoreObject.displayName,
                            scoreObject.score)
            );*/
    }

    /**
     * 枚举所有分数对象
     * @param forEach
     */
    public void forEach(ScoreObjectsForEach forEach) {
        scoreObjects.forEach((s, scoreObject) -> {
            forEach.forEach(s, scoreObject.displayName, scoreObject.score);
        });
    }

    public interface ScoreObjectsForEach {
        void forEach(String scoreName, String displayName, int score);
    }

    public interface ScoreObjectsCallback {
        void removeScoreObject(String displayName);

        void changeScoreObject(String displayName, int score);
    }

}
