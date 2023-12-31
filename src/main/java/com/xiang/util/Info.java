package com.xiang.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Formatting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Info {
    public static MinecraftServer server;

    /**
     * 单位换算
     *
     * @param v 值
     * @return 带单位的值
     */
    public static String unitConversion(int v) {
        if (v < 1000) {
            return String.format("%d", v);
        } else if (v < 1000000) {
            return String.format("%.2fk", v / 1000.0);
        } else if (v < 1000000000) {
            return String.format("%.2fm", v / 1000000.0);
        } else {
            return String.format("%.2fb", v / 1000000000.0);
        }
    }

    /**
     * 单位换算
     *
     * @param v 值
     * @return 带单位的值
     */
    public static String unitConversion(float v) {
        if (v < 1000) {
            return String.format("%.2f", v);
        } else if (v < 1000000) {
            return String.format("%.2fk", v / 1000.0);
        } else if (v < 1000000000) {
            return String.format("%.2fm", v / 1000000.0);
        } else {
            return String.format("%.2fb", v / 1000000000.0);
        }
    }

    /**
     * 获得格式化的世界时间
     */
    public static String getWorldTime() {
        long time = server.getOverworld().getTimeOfDay() % 24000;
        long adjustedTime = (time + 22000) * 60 * 60;

        String sTime = new SimpleDateFormat(" HH:mm").format(new Date(adjustedTime));

        String timeLabel = "";
        if (time < 6000) {
            timeLabel = Formatting.GRAY + "上午";
        } else if (time < 6500) {
            timeLabel = Formatting.WHITE + "中午";
        } else if (time < 12000) {
            timeLabel = Formatting.GRAY + "下午";
        } else if (time < 13800) {
            timeLabel = Formatting.YELLOW + "日落";
        } else if (time < 18000) {
            timeLabel = Formatting.BLUE + "夜晚";
        } else if (time < 22200) {
            timeLabel = Formatting.DARK_BLUE + "午夜";
        } else {
            timeLabel = Formatting.YELLOW + "日出";
        }

        return Formatting.RESET + timeLabel + sTime + Formatting.RESET;
    }

    /**
     * 获取运行时间
     */
    public static String getRunTime(boolean full) {
        long time = server.getOverworld().getTime() / 20;
        long s = time % 60;
        long m = time / 60 % 60;
        long h = time / 60 / 60 % 24;
        long d = time / 60 / 60 / 24;
        if (full)
            return (d + "天 ") + h + "小时 " + m + "分钟 " + s + "秒" + Formatting.RESET;
        return (d + "天 ") + h + "小时" + Formatting.RESET;
    }

    /**
     * 时间单位换算
     *
     * @param time 秒数
     * @return 格式化文本
     */
    public static String timeUnitConversion(long time) {
        long s = time % 60;
        long m = time / 60 % 60;
        long h = time / 60 / 60 % 24;
        long d = time / 60 / 60 / 24;

        StringBuilder result = new StringBuilder();
        if (d > 0) {
            result.append(d).append("d ");
        }
        if (h > 0 || d > 0) {
            result.append(String.format("%02d:%02d:%02d", h, m, s));
        } else if (m > 0) {
            result.append(String.format("%02d:%02d", m, s));
        } else {
            result.append(String.format("%02d", s));
        }

        return Formatting.RESET + result.toString() + Formatting.RESET;
    }


    private static final Formatting[] PERCENTAGE_COLORS = new Formatting[]{
            Formatting.GREEN, Formatting.DARK_GREEN, Formatting.YELLOW,
            Formatting.GOLD, Formatting.RED, Formatting.DARK_RED
    };

    /**
     * 格式化百分百
     *
     * @param percentage 范围0.0-1.0
     */
    public static String formatPercentage(double percentage) {
        final double STEP_SIZE = 1.66666 / 10;
        final int COLOR_COUNT = 6;

        for (int i = 0; i < COLOR_COUNT; i++) {
            if (percentage <= (i + 1) * STEP_SIZE) {
                return Formatting.RESET + String.format("%s§l%.2f%%", PERCENTAGE_COLORS[i], percentage * 100) + Formatting.RESET;
            }
        }

        return Formatting.RESET + String.format("§4§l%.2f%%", percentage * 100) + Formatting.RESET;
    }

    /**
     * mspt阈值表
     */
    private static final float[] MSPT_THRESHOLD = new float[]{
            20, 30, 40, 45, 47, 50
    };

    /**
     * 获取格式化的mspt信息
     */
    public static String getMSPT() {
        float mspt = server.getTickTime();

        for (int i = 0; i < 6; i++) {
            if (mspt <= MSPT_THRESHOLD[i])
                return Formatting.RESET + "" + PERCENTAGE_COLORS[i] + Formatting.BOLD + new BigDecimal(mspt).setScale(2, RoundingMode.HALF_UP) + "ms" + Formatting.RESET;
        }
        return Formatting.RESET + "" + Formatting.YELLOW + Formatting.BOLD + new BigDecimal(mspt).setScale(2, RoundingMode.HALF_UP) + "ms" + Formatting.RESET;
    }

    /**
     * 获得格式化的tps信息
     */
    public static String getTPS() {
        float mspt = server.getTickTime();
        if (mspt <= 50) {
            return Formatting.RESET + "" + Formatting.GREEN + Formatting.BOLD + "20" + Formatting.RESET;
        }
        return Formatting.RESET + "" + Formatting.RED + Formatting.BOLD + (int) (1000 / mspt) + Formatting.RESET;
    }

    /**
     * 更新服务器描述信息
     */
    public static void setServerMotd() {
        //更新服务器描述信息
        server.setMotd("§r§a--§b§l infinity§e§l heaven§r§a --   §b无尽§e天堂    §a[原版生存]      "
                + "§6时间: " + Info.getWorldTime() + " §6运行: §f" + Info.getRunTime(false) + " §6TPS:" + Info.getTPS() + " §6MSPT:" + Info.getMSPT());
    }
}
