package com.xiang.scoreborad;

import java.util.HashMap;

import static com.xiang.scoreborad.BetterObjective.*;

public class AllObjective {
    public static HashMap<String, BetterObjective> objectiveHashMap = new HashMap<>();

    public static void initialize() {

        ObjectiveHandler bottomInfo = new ObjectiveHandler() {
            @Override
            public void onObjectiveUpdate(BetterObjective objective, int cycle) {
                objective.setScore(0,"" , LEFT);
            }

            @Override
            public int getMaxCycle() {
                return 0;
            }
        };


        BetterObjective mainInfo = new BetterObjective("mainInfo", "mainInfo", 16);
        mainInfo.addHeader(bottomInfo);
    }

}
