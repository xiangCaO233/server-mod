package com.xiang.scoreborad;

import com.xiang.ServerUtility;
import com.xiang.alona.AlonaThread;
import com.xiang.util.Info;
import com.xiang.util.SystemInfo;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.xiang.ServerUtility.*;

import java.util.*;

import static com.xiang.scoreborad.BetterObjective.*;

public class AllObjective {
    //infinity heaven
    private static HashMap<String, BetterObjective> objectiveMap = new HashMap<>();
    private static final String[] titleEffect = new String[]{
            "§b§l §6§ni",
            "§b§l i§6§nn",
            "§b§l in§6§nf",
            "§b§l inf§6§ni",
            "§b§l infi§6§nn",
            "§b§l infin§6§ni",
            "§b§l infini§6§nt",
            "§b§l infinit§6§ny",
            "§b§l infinity§e§l §6§nh",
            "§b§l infinity§e§l h§6§ne",
            "§b§l infinity§e§l he§6§na",
            "§b§l infinity§e§l hea§6§nv",
            "§b§l infinity§e§l heav§6§ne",
            "§b§l infinity§e§l heave§6§nn",
            "§b§l infinity§e§l heaven§r§a",
            "§r§c§k-§r§b§l infinity§e§l heaven§r§a §c§k-",
            "§r§c§k-§r§a-§b§l infinity§e§l heaven§r§a -§c§k-",
            "§r§c§k-§r§a--§b§l infinity§e§l heaven§r§a --§c§k-",
            "§r§c§k-§r§a---§b§l infinity§e§l heaven§r§a ---§c§k-",
            "§r§a----§b§l infinity§e§l heaven§r§a ----",
            "§r§a----§b§l infinity§e§l heaven§r§a ----",
            "§r§a----§b§l infinity§e§l heaven§r§a ----",
            "§r§a----§b§l infinity§e§l heaven§r§a ----",
            "§r§a----§b§l infinity§e§l heaven§r§a ----",
            "§r§a----§b§l infinity§e§l heaven§r§a ----",
            "§r§a----§b§l infinity§e§l heaven§r§a ----",
            "§r§a----§b§l §f§lin§b§lfinity§e§l heaven§r§a ----",
            "§r§a----§b§l in§f§lfi§b§lnity§e§l heaven§r§a ----",
            "§r§a----§b§l infi§f§lni§b§lty§e§l heaven§r§a ----",
            "§r§a----§b§l infini§f§lty§b§l§e§l heaven§r§a ----",
            "§r§a----§b§l infinity§e§l §f§lhe§e§laven§r§a ----",
            "§r§a----§b§l infinity§e§l he§f§lav§e§len§r§a ----",
            "§r§a----§b§l infinity§e§l heav§f§len§e§l§r§a ----",
            "§r§a----§b§l infinity§e§l heaven§r§a ----",
            "§r§a----§b§l infinity§e§l heaven§r§a ----",
            "§r§a----§b§l infinity§e§l heaven§r§a ----",
            "§r§a----§b§l infinity§e§l heaven§r§a ----",
            "§r§a----§b§l infinity§e§l heaven§r§a ----",
            "§r§a----§b§l infinity§e§l heaven§r§a ----",
            "§r§a----§b§l infinity§e§l heav§f§len§e§l§r§a ----",
            "§r§a----§b§l infinity§e§l he§f§lav§e§len§r§a ----",
            "§r§a----§b§l infinity§e§l §f§lhe§e§laven§r§a ----",
            "§r§a----§b§l infini§f§lty§b§l§e§l heaven§r§a ----",
            "§r§a----§b§l infi§f§lni§b§lty§e§l heaven§r§a ----",
            "§r§a----§b§l in§f§lfi§b§lnity§e§l heaven§r§a ----",
            "§r§a----§b§l §f§lin§b§lfinity§e§l heaven§r§a ----",
            "§r§a----§b§l infinity§e§l heaven§r§a ----",
            "§r§a----§b§l infinity§e§l heaven§r§a ----",
            "§r§a----§b§l infinity§e§l heaven§r§a ----",
            "§r§a----§b§l infinity§e§l heaven§r§a ----",
            "§r§a----§b§l infinity§e§l heaven§r§a ----",
            "§r§a----§b§l infinity§e§l heaven§r§a ----",
            "§r§a----§4§l§k in§b§l§kf§4§l§kin§b§l§kity§r§e§l heaven§r§a ----",
            "§r§a---§b§l§k inf§4§l§kit§b§l§ky§r§e§l heav§b§l§ke§4§l§kn§r§a ---",
            "§r§a--§4§l§k iny§4§l§k hea§b§l§kven§r§r§a --",
            "§r§a-§4§l§k hea§b§l§kven§r§a -",
            "§r§a§k§4§l §4§l§k he§b§l§ka§4§l§kn§r§a",
            "§r§a§k§4§l §4§l§k h§r§a",
            "",
            "",
            ""
    };

