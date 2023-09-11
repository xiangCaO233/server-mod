package com.xiang;

import net.fabricmc.api.ModInitializer;

import net.minecraft.scoreboard.ScoreboardObjective;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * @author xiang2333
 */
public class ServerUtility implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "server-utility";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	//死亡数
	public static ScoreboardObjective deathCountObj;
	//挖掘数
	public static ScoreboardObjective minedCountObj;
	//放置数
	public static ScoreboardObjective placedBlocksObj;
	//交易数
	public static ScoreboardObjective tradeCountObj;
	//移动统计
	public static ScoreboardObjective moveStatisticsObj;

	//经验获取数
	public static ScoreboardObjective expGetCountObj;

	//击杀数
	public static ScoreboardObjective killCountObj;
	//列表
	public static ArrayList<ScoreboardObjective> scoreboardObjectives;
	//private static final HashMap<String , Integer> SCORES = new HashMap<>();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("server utility initializing");


		scoreboardObjectives = new ArrayList<>();
		scoreboardObjectives.add(deathCountObj);
		scoreboardObjectives.add(minedCountObj);
		scoreboardObjectives.add(placedBlocksObj);
		scoreboardObjectives.add(tradeCountObj);
		scoreboardObjectives.add(moveStatisticsObj);
		scoreboardObjectives.add(expGetCountObj);
		scoreboardObjectives.add(killCountObj);

	}

	/*public static void setScore(String name,int score){
		SCORES.put(name,score);
	}*/
}