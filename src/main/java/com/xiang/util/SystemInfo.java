package com.xiang.util;

import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.util.Util;

import static com.xiang.ServerUtility.willRestart;
import static com.xiang.navigate.Navigator.playerManager;

public class SystemInfo {
    static {
        oshi.SystemInfo systemInfo = new oshi.SystemInfo();
        HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();
        GlobalMemory memory = hardwareAbstractionLayer.getMemory();
        CentralProcessor processor = hardwareAbstractionLayer.getProcessor();
        new Thread(() -> {
            while (true) {
                try {
                    cpuUsedPercentage = getCpuUsedPercentage(processor);
                    ramUsedPercentage = getRamUsedPercentage(memory);
                    if(getRamUsedPercentage(memory) >= 85){
                        willRestart = true;
                    }
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public static double cpuUsedPercentage;
    public static double ramUsedPercentage;


    public static double getCpuUsedPercentage(CentralProcessor processor) {
        // CPU信息
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        // 这里必须要设置延迟
        Util.sleep(1000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softIrq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long ioWait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = Math.max(user + nice + cSys + idle + ioWait + irq + softIrq + steal, 0);


        return 1D - (idle <= 0 ? 0 : (1D * idle / totalCpu));
    }

    public static double getRamUsedPercentage(GlobalMemory memory) {
        return 1. - (memory.getAvailable() * 1.) / (memory.getTotal() * 1.);
    }
}