    public static ArrayDeque<ObjectiveHandler> autoLoops = new ArrayDeque<>();

    public static BetterObjective autoLoopObjective;

    public static void initialize() {

        //计分项的标题特效 无
        ObjectiveHandler titleHandler = new ObjectiveHandler() {
            @Override
            public void onObjectiveUpdate(BetterObjective objective, int cycle) {
                objective.setObjectiveTitle(
                        titleEffect[cycle]
                );
            }

            @Override
            public int getMaxCycle() {
                return titleEffect.length;
            }
        };
        //底部的信息 0-3
        ObjectiveHandler bottomInfoHandler = new ObjectiveHandler() {
            @Override
            public void onObjectiveUpdate(BetterObjective objective, int cycle) {

                objective.setScore(3, "§6---------------------------", LEFT);
                objective.setScore(2, "时间 : " + BetterObjective.format(Info.getWorldTime(), 10, CENTER), LEFT);
                objective.setScore(1, "CPU : " + BetterObjective.format(Info.formatPercentage(SystemInfo.cpuUsedPercentage), 10, CENTER)
                        + " RAM : " + BetterObjective.format(Info.formatPercentage(SystemInfo.ramUsedPercentage), 10, CENTER), LEFT);
                objective.setScore(0, "TPS : " + BetterObjective.format(Info.getTPS(), 10, CENTER)
                                + " MSPT : " + BetterObjective.format(Info.getMSPT(), 10, CENTER)
                        , LEFT);

            }

            @Override
            public int getMaxCycle() {
                return 10;
            }
        };
        //服务器信息 4-11
        ObjectiveHandler serverInfoHandler = new ObjectiveHandler() {
            @Override
            public void onObjectiveUpdate(BetterObjective objective, int cycle) {
                {
                    StringBuilder stringBuilder = new StringBuilder("-- 服务器信息 --       ");
                    stringBuilder.insert(cycle + 2, "§b§l");
                    stringBuilder.insert(cycle, "§e§l");
                    objective.setScore(11, "§b§l" + stringBuilder.toString().replaceAll(" ", ""), CENTER);
                }

                objective.setScore(10, "欢迎来到 §6[ " + (cycle % 8 < 4 ? "§b§lI§e§lH" : "§e§lI§b§lH") + " §r§6]§f 服务器!", LEFT);
                {
                    StringBuilder stringBuilder = new StringBuilder("937403431          ");
                    stringBuilder.insert(cycle + 3, "§b");
                    stringBuilder.insert(cycle, "§f");
                    objective.setScore(9, "我们的群 : §b" + stringBuilder, LEFT);
                }
                objective.setScore(8, " Alona  : " + (AlonaThread.isConnect ? "§a已连接" : (cycle % 8 < 4 ? "§4未连接" : "§c未连接")), LEFT);
                {
                    StringBuilder stringBuilder = new StringBuilder("zedo.top:1234          ");
                    stringBuilder.insert(cycle + 3, "§b");
                    stringBuilder.insert(cycle, "§f");
                    objective.setScore(7, "开放地址 : §b" + stringBuilder, LEFT);
                }

                objective.setScore(6, "运行时长 : §e" + Info.getRunTime(true), LEFT);
                objective.setScore(5, "在线人数 : §e" + ServerUtility.onlinePlayers.size() + "人", LEFT);
                objective.setScore(4, "游戏版本 : §e1.20.1", LEFT);

            }

            @Override
            public int getMaxCycle() {
                return 16;
            }
        };
        //死亡排行榜 4-11   deathsStatisticMap
        ObjectiveHandler deathRankingHandler = new ObjectiveHandler() {
            @Override
            public void onObjectiveUpdate(BetterObjective objective, int cycle) {

                {
                    StringBuilder stringBuilder = new StringBuilder("--死亡排行榜--     ");
                    stringBuilder.insert(cycle + 2, "§c§l");
                    stringBuilder.insert(cycle, "§4§l");
                    objective.setScore(11, "§c§l" + stringBuilder.toString().replaceAll(" ", ""), CENTER);
                }
                printRankInteger(objective, deathsStatisticMap, "次");
            }

            @Override
            public int getMaxCycle() {
                return 12;
            }
        };
        //挖掘数排行榜
        ObjectiveHandler minedRankingHandler = new ObjectiveHandler() {
            @Override
            public void onObjectiveUpdate(BetterObjective objective, int cycle) {

                {
                    StringBuilder stringBuilder = new StringBuilder("--挖掘排行榜--     ");
                    stringBuilder.insert(cycle + 2, "§e§l");
                    stringBuilder.insert(cycle, "§f§l");
                    objective.setScore(11, "§e§l" + stringBuilder.toString().replaceAll(" ", ""), CENTER);
                }

                printRankInteger(objective, minedCountStatisticMap, "个");
            }

            @Override
            public int getMaxCycle() {
                return 12;
            }
        };
        //放置数排行榜
        ObjectiveHandler placedRankingHandler = new ObjectiveHandler() {
            @Override
            public void onObjectiveUpdate(BetterObjective objective, int cycle) {

                {
                    StringBuilder stringBuilder = new StringBuilder("--放置排行榜--     ");
                    stringBuilder.insert(cycle + 2, "§6§l");
                    stringBuilder.insert(cycle, "§e§l");
                    objective.setScore(11, "§6§l" + stringBuilder.toString().replaceAll(" ", ""), CENTER);
                }

                printRankInteger(objective, placedCountStatisticMap, "个");
            }

            @Override
            public int getMaxCycle() {
                return 12;
            }
        };
        //交易数排行榜
        ObjectiveHandler tradeRankingHandler = new ObjectiveHandler() {
            @Override
            public void onObjectiveUpdate(BetterObjective objective, int cycle) {

                {
                    StringBuilder stringBuilder = new StringBuilder("--交易排行榜--     ");
                    stringBuilder.insert(cycle + 2, "§2§l");
                    stringBuilder.insert(cycle, "§a§l");
                    objective.setScore(11, "§2§l" + stringBuilder.toString().replaceAll(" ", ""), CENTER);
                }

                printRankInteger(objective, tradeCountStatisticMap, "次");
            }

            @Override
            public int getMaxCycle() {
                return 12;
            }
        };
        //移动距离排行榜
        ObjectiveHandler moveRankingHandler = new ObjectiveHandler() {
            @Override
            public void onObjectiveUpdate(BetterObjective objective, int cycle) {

                {
                    StringBuilder stringBuilder = new StringBuilder("--移动排行榜--     ");
                    stringBuilder.insert(cycle + 2, "§1§l");
                    stringBuilder.insert(cycle, "§9§l");
                    objective.setScore(11, "§1§l" + stringBuilder.toString().replaceAll(" ", ""), CENTER);
                }

                printRankDouble(objective, moveStatisticMap, "m");
            }

            @Override
            public int getMaxCycle() {
                return 12;
            }
        };
        //经验获取排行榜
        ObjectiveHandler expGetRankingHandler = new ObjectiveHandler() {
            @Override
            public void onObjectiveUpdate(BetterObjective objective, int cycle) {

                {
                    StringBuilder stringBuilder = new StringBuilder("--经验排行榜--     ");
                    stringBuilder.insert(cycle + 2, "§a§l");
                    stringBuilder.insert(cycle, "§2§l");
                    objective.setScore(11, "§a§l" + stringBuilder.toString().replaceAll(" ", ""), CENTER);
                }

                printRankInteger(objective, expGetCountStatisticMap, "点");
            }

            @Override
            public int getMaxCycle() {
                return 12;
            }
        };
        //击杀数排行榜
        ObjectiveHandler killRankingHandler = new ObjectiveHandler() {
            @Override
            public void onObjectiveUpdate(BetterObjective objective, int cycle) {

                {
                    StringBuilder stringBuilder = new StringBuilder("--击杀排行榜--     ");
                    stringBuilder.insert(cycle + 2, "§d§l");
                    stringBuilder.insert(cycle, "§5§l");
                    objective.setScore(11, "§d§l" + stringBuilder.toString().replaceAll(" ", ""), CENTER);
                }

                printRankInteger(objective, killCountStatisticMap, "个");
            }

            @Override
            public int getMaxCycle() {
                return 12;
            }
        };
        //伤害排行榜
        ObjectiveHandler damageRankingHandler = new ObjectiveHandler() {
            @Override
            public void onObjectiveUpdate(BetterObjective objective, int cycle) {

                {
                    StringBuilder stringBuilder = new StringBuilder("--伤害排行榜--     ");
                    stringBuilder.insert(cycle + 2, "§c§l");
                    stringBuilder.insert(cycle, "§4§l");
                    objective.setScore(11, "§c§l" + stringBuilder.toString().replaceAll(" ", ""), CENTER);
                }

                printRankFloatHealth(objective, damageStatisticMap, "点");
            }

            @Override
            public int getMaxCycle() {
                return 12;
            }
        };
        //受伤排行榜
        ObjectiveHandler takeDamageRankingHandler = new ObjectiveHandler() {
            @Override
            public void onObjectiveUpdate(BetterObjective objective, int cycle) {

                {
                    StringBuilder stringBuilder = new StringBuilder("--受伤排行榜--     ");
                    stringBuilder.insert(cycle + 2, "§4§l");
                    stringBuilder.insert(cycle, "§c§l");
                    objective.setScore(11, "§4§l" + stringBuilder.toString().replaceAll(" ", ""), CENTER);
                }

                printRankFloatHealth(objective, takeDamageStatisticMap, "点");
            }

            @Override
            public int getMaxCycle() {
                return 12;
            }
        };
        //在线排行榜
        ObjectiveHandler onlineRankingHandler = new ObjectiveHandler() {
            @Override
            public void onObjectiveUpdate(BetterObjective objective, int cycle) {

                {
                    StringBuilder stringBuilder = new StringBuilder("--在线排行榜--     ");
                    stringBuilder.insert(cycle + 2, "§f§l");
                    stringBuilder.insert(cycle, "§7§l");
                    objective.setScore(11, "§f§l" + stringBuilder.toString().replaceAll(" ", ""), CENTER);
                }

                printRankIntegerTime(objective, onlineStatisticMap);
            }

            @Override
            public int getMaxCycle() {
                return 12;
            }
        };
        //等级排行榜
        ObjectiveHandler levelRankingHandler = new ObjectiveHandler() {
            @Override
            public void onObjectiveUpdate(BetterObjective objective, int cycle) {

                {
                    StringBuilder stringBuilder = new StringBuilder("--等级排行榜--     ");
                    stringBuilder.insert(cycle + 2, "§a§l");
                    stringBuilder.insert(cycle, "§2§l");
                    objective.setScore(11, "§a§l" + stringBuilder.toString().replaceAll(" ", ""), CENTER);
                }

                printRankInteger(objective, levelMap, "级");
            }

            @Override
            public int getMaxCycle() {
                return 12;
            }
        };


        BetterObjective serverInfoObjective = new BetterObjective("serverInfoObjective", "serverInfoObjective", 12);
        serverInfoObjective.addHeader(bottomInfoHandler);
        serverInfoObjective.addHeader(titleHandler);
        serverInfoObjective.addHeader(serverInfoHandler);

        BetterObjective deathRankingObjective = new BetterObjective("deathRankingObjective", "deathRankingObjective", 12);
        deathRankingObjective.addHeader(bottomInfoHandler);
        deathRankingObjective.addHeader(titleHandler);
        deathRankingObjective.addHeader(deathRankingHandler);

        BetterObjective minedRankingObjective = new BetterObjective("minedRankingObjective", "minedRankingObjective", 12);
        minedRankingObjective.addHeader(bottomInfoHandler);
        minedRankingObjective.addHeader(titleHandler);
        minedRankingObjective.addHeader(minedRankingHandler);

        BetterObjective placedRankingObjective = new BetterObjective("placedRankingObjective", "placedRankingObjective", 12);
        placedRankingObjective.addHeader(bottomInfoHandler);
        placedRankingObjective.addHeader(titleHandler);
        placedRankingObjective.addHeader(placedRankingHandler);

        BetterObjective tradeRankingObjective = new BetterObjective("tradeRankingObjective", "tradeRankingObjective", 12);
        tradeRankingObjective.addHeader(bottomInfoHandler);
        tradeRankingObjective.addHeader(titleHandler);
        tradeRankingObjective.addHeader(tradeRankingHandler);

        BetterObjective moveRankingObjective = new BetterObjective("moveRankingObjective", "moveRankingObjective", 12);
        moveRankingObjective.addHeader(bottomInfoHandler);
        moveRankingObjective.addHeader(titleHandler);
        moveRankingObjective.addHeader(moveRankingHandler);

        BetterObjective expGetRankingObjective = new BetterObjective("expGetRankingObjective", "expGetRankingObjective", 12);
        expGetRankingObjective.addHeader(bottomInfoHandler);
        expGetRankingObjective.addHeader(titleHandler);
        expGetRankingObjective.addHeader(expGetRankingHandler);

        BetterObjective killRankingObjective = new BetterObjective("killRankingObjective", "killRankingObjective", 12);
        killRankingObjective.addHeader(bottomInfoHandler);
        killRankingObjective.addHeader(titleHandler);
        killRankingObjective.addHeader(killRankingHandler);

        BetterObjective damageRankingObjective = new BetterObjective("damageRankingObjective", "damageRankingObjective", 12);
        damageRankingObjective.addHeader(bottomInfoHandler);
        damageRankingObjective.addHeader(titleHandler);
        damageRankingObjective.addHeader(damageRankingHandler);

        BetterObjective takeDamageRankingObjective = new BetterObjective("takeDamageRankingObjective", "takeDamageRankingObjective", 12);
        takeDamageRankingObjective.addHeader(bottomInfoHandler);
        takeDamageRankingObjective.addHeader(titleHandler);
        takeDamageRankingObjective.addHeader(takeDamageRankingHandler);

        BetterObjective onlineRankingObjective = new BetterObjective("onlineRankingObjective", "onlineRankingObjective", 12);
        onlineRankingObjective.addHeader(bottomInfoHandler);
        onlineRankingObjective.addHeader(titleHandler);
        onlineRankingObjective.addHeader(onlineRankingHandler);

        BetterObjective levelRankingObjective = new BetterObjective("levelRankingObjective", "levelRankingObjective", 12);
        levelRankingObjective.addHeader(bottomInfoHandler);
        levelRankingObjective.addHeader(titleHandler);
        levelRankingObjective.addHeader(levelRankingHandler);

        autoLoops.add(serverInfoHandler);
        autoLoops.add(deathRankingHandler);
        autoLoops.add(serverInfoHandler);
        autoLoops.add(minedRankingHandler);
        autoLoops.add(serverInfoHandler);
        autoLoops.add(placedRankingHandler);
        autoLoops.add(serverInfoHandler);
        autoLoops.add(tradeRankingHandler);
        autoLoops.add(serverInfoHandler);
        autoLoops.add(moveRankingHandler);
        autoLoops.add(serverInfoHandler);
        autoLoops.add(expGetRankingHandler);
        autoLoops.add(serverInfoHandler);
        autoLoops.add(killRankingHandler);
        autoLoops.add(serverInfoHandler);
        autoLoops.add(damageRankingHandler);
        autoLoops.add(serverInfoHandler);
        autoLoops.add(takeDamageRankingHandler);
        autoLoops.add(serverInfoHandler);
        autoLoops.add(onlineRankingHandler);
        autoLoops.add(serverInfoHandler);
        autoLoops.add(levelRankingHandler);

        autoLoopObjective = new BetterObjective("autoLoopObjective", "autoLoopObjective", 12);
        autoLoopObjective.addHeader(bottomInfoHandler);
        autoLoopObjective.addHeader(titleHandler);
        autoLoopObjective.addHeader(onlineRankingHandler);

        objectiveMap.put("serverInfo", serverInfoObjective);
        objectiveMap.put("deathRanking", deathRankingObjective);
        objectiveMap.put("minedRanking", minedRankingObjective);
        objectiveMap.put("placedRanking", placedRankingObjective);
        objectiveMap.put("tradeRanking", tradeRankingObjective);
        objectiveMap.put("moveRanking", moveRankingObjective);
        objectiveMap.put("expGetRanking", expGetRankingObjective);
        objectiveMap.put("killRanking", killRankingObjective);
        objectiveMap.put("damageRanking", damageRankingObjective);
        objectiveMap.put("takeDamageRanking", takeDamageRankingObjective);
        objectiveMap.put("onlineRanking", onlineRankingObjective);
        objectiveMap.put("levelRanking", levelRankingObjective);
        objectiveMap.put("autoLoop", autoLoopObjective);
    }

