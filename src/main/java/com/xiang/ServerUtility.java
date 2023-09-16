package com.xiang;

import com.xiang.scoreborad_old.BetterScoreboard;
import net.fabricmc.api.ModInitializer;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.xiang.navigate.Navigator.playerManager;

/**
 * @author xiang2333
 */
public class ServerUtility implements ModInitializer {
	public static final String MOD_ID = "server-utility";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

//	public static String lastMsptName;
	public static File config;
	public static File backupsPath;
	public static File worldPath;
	public static Properties prop;
	//记录来过的玩家
	public static ArrayList<String> usedPlayers;
//	public static ArrayList<String> autoScoreBoardPlayers;
//	static Thread updateScoreboardTimer;
	static Thread backupTimer;
//	public static ServerScoreboard serverScoreboard;
	public static BetterScoreboard betterScoreboard;
//	public static boolean stopScoreboardT;
	public static boolean stopBackupT;
//	public static int scoreboardObjectiveIndex=0;
//	//生命值
	public static ScoreboardObjective healthObj;
//	//死亡数
//	public static ScoreboardObjective deathCountObj;
//	//挖掘数
//	public static ScoreboardObjective minedCountObj;
//	//放置数
//	public static ScoreboardObjective placedCountObj;
//	//交易数
//	public static ScoreboardObjective tradeCountObj;
//	//移动统计
//	public static ScoreboardObjective moveDistanceObj;
	/**
	 * 玩家移动统计缓存
	 */
	public static HashMap<String,Double> moveStatisticMap;
//	//经验获取数
//	public static ScoreboardObjective expGetCountObj;
//	//伤害榜
//	public static ScoreboardObjective damageObj;
	/**
	 * 玩家输出伤害缓存
	 */
	public static HashMap<String,Float> damageStatisticMap;
//	//受伤榜
//	public static ScoreboardObjective takeDamageObj;
	/**
	 * 玩家承受伤害缓存
	 */
	public static HashMap<String,Float> takeDamageStatisticMap;
//	//击杀数
//	public static ScoreboardObjective killCountObj;
//	//等级榜
//	public static ScoreboardObjective levelObj;
//	//榜列表
//	public static ArrayList<ScoreboardObjective> scoreboardObjectives;

	@Override
	public void onInitialize() {
		LOGGER.info("server utility initializing");
//		lastMsptName="";
		prop = new Properties();
//		stopScoreboardT = true;
		usedPlayers = new ArrayList<>();
//		autoScoreBoardPlayers = new ArrayList<>();
		moveStatisticMap = new HashMap<>();
		damageStatisticMap = new HashMap<>();
		takeDamageStatisticMap = new HashMap<>();
//		scoreboardObjectives = new ArrayList<>();

		config = new File("config/sudata.cfg");
		File configPath = config.getParentFile();
		backupsPath = new File("backups");
		worldPath = new File("world");



		//检查配置文件路径
		if (!configPath.exists()){
			configPath.mkdir();
		}
		//检查配置文件
		if (!config.exists()) {
			try {
				config.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		//加载配置文件
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(config);
			prop.load(fis);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		//检查备份文件夹目录
		if (!(backupsPath.exists()&& backupsPath.isDirectory())){
			backupsPath.mkdir();
		}
		//初始化better计分板
		betterScoreboard = new BetterScoreboard("LifeGarden","--LifeGarden--");
	}

	public static void startBackupTimer(){
		backupTimer = new Thread(()->{
			while (!stopBackupT){
				playerManager.broadcast(Text.of("开始备份"), false);
				playerManager.broadcast(Text.of(("备份花费:" + ServerUtility.createBackup() + "ms")), false);
				try {
					Thread.sleep(60000*15);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

/*	public static void startScoreBoardTimer() {

		updateScoreboardTimer = new Thread(() -> {
			while (!stopScoreboardT) {
				for (ServerPlayerEntity player : playerManager.getPlayerList()) {
					ScoreboardObjective objective = scoreboardObjectives.get(scoreboardObjectiveIndex);
					if (objective != null) {
						player.getScoreboard().setObjectiveSlot(Scoreboard.getDisplaySlotId("sidebar"), objective);
					}
				}
				if (scoreboardObjectiveIndex >= scoreboardObjectives.size() - 1) {
					scoreboardObjectiveIndex = 0;
				} else {
					scoreboardObjectiveIndex++;
				}
				try {
					//随机10~15s
					Thread.sleep(
							new Random().nextInt(10000, 15000)
					);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		});
		//updateScoreboardTimer.start();
	}*/

	public static long createBackup(){
		Date date = new Date();
		long preTime = System.currentTimeMillis();
		File backupFile = new File("backups/"+date.toString().replaceAll(" ","--").replaceAll(":","-")+".zip");
		try{
			ZipOutputStream os = new ZipOutputStream(new FileOutputStream(backupFile));
			putPathInZip(worldPath,os);
			os.closeEntry();
			os.flush();
			os.close();
			return  (System.currentTimeMillis() - preTime);
		}catch (IOException e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将路径放入Zip
	 * @param path 指定路径
	 * @param os zip输出流
	 */
	private static void putPathInZip(File path,ZipOutputStream os){
		if (path.exists()){
			if (path.isDirectory()) {
				for (File file : path.listFiles()) {
					if (file.isDirectory()) {
						putPathInZip(file, os);
					} else {
						writeData(file,os);
					}
				}
			}else {
				writeData(path,os);
			}
		}
	}
	private static void writeData(File file,ZipOutputStream os){
		try {
			FileInputStream fis = new FileInputStream(file);
			os.putNextEntry(new ZipEntry(file.getPath()));
			os.write(fis.readAllBytes());
			os.closeEntry();
		} catch (IOException ignored) {
		}
	}

	/*public static void newAuto(SetAutoCallback processor){
		processor.server_mod$updateTimer();
	}

	public interface SetAutoCallback{
		void server_mod$updateTimer();
	}*/
}