package com.xiang.scoreborad;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
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

import static net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket.*;

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
     * 用于切换原版计分项
     */
    boolean isScoreObjective1 = true;

    /**
     * 计分项宽度
     */
    int width = 0;
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
     * 分数项处理器列表
     */
    ArrayList<ObjectiveHandler> objectiveHandler = new ArrayList<>();
    /**
     * 累增的周期数
     */
    int cycle = 0;
    /**
     * 缓存计分项1
     */
    ScoreboardObjective scoreObjective1;
    /**
     * 缓存计分项2
     */
    ScoreboardObjective scoreObjective2;

    /**
     * 创建更好的计分项
     *
     * @param size 分数列表的大小 (固定)
     */
    public BetterObjective(String objectiveName, String objectiveTitle, int size) {
        //构建占位符
        setPlaceholderWidth(36);
        scoreList = new String[size];
        for (int i = 0; i < scoreList.length; i++) {
            scoreList[i] = " ".repeat(i + 1);
        }
        objectiveTitle = (objectiveTitle == null ? "" : objectiveTitle);
        scoreObjective1 = new ScoreboardObjective(new Scoreboard(), objectiveName + "_1", ScoreboardCriterion.DUMMY, Text.of(objectiveTitle), ScoreboardCriterion.RenderType.INTEGER);
        scoreObjective2 = new ScoreboardObjective(new Scoreboard(), objectiveName + "_2", ScoreboardCriterion.DUMMY, Text.of(objectiveTitle), ScoreboardCriterion.RenderType.INTEGER);
    }

    /**
     * 获取当前的计分板计分项
     */
    private ScoreboardObjective getScoreObjective() {
        return (isScoreObjective1 ? scoreObjective1 : scoreObjective2);
    }

    /**
     * 切换计分项
     */
    private void switchScoreObjective() {
        isScoreObjective1 = !isScoreObjective1;
    }

    /**
     * 显示给玩家
     */
    public void show() {

        ScoreboardObjective oldObjective = getScoreObjective();
        switchScoreObjective();
        ScoreboardObjective newObjective = getScoreObjective();


        //创建新计分板
        sendPacket(new ScoreboardObjectiveUpdateS2CPacket(newObjective, ADD_MODE));
        sendPacket(new ScoreboardDisplayS2CPacket(Scoreboard.MAX_SIDEBAR_TEAM_DISPLAY_SLOT_ID, newObjective));
        //sendPacket(new ScoreboardObjectiveUpdateS2CPacket(newObjective, UPDATE_MODE));


        //发送新数据
        for (int i = 0; i < scoreList.length; i++) {
            sendPacket(new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.CHANGE, newObjective.getName(), scoreList[i], i));
        }

        //显示新的计分项
        sendPacket(new ScoreboardDisplayS2CPacket(Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, newObjective));

        //删除旧计分项
        sendPacket(new ScoreboardObjectiveUpdateS2CPacket(oldObjective, REMOVE_MODE));
        //playerList.forEach(player -> new ScoreboardDisplayS2CPacket(Scoreboard.MAX_SIDEBAR_TEAM_DISPLAY_SLOT_ID, oldObjective));
    }

    /**
     * 增加一个分数项处理器
     *
     * @param handler 分数项处理器
     */
    public void addHeader(ObjectiveHandler handler) {
        objectiveHandler.add(handler);
    }

    /**
     * 获得处理器列表
     */
    public ArrayList<ObjectiveHandler> getHeaderList() {
        return objectiveHandler;
    }

    /**
     * 设置占位符宽度 (影响计分项宽度)
     *
     * @param width 长度
     */
    public void setPlaceholderWidth(int width) {
        this.width = width;
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
     * 将现有的所有分数告诉指定玩家
     *
     * @param player 玩家
     */
    @Deprecated
    public void syncAllScore(ServerPlayerEntity player) {
        ScoreboardObjective scoreboardObjective = getScoreObjective();
        for (int i = 0; i < scoreList.length; i++) {
            player.networkHandler.sendPacket(new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.CHANGE, scoreboardObjective.getName(), scoreList[i], i));
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
        //检查索引
        if (index < 0 || index >= scoreList.length) {
            throw new IllegalArgumentException("索引越界");
        }

        //格式化字符串
        displayTitle = format(displayTitle, width, alignment);
        displayTitle = clearTrailingSpaces(displayTitle);
        displayTitle += "§r".repeat(index);


        //检查是否修改
        if (!scoreList[index].equals(displayTitle)) {
            //更新分数
            //removeMCscore(scoreList[index]);
            scoreList[index] = displayTitle;
            //modifyMCscore(displayTitle, index);
        }
    }

    /**
     * 清除尾部空格
     */
    public static String clearTrailingSpaces(String input) {
        int i = input.length() - 1;
        while (i >= 0 && Character.isWhitespace(input.charAt(i))) {
            i--;
        }
        return input.substring(0, i + 1);
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
        if (displayTitle == null)
            return "null";
        // 去除颜色字符的原始字符串
        String original = displayTitle.replaceAll("§[0-9a-zA-Z]", "");
        // 加工显示标题，对齐方式 0左对齐 1右对齐 2居中
        if (width < original.length()) {
            System.out.println("宽度必须大于或等于 displayTitle 的长度。");
            return "?????";
        }

        int paddingSize = width - original.length();
        String padding = " ".repeat(paddingSize);

        return switch (alignment) {
            case 0 -> displayTitle + padding;
            case 1 -> padding + displayTitle;
            case 2 ->
                    " ".repeat((int) Math.floor(paddingSize / 2.)) + displayTitle + " ".repeat((int) Math.ceil(paddingSize / 2.));
            default -> throw new RuntimeException("Unexpected alignment value: " + alignment);
        };
    }

    /**
     * 处理更新并显示分数 (通过分数项处理器)
     */
    public void handlerAndShowScore() {
        for (ObjectiveHandler handler : objectiveHandler) {
            handler.onObjectiveUpdate(this, cycle % handler.getMaxCycle());
        }
        cycle++;
        show();
    }

    /**
     * 修改计分项标题名
     *
     * @param title 标题
     */
    public void setObjectiveTitle(@NotNull String title) {
        Text text = Text.of(title);
        scoreObjective1.setDisplayName(text);
        scoreObjective2.setDisplayName(text);
    }

    /**
     * 添加需要被显示计分项的玩家
     *
     * @param player 玩家
     */
    public void addPlayer(ServerPlayerEntity player) {
        //判断玩家 重复
        if (playerList.contains(player)) return;
        //添加到列表
        playerList.add(player);
    }

    /**
     * 移除要显示计分项的玩家
     *
     * @param player 玩家
     */
    public void removePlayer(ServerPlayerEntity player) {
        //判断玩家存在
        playerList.remove(player);
        sendPacket(new ScoreboardObjectiveUpdateS2CPacket(scoreObjective1, REMOVE_MODE));
        sendPacket(new ScoreboardObjectiveUpdateS2CPacket(scoreObjective2, REMOVE_MODE));
    }

    /**
     * 获得显示的玩家列表
     */
    public ArrayList<ServerPlayerEntity> getPlayerList() {
        return playerList;
    }
}