    public static boolean setPlayerObjective(ServerPlayerEntity player, String objectiveName) {
        BetterObjective target = objectiveMap.get(objectiveName);
        if (target == null)
            return false;
        for (BetterObjective objective : objectiveMap.values()) {
            objective.removePlayer(player);
        }
        target.addPlayer(player);
        return true;
    }

    public static Set<String> getObjectiveNames() {
        return objectiveMap.keySet();
    }

    public static Collection<BetterObjective> getObjectives() {
        return objectiveMap.values();
    }

    private static void printRankDouble(BetterObjective objective, HashMap<UUID, Double> placedCountStatisticMap, String unit) {
        ArrayList<Map.Entry<UUID, Double>> rankingList = sortRankingDouble(placedCountStatisticMap);
        for (int i = 0; i < 7; i++) {
            String color = "§e";
            if (i == 0)
                color = "§6";
            if (i == 1)
                color = "§b";
            if (i == 2)
                color = "§a";

            if (rankingList.size() > i) {
                Map.Entry<UUID, Double> playerStat = rankingList.get(rankingList.size() - 1 - i);
                String playerName = playerNameMapping.get(playerStat.getKey());
                objective.setScore(10 - i, color + (i + 1) + " " +
                        BetterObjective.format(playerName, 16, LEFT) +
                        BetterObjective.format(Info.unitConversion(playerStat.getValue().intValue()) + unit, 14, RIGHT), LEFT);
            } else {
                objective.setScore(10 - i, color + (i + 1) + " 暂无", LEFT);
            }

        }
    }

