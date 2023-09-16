package com.xiang.scoreborad;

import com.xiang.navigate.Navigator;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Formatting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ScoreboardThread extends Thread {
    public ScoreboardThread() {
        setName("ScoreboardThread");
    }

    private static long SWITCH_TIMEOUT = 20000;//切换时间

    private static int mainTitleIndex = 0;//当前动画播放索引
    //protected static String nextPageColor = "";//下一个页面的颜色 (预判)


    private static long lastSwitchTime = 0;//上一次更新的时间戳

    private static final int[] stateMap = {0, 1, 0, 2, 0, 3, 0, 4, 0, 5, 0, 6, 0, 7, 0, 8};//要显示状态的索引表
    private static int stateIndex = 0;//当前的状态索引
    private static int nextStateIndex = 0;//下一个在状态索引

    private static final ArrayList<ScoreboardState> scoreboardStates = new ArrayList<>();


    private static final String[] mainTitle = {"§6----§4§l Life §3§lGarden §r§6----",
            "§6----§c§l L§4§life §3§lGarden §r§6----",
            "§6----§c§l Li§4§lfe §3§lGarden §r§6----",
            "§6----§c§l Lif§4§le §3§lGarden §r§6----",
            "§6----§c§l Life §3§lGarden §r§6----",
            "§6----§c§l Life §b§lG§3§larden §r§6----",
            "§6----§c§l Life §b§lGa§3§lrden §r§6----",
            "§6----§c§l Life §b§lGar§3§lden §r§6----",
            "§6----§c§l Life §b§lGard§3§len §r§6----",
            "§6----§c§l Life §b§lGarde§3§ln §r§6----",
            "§6----§c§l Life §b§lGarden §r§6----",
            "§6----§4§l Life §3§lGarden §r§6----",
            "§6----§c§l Life §b§lGarden §r§6----",
            "§6----§4§l Life §3§lGarden §r§6----",
            "§6----§c§l Life §b§lGarden §r§6----",
            "§6----§4§l Life §3§lGarden §r§6----",

    };

    private interface ScoreboardStateRun {
        void run(BufferedBetterScoreboard betterScoreboard);
    }


    private static class ScoreboardState {
        public String title;//标题
        public String color;//颜色
        public ScoreboardStateRun scoreboardStateRun;//处理程序
        public BufferedBetterScoreboard betterScoreboard;//更好的计分板


        public void run(float progress) {
            mainTitleIndex++;
            mainTitleIndex %= mainTitle.length;

            betterScoreboard.changeScoreboardShowName(mainTitle[mainTitleIndex]);//播放主标题动画


            scoreboardStateRun.run(betterScoreboard);//执行内部

            ScoreboardState next = scoreboardStates.get(stateMap[nextStateIndex]);

            StringBuilder titleProgressStr = new StringBuilder("-------------------------");
            titleProgressStr.insert((int) (19 * progress), next.color + "§l");//插入下一个颜色
            titleProgressStr.insert(0, color + "§l");//插入颜色


            betterScoreboard.scoreObjects.changeScoreObject("进度", titleProgressStr.toString(), 5);

            betterScoreboard.scoreObjects.changeScoreObject("页面", "   §r[" + color + title + "§r]  <  §r" + next.color + next.title, 4);

            betterScoreboard.scoreObjects.changeScoreObject("游戏时", "游戏时间 : §e" + getWorldTime(), 3);

            betterScoreboard.scoreObjects.changeScoreObject("已运行", "已 运 行 : §e" + getRunTime(), 2);


            String cpu = "CPU : " + percentageToColor(SystemInfo.cpuUsedPercentage) + "§l" + new BigDecimal(SystemInfo.cpuUsedPercentage * 100).setScale(2, RoundingMode.HALF_UP) + "%                ";
            String ram = "§rRAM : " + percentageToColor(SystemInfo.ramUsedPercentage) + "§l" + new BigDecimal(SystemInfo.ramUsedPercentage * 100).setScale(2, RoundingMode.HALF_UP) + "%";
            betterScoreboard.scoreObjects.changeScoreObject("CPURAM", cpu.substring(0, 18) + ram, 1);

            //betterScoreboard.scoreObjects.changeScoreObject("RAM", " R A M  : " + percentageToColor(SystemInfo.ramUsedPercentage) + "§l" + new BigDecimal(SystemInfo.ramUsedPercentage * 100).setScale(2, RoundingMode.HALF_UP) + "%", 2);
            //LifeGardenMod.server.get

            //String tps = "TPS : " + (SecondThread.tps < 20 ? "§c" : "§a") + "§l" + SecondThread.tps + "                  ";
            float mspt = Navigator.playerManager.getServer().getTickTime();
            String msptS = "§rMSPT : " + MSPTToColor(mspt) + "§l" + new BigDecimal(mspt).setScale(2, RoundingMode.HALF_UP) + "ms";

            //betterScoreboard.scoreObjects.changeScoreObject("TPS", tps.substring(0, 17) + msptS, 0);


            //1 - 15

/*

            betterScoreboard.scoreObjects.changeScoreObject("CPU", " C P U  : " + percentageToColor(SystemInfo.cpuUsedPercentage) + "§l" + new BigDecimal(SystemInfo.cpuUsedPercentage * 100).setScale(2, RoundingMode.HALF_UP) + "%", 3);

            betterScoreboard.scoreObjects.changeScoreObject("RAM", " R A M  : " + percentageToColor(SystemInfo.ramUsedPercentage) + "§l" + new BigDecimal(SystemInfo.ramUsedPercentage * 100).setScale(2, RoundingMode.HALF_UP) + "%", 2);
            //LifeGardenMod.server.get
            betterScoreboard.scoreObjects.changeScoreObject("TPS", " T P S  : " + (SecondThread.tps < 20 ? "§c" : "§a") + "§l" + SecondThread.tps, 1);

            float mspt = LifeGardenMod.server.getTickTime();
            betterScoreboard.scoreObjects.changeScoreObject("MSPT", "M S P T : " + MSPTToColor(mspt) + "§l" + new BigDecimal(mspt).setScale(2, RoundingMode.HALF_UP) + "ms", 0);

*/

            betterScoreboard.show();


        }

        public ScoreboardState(String title, String color, ScoreboardStateRun scoreboardStateRun, BufferedBetterScoreboard betterScoreboard) {
            this.title = title;
            this.color = color;
            this.scoreboardStateRun = scoreboardStateRun;
            this.betterScoreboard = betterScoreboard;
        }
    }

    public static final BufferedBetterScoreboard betterScoreboard = new BufferedBetterScoreboard("life", "");

    public void printRank(LinkedHashMap<String, Integer> statData, String unit, PrintRankConversion conversion) {
        AtomicInteger index = new AtomicInteger();

        ArrayList<Map.Entry<String, Integer>> sortStatData = new ArrayList<>(statData.entrySet());
        Collections.sort(sortStatData, Comparator.comparingInt(Map.Entry::getValue));


        for (int i = 0; i < 9; i++) {
            int score = 14 - (i);
            if (sortStatData.size() - 1 < i) {
                betterScoreboard.scoreObjects.changeScoreObject("空" + score, "            ".substring(i), score);
                continue;
            }
            Map.Entry<String, Integer> sortStat = sortStatData.get(sortStatData.size() - 1 - i);
            String name = sortStat.getKey();
            String color = "§e";
            if (i == 0) {
                color = "§6";
            }
            if (i == 1) {
                color = "§b";
            }
            if (i == 2) {
                color = "§a";
            }


            betterScoreboard.scoreObjects.changeScoreObject(name,
                    color + ("#" + (i + 1) + "  ").substring(0, 3) + " " +
                            (name + "                 ").substring(0, 16) + " " +
                            conversion.conversion(sortStat.getValue()) + unit,
                    score);

        }

    }

    interface PrintRankConversion {
        String conversion(int data);
    }

    int tempIndex = 0;

    public void changeScoreObject(String title, String show) {
        betterScoreboard.scoreObjects.changeScoreObject(title, show, ++tempIndex);
    }

    @Override
    public void run() {


        scoreboardStates.add(new ScoreboardState("主信息", "§a", betterScoreboard -> {
            tempIndex = 5;

            changeScoreObject("现时间", "时    钟 : §e" + new SimpleDateFormat("HH:mm:ss").format(new Date()));
            changeScoreObject("游戏版本", "游戏版本 : §e1.19.3");
            changeScoreObject("官方群号", "官方群号 : §e937403431");
            changeScoreObject("空13", "   ");
            changeScoreObject("地址", "地    址 : §ezedo.top:1145");
            changeScoreObject("空11", " ");
            changeScoreObject("欢迎", "欢迎来到  §c[L§bG]§r !");


            //changeScoreObject("游戏版本", "游戏版本 : 1.19.3");

        }, betterScoreboard));
        scoreboardStates.add(new ScoreboardState("死亡榜", "§c", betterScoreboard -> {
            //玩家名   状态
            LinkedHashMap<String, Integer> statData = new LinkedHashMap<>();

            //读取在线玩家状态
            for (ServerPlayerEntity player : Navigator.playerManager.getPlayerList()) {
                String name = player.getName().getString();
                int stat = player.getStatHandler().getStat(Stats.CUSTOM, Stats.DEATHS);
                statData.put(name, stat);
            }

            printRank(statData, "次", ScoreboardThread::unitConversion);


        }, betterScoreboard));
        scoreboardStates.add(new ScoreboardState("挖掘榜", "§e", betterScoreboard -> {


            //玩家名   状态
            LinkedHashMap<String, Integer> statData = new LinkedHashMap<>();


            //读取在线玩家状态
            for (ServerPlayerEntity player : Navigator.playerManager.getPlayerList()) {
                if ("127.0.0.1".equals(player.getIp()))//检测机器人
                    continue;
                String name = player.getName().getString();
                AtomicInteger stat = new AtomicInteger(0);
                Registries.BLOCK.forEach(block -> {
                    stat.addAndGet(player.getStatHandler().getStat(Stats.MINED, block));
                });


                statData.put(name, stat.get());
            }


            printRank(statData, "个", ScoreboardThread::unitConversion);


        }, betterScoreboard));
        scoreboardStates.add(new ScoreboardState("放置榜", "§6", betterScoreboard -> {


            //玩家名   状态
            LinkedHashMap<String, Integer> statData = new LinkedHashMap<>();


            //读取在线玩家状态
            for (ServerPlayerEntity player : Navigator.playerManager.getPlayerList()) {
                if ("127.0.0.1".equals(player.getIp()))//检测机器人
                    continue;
                String name = player.getName().getString();
                AtomicInteger stat = new AtomicInteger(0);
                Registries.BLOCK.forEach(block -> {
                    stat.addAndGet(player.getStatHandler().getStat(Stats.USED, block.asItem()));
                });


                statData.put(name, stat.get());
            }



            printRank(statData, "个", ScoreboardThread::unitConversion);


        }, betterScoreboard));
        scoreboardStates.add(new ScoreboardState("击杀榜", "§d", betterScoreboard -> {


            //玩家名   状态
            LinkedHashMap<String, Integer> statData = new LinkedHashMap<>();





            printRank(statData, "个", ScoreboardThread::unitConversion);

        }, betterScoreboard));
        scoreboardStates.add(new ScoreboardState("在线榜", "§f", betterScoreboard -> {

            //玩家名   状态
            LinkedHashMap<String, Integer> statData = new LinkedHashMap<>();





            printRank(statData, "", data -> timeUnitConversion(data / 20));

        }, betterScoreboard));
        scoreboardStates.add(new ScoreboardState("受伤榜", "§4", betterScoreboard -> {

            //玩家名   状态
            LinkedHashMap<String, Integer> statData = new LinkedHashMap<>();


            printRank(statData, "点", ScoreboardThread::unitConversion);

        }, betterScoreboard));
        scoreboardStates.add(new ScoreboardState("行程榜", "§1", betterScoreboard -> {
            //玩家名   状态
            LinkedHashMap<String, Integer> statData = new LinkedHashMap<>();





            printRank(statData, "km", data -> unitConversion(data / 1000));


        }, betterScoreboard));
        scoreboardStates.add(new ScoreboardState("交易榜", "§2", betterScoreboard -> {

            //玩家名   状态
            LinkedHashMap<String, Integer> statData = new LinkedHashMap<>();


            printRank(statData, "次", ScoreboardThread::unitConversion);

        }, betterScoreboard));


        while (true) {


            try {
                //检测是否可以切换下一个状态
                if (System.currentTimeMillis() - SWITCH_TIMEOUT > lastSwitchTime) {
                    stateIndex++;
                    stateIndex %= stateMap.length;
                    lastSwitchTime = System.currentTimeMillis();//跟新时间戳
                    nextStateIndex = stateIndex + 1;
                    nextStateIndex %= stateMap.length;
                    betterScoreboard.scoreObjects.removeAllScoreObject();//移除所有分数对象
                    if (stateMap[stateIndex] == 0) {
                        SWITCH_TIMEOUT = 5000;
                    } else {
                        SWITCH_TIMEOUT = 15000;
                    }
                }

                scoreboardStates.get(stateMap[stateIndex]).run(1f - 1f * (System.currentTimeMillis() - lastSwitchTime) / SWITCH_TIMEOUT);

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("计分板发生异常，但是无视~");
            }

        }
    }

    public static String unitConversion(Integer v) {
        int value = (v == null ? 0 : v);
        if (value < 1000) {
            return value + "";
        } else if (value < 1000000) {
            return new BigDecimal(value / 1000.).setScale(2, RoundingMode.HALF_UP) + "k";
        } else if (value < 1000000000) {
            return new BigDecimal(value / 1000000.).setScale(2, RoundingMode.HALF_UP) + "m";
        } else if (value < Integer.MAX_VALUE) {
            return new BigDecimal(value / 1000000000.).setScale(2, RoundingMode.HALF_UP) + "b";
        }
        return "我不道啊";
    }

    public static String getWorldTime() {
        long time = Navigator.playerManager.getServer().getOverworld().getTimeOfDay() % 24000;

        String sTime = Formatting.YELLOW + new SimpleDateFormat(" HH:mm").format(new Date((time + 22000) * 60 * 60));

        if (time < 6000) {
            return Formatting.GRAY + "上午" + sTime;
        } else if (time < 6500) {
            return Formatting.WHITE + "中午" + sTime;
        } else if (time < 12000) {
            return Formatting.GRAY + "下午" + sTime;
        } else if (time < 13800) {
            return Formatting.YELLOW + "日落" + sTime;
        } else if (time < 18000) {
            return Formatting.BLUE + "夜晚" + sTime;
        } else if (time < 22200) {
            return Formatting.DARK_BLUE + "午夜" + sTime;
        } else {
            return Formatting.YELLOW + "日出" + sTime;
        }
    }

    public static String getRunTime() {
        long time = Navigator.playerManager.getServer().getOverworld().getTime() / 20;
        long s = time % 60;
        long m = time / 60 % 60;
        long h = time / 60 / 60 % 24;
        long d = time / 60 / 60 / 24;
        return d + "天 " + h + "小时";
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
        return d + "d " + h + ":" + m + ":" + s;
    }


    private static final String[] percentageColors = new String[]{"§a", "§2", "§e", "§6", "§c", "§4"};

    public static String percentageToColor(double percentage) {
        for (int i = 0; i < 6; i++) {
            if (percentage <= (i + 1) * 1.66666 / 10) return percentageColors[i];
        }
        return "§4";
    }

    private static final float[] msptThreshold = new float[]{
            20, 30, 40, 45, 47, 50
    };

    public static String MSPTToColor(float mspt) {

        for (int i = 0; i < 6; i++) {
            if (mspt <= msptThreshold[i]) return percentageColors[i];
        }

        return "§4";
    }
}
