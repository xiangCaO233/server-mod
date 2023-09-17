package com.xiang.scoreborad;

import com.xiang.ServerUtility;
import com.xiang.util.Info;
import com.xiang.util.SystemInfo;
import net.minecraft.util.Formatting;

import java.util.HashMap;

import static com.xiang.scoreborad.BetterObjective.*;

public class AllObjective {
    public static HashMap<String, BetterObjective> objectiveMap = new HashMap<>();
    public static final String[] titleEffect=//infinity heaven
            new String[]{
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
                    "§r§a §b§l infinity§e§l heaven§r§a ",
                    "§r§c§k-§r §b§l infinity§e§l heaven§r§a §c§k-",
                    "§r§c§k-§r§a- §b§l infinity§e§l heaven§r§a -§c§k-",
                    "§r§c§k-§r§a-- §b§l infinity§e§l heaven§r§a --§c§k-",
                    "§r§c§k-§r§a--- §b§l infinity§e§l heaven§r§a ---§c§k-",
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

    public static void initialize() {
        //计分项的标题特效 无
        ObjectiveHandler titleHandler=new ObjectiveHandler() {
            @Override
            public void onObjectiveUpdate(BetterObjective objective, int cycle) {
                objective.setObjectiveTitleName(
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

                objective.setScore(3, "§6---------------------------" , LEFT);
                objective.setScore(2, "时间 : " + BetterObjective.format(Info.getWorldTime(),10,CENTER),LEFT);
                objective.setScore(1, "CPU : " + BetterObjective.format(Info.formatPercentage(SystemInfo.cpuUsedPercentage),10,CENTER)
                        + " RAM : " + BetterObjective.format(Info.formatPercentage(SystemInfo.ramUsedPercentage),10,CENTER), LEFT);
                objective.setScore(0, "TPS : " + BetterObjective.format(Info.getTPS(),10,CENTER)
                                +" MSPT : " + BetterObjective.format(Info.getMSPT(),10,CENTER)
                        , LEFT);

            }

            @Override
            public int getMaxCycle() {
                return 10;
            }
        };

        //服务器信息 4-11
        ObjectiveHandler serverInfoHandler=new ObjectiveHandler() {
            @Override
            public void onObjectiveUpdate(BetterObjective objective, int cycle) {



                objective.setScore(11,"-- 服务器信息 --",CENTER);
                objective.setScore(10,"欢迎来到 §6[ "+(cycle%8<4?"§b§lI§e§lH":"§e§lI§b§lH")+" §r§6]§f 服务器!",LEFT);
                {
                    StringBuilder stringBuilder=new StringBuilder("937403431          ");
                    stringBuilder.insert(cycle+3,"§b");
                    stringBuilder.insert(cycle,"§f");
                    objective.setScore(9,"我们的群 : §b"+stringBuilder,LEFT);
                }
                objective.setScore(8," Alona  : §c未连接",LEFT);
                {
                    StringBuilder stringBuilder=new StringBuilder("zedo.top:1234          ");
                    stringBuilder.insert(cycle+3,"§b");
                    stringBuilder.insert(cycle,"§f");
                    objective.setScore(7,"开放地址 : §b"+stringBuilder,LEFT);
                }

                objective.setScore(6,"运行时长 : §e"+Info.getRunTime(),LEFT);
                objective.setScore(5,"在线人数 : §e"+ ServerUtility.onlinePlayers.size()+"人",LEFT);
                objective.setScore(4,"游戏版本 : §e1.20.1",LEFT);

            }

            @Override
            public int getMaxCycle() {
                return 16;
            }
        };



        BetterObjective mainInfo = new BetterObjective("mainInfo", "mainInfo", 12);
        mainInfo.addHeader(bottomInfoHandler);
        mainInfo.addHeader(titleHandler);
        mainInfo.addHeader(serverInfoHandler);

        objectiveMap.put("mainInfo", mainInfo);
    }

}