    private static void printRankInteger(BetterObjective objective, HashMap<UUID, Integer> placedCountStatisticMap, String unit) {
        ArrayList<Map.Entry<UUID, Integer>> rankingList = sortRankingInteger(placedCountStatisticMap);
        for (int i = 0; i < 7; i++) {
            String color = "§e";
            if (i == 0)
                color = "§6";
            if (i == 1)
                color = "§b";
            if (i == 2)
                color = "§a";

            if (rankingList.size() > i) {
                Map.Entry<UUID, Integer> playerStat = rankingList.get(rankingList.size() - 1 - i);
                String playerName = playerNameMapping.get(playerStat.getKey());
                objective.setScore(10 - i, color + (i + 1) + " " +
                        BetterObjective.format(playerName, 16, LEFT) +
                        BetterObjective.format(Info.unitConversion(playerStat.getValue()) + unit, 14, RIGHT), LEFT);
            } else {
                objective.setScore(10 - i, color + (i + 1) + " 暂无", LEFT);
            }

        }
    }

    private static void printRankIntegerTime(BetterObjective objective, HashMap<UUID, Integer> placedCountStatisticMap) {
        ArrayList<Map.Entry<UUID, Integer>> rankingList = sortRankingInteger(placedCountStatisticMap);
        for (int i = 0; i < 7; i++) {
            String color = "§e";
            if (i == 0)
                color = "§6";
            if (i == 1)
                color = "§b";
            if (i == 2)
                color = "§a";

            if (rankingList.size() > i) {
                Map.Entry<UUID, Integer> playerStat = rankingList.get(rankingList.size() - 1 - i);
                String playerName = playerNameMapping.get(playerStat.getKey());
                if (playerName == null) {
                    playerName = "未知玩家";
                }
                objective.setScore(10 - i, color + (i + 1) + " " +
                        BetterObjective.format(playerName, 16, LEFT) +
                        BetterObjective.format(Info.timeUnitConversion(playerStat.getValue()), 14, RIGHT), LEFT);
            } else {
                objective.setScore(10 - i, color + (i + 1) + " 暂无", LEFT);
            }

        }
    }

