package com.xiang;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.advancement.criterion.VillagerTradeCriterion;
import net.minecraft.datafixer.fix.VillagerTradeFix;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.profiling.jfr.sample.FileIoSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * @author xiang2333
 */
public class ServerUtility implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "server-utility";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static File config = new File("config/sudata.cfg");
	public static Properties prop;
	public static int scoreboardObjectiveIndex=0;
	//生命值
	public static ScoreboardObjective healthObj;
	//死亡数
	public static ScoreboardObjective deathCountObj;
	//挖掘数
	public static ScoreboardObjective minedCountObj;
	//放置数
	public static ScoreboardObjective placedCountObj;
	//交易数
	public static ScoreboardObjective tradeCountObj;
	//移动统计
	public static ScoreboardObjective moveDistanceObj;
	public static HashMap<String,Double> moveStatisticMap;
	//经验获取数
	public static ScoreboardObjective expGetCountObj;
	//伤害榜
	public static ScoreboardObjective damageObj;
	public static HashMap<String,Float> damageStatisticMap;
	//受伤榜
	public static ScoreboardObjective takeDamageObj;
	public static HashMap<String,Float> takeDamageStatisticMap;
	//击杀数
	public static ScoreboardObjective killCountObj;
	//等级榜
	public static ScoreboardObjective levelObj;
	//榜列表
	public static ArrayList<ScoreboardObjective> scoreboardObjectives;

	@Override
	public void onInitialize() {
		LOGGER.info("server utility initializing");
		prop = new Properties();
		moveStatisticMap = new HashMap<>();
		damageStatisticMap = new HashMap<>();
		takeDamageStatisticMap = new HashMap<>();
		scoreboardObjectives = new ArrayList<>();
	}

}