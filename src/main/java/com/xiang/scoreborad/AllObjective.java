package com.xiang.scoreborad;

import com.xiang.util.Info;
import net.minecraft.util.Formatting;

import java.util.HashMap;

import static com.xiang.scoreborad.BetterObjective.*;

public class AllObjective {
    public static HashMap<String, BetterObjective> objectiveMap = new HashMap<>();

    public static void initialize() {
        //底部的信息
        ObjectiveHandler bottomInfo = new ObjectiveHandler() {
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


        BetterObjective mainInfo = new BetterObjective("mainInfo", "mainInfo", 15);
        mainInfo.addHeader(bottomInfo);

        objectiveMap.put("mainInfo", mainInfo);
    }

}
