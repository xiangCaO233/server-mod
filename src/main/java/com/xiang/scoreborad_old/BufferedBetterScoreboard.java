package com.xiang.scoreborad_old;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

public class BufferedBetterScoreboard {
    BetterScoreboard[] betterScoreboardBuf;//缓存
    BetterScoreboard betterScoreboardShow;//要显示的目标
    BetterScoreboard betterScoreboardCanvas;//将要显示的目标
    int showIndex = 0;//显示索引

    public ScoreObjects scoreObjects = new ScoreObjects(new ScoreObjects.ScoreObjectsCallback() {
        @Override
        public void removeScoreObject(String displayName) {

        }

        @Override
        public void changeScoreObject(String displayName, int score) {

        }
    });//所有分数对象  分数名  显示名


    public BufferedBetterScoreboard(String scoreboardName, String displayName) {
        betterScoreboardBuf = new BetterScoreboard[]{
                new BetterScoreboard(scoreboardName + "_1", displayName),
                new BetterScoreboard(scoreboardName + "_2", displayName)
        };
        show();
    }

    public void changeScoreboardShowName(@NotNull String name) {
        betterScoreboardCanvas.changeScoreboardShowName(name);
    }

    public void show() {

        betterScoreboardShow = betterScoreboardBuf[showIndex];
        showIndex++;
        showIndex %= betterScoreboardBuf.length;
        betterScoreboardCanvas = betterScoreboardBuf[showIndex];


        //betterScoreboardCanvas.addScoreboardToPlayer();
        betterScoreboardCanvas.showHide();
        betterScoreboardCanvas.scoreObjects.removeAllScoreObject();
        scoreObjects.forEach((scoreName, displayName, score) -> {
            betterScoreboardCanvas.scoreObjects.changeScoreObject(scoreName, displayName, score);
        });

        betterScoreboardCanvas.show();

    }

    public void addScoreboardToPlayer(PlayerEntity player) {
        for (BetterScoreboard betterScoreboard : betterScoreboardBuf) {
            betterScoreboard.addScoreboardToPlayer(player);
        }
    }

}
