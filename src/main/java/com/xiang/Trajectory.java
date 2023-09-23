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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import static com.xiang.navigate.Navigator.playerManager;

public class Trajectory {
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
        ByteBuffer byteBuffer = ByteBuffer.wrap(inputStream.readAllBytes());
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

    /**
     * 保存现有的数据 重新记录
     */
    public void save() throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(20 * posXList.size() + 8 + 4);
        byteBuffer.putInt(posXList.size());
        byteBuffer.putLong(recordTime);
        for (int i = 0; i < posXList.size(); i++) {
            byteBuffer.putFloat(posXList.get(i));
            byteBuffer.putFloat(posYList.get(i));
            byteBuffer.putFloat(posZList.get(i));
            byteBuffer.putFloat(pitchList.get(i));
            byteBuffer.putFloat(yawList.get(i));
        }
        System.out.println(posXList.size());
        String recordTimeStr = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date(recordTime));
        FileOutputStream fileOutputStream = new FileOutputStream("./trajectory/" + owner.getEntityName() + "_" + recordTimeStr + "_" + getTimeLength() + "s.dat");
        byteBuffer.flip();
        fileOutputStream.getChannel().write(byteBuffer);
        fileOutputStream.close();

        posXList.clear();
        posYList.clear();
        posZList.clear();
        pitchList.clear();
        yawList.clear();
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
            /*player.setPos(posXList.get(index), posYList.get(index), posZList.get(index));
            player.setPitch(pitchList.get(index));
            player.setYaw(yawList.get(index));*/

            /*ServerPlayNetworking.send(player, new PlayerMoveC2SPacket.Full(
                    posXList.get(index)
                    , posYList.get(index)
                    , posZList.get(index)
                    , yawList.get(index)
                    , pitchList.get(index)
                    , player.isOnGround()
            ));
        }*/
            /*player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.LookAndOnGround(posXList.get(index), posYList.get(index), false)
            );

            player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                    posXList.get(index)
                    , posYList.get(index)
                    , posZList.get(index)
                    , false
            ));*/
            //player.p

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

        } else {
            //记录轨迹
            Vec3d pos = owner.getPos();
            posXList.add((float) pos.getX());
            posYList.add((float) pos.getY());
            posZList.add((float) pos.getZ());
            pitchList.add(owner.getPitch());
            yawList.add(owner.getYaw());
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
