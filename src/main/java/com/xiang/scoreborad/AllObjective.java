package com.xiang.scoreborad;

import com.xiang.util.Info;

import java.util.HashMap;

import static com.xiang.scoreborad.BetterObjective.*;

public class AllObjective {
    public static HashMap<String, BetterObjective> objectiveMap = new HashMap<>();

    public static void initialize() {
        //底部的信息
        ObjectiveHandler bottomInfo = new ObjectiveHandler() {
            @Override
            public void onObjectiveUpdate(BetterObjective objective, int cycle) {
                objective.setScore(0, "MSPT : " + Info.getMSPT(), LEFT);
                objective.setScore(1, "cycle : " + cycle, LEFT);
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
