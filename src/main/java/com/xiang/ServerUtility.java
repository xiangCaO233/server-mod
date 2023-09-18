package com.xiang;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xiang.alona.AlonaThread;
import com.xiang.scoreborad.BetterObjective;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
    public static File configfile;
    public static File backupsPath;
    public static File worldPath;
    public static JsonObject configJson;
    //记录来过的玩家
    public static ArrayList<PlayerEntity> usedPlayers;
    public static ArrayList<PlayerEntity> onlinePlayers;
    //备份线程
    static Thread backupTimer;
    //备份停止标记
    public static boolean stopBackupT;
    //生命值积分项
    public static ScoreboardObjective healthObj;

    //玩家名uuid映射表
    public static HashMap<UUID, String> playerNameMapping;
    //死亡数缓存
    public static HashMap<UUID, Integer> deathsStatisticMap;
    //挖掘数缓存
    public static HashMap<UUID, Integer> minedCountStatisticMap;
    //放置数缓存
    public static HashMap<UUID, Integer> placedCountStatisticMap;
    //交易数缓存
    public static HashMap<UUID, Integer> tradeCountStatisticMap;
    /**
     * 玩家移动统计缓存
     */
    public static HashMap<UUID, Double> moveStatisticMap;
    //经验获取缓存
    public static HashMap<UUID, Integer> expGetCountStatisticMap;
    public static HashMap<UUID, Integer> levelMap;
    public static HashMap<UUID, Integer> killCountStatisticMap;
    /**
     * 玩家输出伤害缓存
     */
    public static HashMap<UUID, Float> damageStatisticMap;
    /**
     * 玩家承受伤害缓存
     */
    public static HashMap<UUID, Float> takeDamageStatisticMap;
    public static HashMap<UUID, Integer> onlineStatisticMap;
    /**
     * 公开的计分项 (测试)
     */
    public static BetterObjective betterObjective = new BetterObjective("scoreboard", "尼玛", 10);

    @Override
    public void onInitialize() {
        LOGGER.info("server utility initializing");
        usedPlayers = new ArrayList<>();
        onlinePlayers = new ArrayList<>();

        playerNameMapping = new HashMap<>();
        deathsStatisticMap = new HashMap<>();
        minedCountStatisticMap = new HashMap<>();
        placedCountStatisticMap = new HashMap<>();
        tradeCountStatisticMap = new HashMap<>();
        expGetCountStatisticMap = new HashMap<>();
        levelMap = new HashMap<>();
        killCountStatisticMap = new HashMap<>();
        moveStatisticMap = new HashMap<>();
        damageStatisticMap = new HashMap<>();
        takeDamageStatisticMap = new HashMap<>();
        onlineStatisticMap = new HashMap<>();

        configfile = new File("config/sudata.cfg");
        File configPath = configfile.getParentFile();
        backupsPath = new File("backups");
        worldPath = new File("world");

        //检查配置文件路径
        if (!configPath.exists()) {
            configPath.mkdir();
        }
        //检查配置文件
        if (!configfile.exists()) {
            try {
                configfile.createNewFile();
                BufferedWriter bw = new BufferedWriter(new FileWriter(configfile));
                bw.write("{}");
                bw.flush();
                bw.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        //加载配置文件
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(configfile));
            StringBuilder json = new StringBuilder();
            String str;
            while ((str = br.readLine()) != null) {
                json.append(str);
            }
            configJson = new JsonParser().parse(json.toString()).getAsJsonObject();


            JsonObject deathJson = configJson.getAsJsonObject("deaths");
            JsonObject minedCountJson = configJson.getAsJsonObject("minedCount");
            JsonObject placedCountJson = configJson.getAsJsonObject("placedCount");
            JsonObject tradeCountJson = configJson.getAsJsonObject("tradeCount");
            JsonObject moveJson = configJson.getAsJsonObject("move");
            JsonObject expGetCountJson = configJson.getAsJsonObject("expGetCount");
            JsonObject levelJson = configJson.getAsJsonObject("level");
            JsonObject killCountJson = configJson.getAsJsonObject("killCount");
            JsonObject damageJson = configJson.getAsJsonObject("damage");
            JsonObject takeDamageJson = configJson.getAsJsonObject("takeDamage");
            JsonObject onlineJson = configJson.getAsJsonObject("online");
            JsonObject playerNameJson = configJson.getAsJsonObject("players");
            if (deathJson != null) {
                for (String uuid : deathJson.keySet()) {
                    deathsStatisticMap.put(UUID.fromString(uuid), deathJson.get(uuid).getAsInt());
                }
            }
            if (minedCountJson != null) {
                for (String uuid : minedCountJson.keySet()) {
                    minedCountStatisticMap.put(UUID.fromString(uuid), minedCountJson.get(uuid).getAsInt());
                }
            }
            if (placedCountJson != null) {
                for (String uuid : placedCountJson.keySet()) {
                    minedCountStatisticMap.put(UUID.fromString(uuid), placedCountJson.get(uuid).getAsInt());
                }
            }
            if (tradeCountJson != null) {
                for (String uuid : tradeCountJson.keySet()) {
                    tradeCountStatisticMap.put(UUID.fromString(uuid), tradeCountJson.get(uuid).getAsInt());
                }
            }
            if (moveJson != null) {
                for (String uuid : moveJson.keySet()) {
                    moveStatisticMap.put(UUID.fromString(uuid), moveJson.get(uuid).getAsDouble());
                }
            }
            if (expGetCountJson != null) {
                for (String uuid : expGetCountJson.keySet()) {
                    expGetCountStatisticMap.put(UUID.fromString(uuid), expGetCountJson.get(uuid).getAsInt());
                }
            }
            if (levelJson != null) {
                for (String uuid : levelJson.keySet()) {
                    levelMap.put(UUID.fromString(uuid), levelJson.get(uuid).getAsInt());
                }
            }
            if (killCountJson != null) {
                for (String uuid : killCountJson.keySet()) {
                    killCountStatisticMap.put(UUID.fromString(uuid), killCountJson.get(uuid).getAsInt());
                }
            }
            if (damageJson != null) {
                for (String uuid : damageJson.keySet()) {
                    damageStatisticMap.put(UUID.fromString(uuid), damageJson.get(uuid).getAsFloat());
                }
            }
            if (takeDamageJson != null) {
                for (String uuid : takeDamageJson.keySet()) {
                    takeDamageStatisticMap.put(UUID.fromString(uuid), takeDamageJson.get(uuid).getAsFloat());
                }
            }
            if (onlineJson != null) {
                for (String uuid : onlineJson.keySet()) {
                    onlineStatisticMap.put(UUID.fromString(uuid), onlineJson.get(uuid).getAsInt());
                }
            }
            if (playerNameJson != null) {
                for (String uuid : playerNameJson.keySet()) {
                    playerNameMapping.put(UUID.fromString(uuid), onlineJson.get(uuid).getAsString());
                }
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        //检查备份文件夹目录
        if (!(backupsPath.exists() && backupsPath.isDirectory())) {
            backupsPath.mkdir();
        }
        //初始化better计分板
        //betterScoreboard = new BetterScoreboard("LifeGarden","--LifeGarden--");
        new AlonaThread().start();

    }

    /**
     * 检查统计表中是否存在
     *
     * @param player 玩家名
     */
    public static void checkStatistic(PlayerEntity player) {
        UUID uuid = player.getUuid();
        if (!deathsStatisticMap.containsKey(uuid)) {
            deathsStatisticMap.put(uuid, 0);
        }
        if (!minedCountStatisticMap.containsKey(uuid)) {
            minedCountStatisticMap.put(uuid, 0);
        }
        if (!placedCountStatisticMap.containsKey(uuid)) {
            placedCountStatisticMap.put(uuid, 0);
        }
        if (!tradeCountStatisticMap.containsKey(uuid)) {
            tradeCountStatisticMap.put(uuid, 0);
        }
        if (!expGetCountStatisticMap.containsKey(uuid)) {
            expGetCountStatisticMap.put(uuid, 0);
        }
        if (!moveStatisticMap.containsKey(uuid)) {
            moveStatisticMap.put(uuid, 0.0);
        }
        if (!damageStatisticMap.containsKey(uuid)) {
            damageStatisticMap.put(uuid, 0f);
        }
        if (!takeDamageStatisticMap.containsKey(uuid)) {
            takeDamageStatisticMap.put(uuid, 0f);
        }
        if (!killCountStatisticMap.containsKey(uuid)) {
            killCountStatisticMap.put(uuid, 0);
        }
    }

    public static void startBackupTimer() {
        backupTimer = new Thread(() -> {
            while (!stopBackupT) {
                playerManager.broadcast(Text.of(Formatting.GOLD + "开始备份"), false);
                playerManager.broadcast(Text.of((Formatting.GREEN + "备份花费:" + Formatting.RESET + ServerUtility.createBackup() + "ms")), false);
                try {
                    Thread.sleep(60000 * 15);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        //Text.translatable(, )
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

    public static long createBackup() {
        File[] backups = Objects.requireNonNull(backupsPath.listFiles());
        if (backups.length >= 5) {
            //保留五个备份
            ArrayList<Date> dates = new ArrayList<>();
            HashMap<Date, File> fileMap = new HashMap<>();
            for (File file : backups) {
                //LOGGER.info("处理文件:" + file);
                Date date = new Date(file.lastModified());
                dates.add(date);
                fileMap.put(date, file);
            }
            dates.sort(Date::compareTo);
            File earliestFile = fileMap.get(dates.get(0));
            earliestFile.delete();
        }
        Date date = new Date();

        long preTime = System.currentTimeMillis();
        File backupFile = new File("backups/" + date.toString().replaceAll(" ", "--").replaceAll(":", "-") + ".zip");
        try {
            ZipOutputStream os = new ZipOutputStream(new FileOutputStream(backupFile));
            putPathInZip(worldPath, os);
            os.closeEntry();
            os.flush();
            os.close();
            return (System.currentTimeMillis() - preTime);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将路径放入Zip
     *
     * @param path 指定路径
     * @param os   zip输出流
     */
    private static void putPathInZip(File path, ZipOutputStream os) {
        if (path.exists()) {
            if (path.isDirectory()) {
                for (File file : path.listFiles()) {
                    if (file.isDirectory()) {
                        putPathInZip(file, os);
                    } else {
                        writeData(file, os);
                    }
                }
            } else {
                writeData(path, os);
            }
        }
    }

    private static void writeData(File file, ZipOutputStream os) {
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