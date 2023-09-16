package com.xiang.scoreborad;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 更好的计分项
 */
public class BetterObjective {
    /**
     * 所有的分数列表
     */
    String[] scoreList;

    /**
     * 需要显示的玩家列表
     */
    ArrayList<ServerPlayerEntity> playerList = new ArrayList<>();

    /**
     * 计分项 (原版)
     */
    ScoreboardObjective scoreObjective;

    /**
     * 占位符
     */
    String placeholder;
    /**
     * 左对齐
     */
    public static final int LEFT = 0;
    /**
     * 右对齐
     */
    public static final int RIGHT = 1;
    /**
     * 居中对齐
     */
    public static final int CENTER = 2;

    /**
     * 创建更好的计分项
     *
     * @param size 分数列表的大小 (固定)
     */
    public BetterObjective(String objectiveName, String displayName, int size) {
        //构建占位符
        setPlaceholderWidth(36);
        scoreList = new String[size];
        for (int i = 0; i < scoreList.length; i++) {
            scoreList[i] = placeholder.substring(i);
        }
        scoreObjective = new ScoreboardObjective(new Scoreboard(), objectiveName,
                ScoreboardCriterion.DUMMY,
                Text.of((displayName == null ? "" : displayName)),
                ScoreboardCriterion.RenderType.INTEGER);
    }

    /**
     * 设置占位符宽度 (影响计分项宽度)
     *
     * @param width 长度
     */
    public void setPlaceholderWidth(int width) {
        //构建占位符
        placeholder = " ".repeat(width);
    }

    /**
     * 给计分项内所有玩家发送包
     *
     * @param packet 数据包
     */
    private void sendPacket(Packet<?> packet) {
        for (ServerPlayerEntity player : playerList) {
            player.networkHandler.sendPacket(packet);
        }
    }

    /**
     * 移除mc的分数 (原版)
     *
     * @param playerName 分数的玩家名
     */
    private void removeMCscore(String playerName) {
        sendPacket(
                new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.REMOVE, scoreObjective.getName(), playerName, 0)
        );
    }

    /**
     * 修改mc的分数 (原版)
     *
     * @param playerName 分数的玩家名
     * @param score      分数
     */
    private void modifyMCscore(String playerName, int score) {
        sendPacket(
                new ScoreboardPlayerUpdateS2CPacket(
                        ServerScoreboard.UpdateMode.CHANGE,
                        scoreObjective.getName(),
                        playerName,
                        score)
        );
    }

    /**
     * 将现有的所有分数告诉指定玩家
     *
     * @param player 玩家
     */
    public void syncAllScore(ServerPlayerEntity player) {
        for (int i = 0; i < scoreList.length; i++) {
            player.networkHandler.sendPacket(
                    new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.CHANGE, scoreObjective.getName(), scoreList[i], i)
            );
        }
    }

    /**
     * 设置指定下标的分数的显示标题
     *
     * @param index        分数列表的索引
     * @param displayTitle 显示的标题
     * @param alignment    对齐方式 0左对齐 1右对齐 2居中
     */
    public void setScore(int index, String displayTitle, int alignment) {
        //加工显示标题   对齐方式 0左对齐 1右对齐
        String str = placeholder.substring(displayTitle.length());
        switch (alignment) {
            case 0 -> displayTitle = displayTitle + str;
            case 1 -> displayTitle = str + displayTitle;
            case 2 -> displayTitle = str.substring(str.length() / 2) + displayTitle + str.substring(str.length() / 2);
        }

        //检查索引
        if (index < 0 || index >= scoreList.length) {
            throw new IllegalArgumentException("索引越界");
        }


        //检查是否修改
        if (!scoreList[index].equals(displayTitle)) {
            //更新分数
            removeMCscore(scoreList[index]);
            scoreList[index] = displayTitle;
            modifyMCscore(displayTitle, index);
        }
    }

    /**
     * 格式化字符串
     *
     * @param displayTitle 显示的标题
     * @param width        显示的宽度
     * @param alignment    对齐方式 0左对齐 1右对齐 2居中
     * @return 格式化后的字符串
     */
    public static String format(String displayTitle, int width, int alignment) {
        // 加工显示标题，对齐方式 0左对齐 1右对齐 2居中
        if (width < displayTitle.length()) {
            throw new IllegalArgumentException("Width must be greater than or equal to the length of displayTitle.");
        }

        int paddingSize = width - displayTitle.length();
        String padding = " ".repeat(paddingSize);

        return switch (alignment) {
            case 0 -> padding + displayTitle;
            case 1 -> displayTitle + padding;
            case 2 ->
                    " ".repeat((int) Math.floor(paddingSize / 2.)) + displayTitle + " ".repeat((int) Math.ceil(paddingSize / 2.));
            default -> throw new IllegalArgumentException("Unexpected alignment value: " + alignment);
        };
    }


    /**
     * 修改计分板显示名
     *
     * @param name 名字
     */
    public void changeScoreboardShowName(@NotNull String name) {
        //检查是否有改动
        if (name.equals(scoreObjective.getDisplayName().getString()))
            return;
        scoreObjective.setDisplayName(Text.of(name));
        //更新名字
        sendPacket(new ScoreboardObjectiveUpdateS2CPacket(scoreObjective, 2));
    }

    /**
     * 添加需要被显示计分项的玩家
     *
     * @param player 玩家
     */
    public void addDisplayPlayer(ServerPlayerEntity player) {
        //判断玩家 重复
        if (playerList.contains(player))
            return;
        //添加到列表
        playerList.add(player);
        //发送添加玩家
        sendPacket(new ScoreboardObjectiveUpdateS2CPacket(scoreObjective, 0));
        //同步所有分数
        syncAllScore(player);
        //设置计分项显示的槽位
        player.getScoreboard().setObjectiveSlot(Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, scoreObjective);
    }
}