    private static void printRankFloatHealth(BetterObjective objective, HashMap<UUID, Float> placedCountStatisticMap, String unit) {
        ArrayList<Map.Entry<UUID, Float>> rankingList = sortRankingFloat(placedCountStatisticMap);
        for (int i = 0; i < 7; i++) {
            String color = "§e";
            if (i == 0)
                color = "§6";
            if (i == 1)
                color = "§b";
            if (i == 2)
                color = "§a";

            if (rankingList.size() > i) {
                Map.Entry<UUID, Float> playerStat = rankingList.get(rankingList.size() - 1 - i);
                String playerName = playerNameMapping.get(playerStat.getKey());
                objective.setScore(10 - i, color + (i + 1) + " " +
                        BetterObjective.format(playerName, 16, LEFT) +
                        BetterObjective.format(Info.unitConversion(playerStat.getValue() * 2) + unit, 14, RIGHT), LEFT);
            } else {
                objective.setScore(10 - i, color + (i + 1) + " 暂无", LEFT);
            }

        }
    }

    public static ArrayList<Map.Entry<UUID, Integer>> sortRankingInteger(HashMap<UUID, Integer> statData) {
        ArrayList<Map.Entry<UUID, Integer>> sortStatData = new ArrayList<>(statData.entrySet());
        sortStatData.sort(Comparator.comparingInt(Map.Entry::getValue));
        return sortStatData;
    }

