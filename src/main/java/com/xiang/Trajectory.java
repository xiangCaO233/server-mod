package com.xiang;

import com.xiang.util.Info;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.io.*;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.xiang.navigate.Navigator.playerManager;

public class Trajectory {
    public static void main(String[] args) {
        new Thread(()->{
            for (int i = 0; i < 100000; i++) {
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(114514);
            }
            System.out.println("over");
            System.gc();
            new Scanner(System.in).nextLine();
        }).start();
    }
    /**
     * 记录轨迹的开始时间戳
     */
    private long recordTime;
    /**
     * 播放的状态
     */
    private boolean isPlay;
    /**
     * 播放的索引
     */
    private int index = 0;
    private final ArrayList<Float> posXList = new ArrayList<>();
    private final ArrayList<Float> posYList = new ArrayList<>();
    private final ArrayList<Float> posZList = new ArrayList<>();
    private final ArrayList<Float> pitchList = new ArrayList<>();
    private final ArrayList<Float> yawList = new ArrayList<>();

    /**
     * 所有者
     */
    private final ServerPlayerEntity owner;

    /**
     * 播放者
     */
    private final ServerPlayerEntity player;

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    /**
     * 开始录制时
     */
    public Trajectory(ServerPlayerEntity owner) {
        this.owner = owner;
        player = null;
        recordTime = System.currentTimeMillis();
    }


    /**
     * 播放轨迹时
     */
    public Trajectory(ServerPlayerEntity player, FileInputStream inputStream) throws IOException {
        this.owner = null;
        this.player = player;
        recordTime = System.currentTimeMillis();
        ByteBuffer byteBuffer;
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream)) {
            byteBuffer = ByteBuffer.wrap(gzipInputStream.readAllBytes());
        }
        synchronized (this) {
            int tickLength = byteBuffer.getInt();
            recordTime = byteBuffer.getLong();
            for (int i = 0; i < tickLength; i++) {
                posXList.add(byteBuffer.getFloat());
                posYList.add(byteBuffer.getFloat());
                posZList.add(byteBuffer.getFloat());
                pitchList.add(byteBuffer.getFloat());
                yawList.add(byteBuffer.getFloat());
            }
        }
    }

    /**
     * 保存现有的数据 重新记录
     */
    public void save() throws IOException {
        List<Float> tempPosXList;
        List<Float> tempPosYList;
        List<Float> tempPosZList;
        List<Float> tempPitchList;
        List<Float> tempYawList;
        long time = getTimeLength();
        recordTime = System.currentTimeMillis();
        synchronized (this) {
            tempPosXList = (List<Float>) posXList.clone();
            tempPosYList = (List<Float>) posYList.clone();
            tempPosZList = (List<Float>) posZList.clone();
            tempPitchList = (List<Float>) pitchList.clone();
            tempYawList = (List<Float>) yawList.clone();

            posXList.clear();
            posYList.clear();
            posZList.clear();
            pitchList.clear();
            yawList.clear();
        }


        int listSize = tempPosXList.size();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(20 * listSize + 8 + 4);
        byteBuffer.putInt(listSize);
        byteBuffer.putLong(recordTime);
        for (int i = 0; i < listSize; i++) {
            byteBuffer.putFloat(tempPosXList.get(i));
            byteBuffer.putFloat(tempPosYList.get(i));
            byteBuffer.putFloat(tempPosZList.get(i));
            byteBuffer.putFloat(tempPitchList.get(i));
            byteBuffer.putFloat(tempYawList.get(i));
        }

        byteBuffer.flip();

        // 创建一个ByteArrayOutputStream来存储压缩后的数据
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        // 将ByteBuffer的内容写入到GZIPOutputStream中
        while (byteBuffer.hasRemaining()) {
            gzipOutputStream.write(byteBuffer.get());
        }
        gzipOutputStream.close(); // 关闭压缩流

        // 将压缩后的数据写入文件
        String recordTimeStr = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date(recordTime));
        String filePath = "./trajectory/" + owner.getEntityName() + "_" + recordTimeStr + "_" + time + "s.dat";
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        fileOutputStream.write(byteArrayOutputStream.toByteArray());
        fileOutputStream.close();

    }

    /**
     * 设置播放位置
     *
     * @param tick 相对时间tick
     */
    public void setPlayPos(int tick) {
        index = tick;
        if (index >= posXList.size()) {
            index = posXList.size() - 1;
            isPlay = false;
        }
    }

    public void setPlayStat(boolean isPlay) {
        this.isPlay = isPlay;
    }

    public void serverTickHandler() {
        if (player != null) {
            synchronized (this) {
                playerManager.broadcast(Text.of(Formatting.GOLD + "播放时间：" + (index / 20) + "s - " + (posXList.size() / 20) + "s " + (isPlay ? "播放中" : "暂停")), true);

                //播放轨迹
                if (isPlay) {


                    player.networkHandler.requestTeleport(posXList.get(index)
                            , posYList.get(index)
                            , posZList.get(index)
                            , yawList.get(index)
                            , pitchList.get(index));


                    setPlayPos(++index);
                }
            }

        } else {
            //记录轨迹
            Vec3d pos = owner.getPos();
            synchronized (this) {
                posXList.add((float) pos.getX());
                posYList.add((float) pos.getY());
                posZList.add((float) pos.getZ());
                pitchList.add(owner.getPitch());
                yawList.add(owner.getYaw());
            }
        }

    }

    // 在适当的地方调用此方法来发送位置和视角信息给客户端
    private void sendPlayerPositionAndLook(ServerPlayerEntity player, double x, double y, double z, float yaw, float pitch) {
        PlayerPositionLookS2CPacket packet = new PlayerPositionLookS2CPacket(x, y, z, yaw, pitch, Collections.emptySet(), 0);
        player.networkHandler.sendPacket(packet);
    }

    /**
     * 获得总时长
     *
     * @return 秒
     */
    public long getTimeLength() {
        return posXList.size() / 20;
    }

}
