package com.xiang;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.io.InputStream;
import java.util.ArrayList;

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
     * 开始录制时
     */
    public Trajectory(ServerPlayerEntity owner) {
        this.owner = owner;
        recordTime = System.currentTimeMillis();
    }

    /**
     * 播放轨迹时
     */
    public Trajectory(ServerPlayerEntity player, InputStream inputStream) {
        this.owner = null;
        recordTime = System.currentTimeMillis();
    }

    /**
     * 添加一个位置
     */
    public void addPos(Vec3d v) {
        posXList.add((float) v.getX());
        posYList.add((float) v.getY());
        posZList.add((float) v.getZ());
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

    /**
     * 播放位置
     */
    public static void playPos(Trajectory trajectory, ServerPlayerEntity player) {
        /**
         * 播放一帧
         */


        if (trajectory.isPlay) {
            trajectory.setPlayPos(++trajectory.index);
        }
    }
}