    public static ArrayList<Map.Entry<UUID, Float>> sortRankingFloat(HashMap<UUID, Float> statData) {
        ArrayList<Map.Entry<UUID, Float>> sortStatData = new ArrayList<>(statData.entrySet());
        sortStatData.sort(Comparator.comparingDouble(Map.Entry::getValue));
        return sortStatData;
    }

    public static ArrayList<Map.Entry<UUID, Double>> sortRankingDouble(HashMap<UUID, Double> statData) {
        ArrayList<Map.Entry<UUID, Double>> sortStatData = new ArrayList<>(statData.entrySet());
        sortStatData.sort(Comparator.comparingDouble(Map.Entry::getValue));
        return sortStatData;
    }


    /*public void printRanking(HashMap<String, ?> statData, String unit, PrintRankConversion conversion) {
        AtomicInteger index = new AtomicInteger();

        if (statData instanceof HashMap<String,Integer> integerHashMap){

        }
        ArrayList<Map.Entry<String, Integer>> sortStatData = new ArrayList<>(statData.entrySet());
        sortStatData.sort(Comparator.comparingInt(Map.Entry::getValue));


        for (int i = 0; i < 9; i++) {
            int score = 14 - (i);
            if (sortStatData.size() - 1 < i) {
                betterScoreboard.scoreObjects.changeScoreObject("空" + score, "            ".substring(i), score);
                continue;
            }
            Map.Entry<String, Integer> sortStat = sortStatData.get(sortStatData.size() - 1 - i);
            String name = sortStat.getKey();
            String color = "§e";
            if (i == 0)
                color = "§6";
            if (i == 1)
                color = "§b";
            if (i == 2)
                color = "§a";


            betterScoreboard.scoreObjects.changeScoreObject(name,
                    color + ("#" + (i + 1) + "  ").substring(0, 3) + " " +
                            (name + "                 ").substring(0, 16) + " " +
                            conversion.conversion(sortStat.getValue()) + unit,
                    score);

        }
    }*/

}
