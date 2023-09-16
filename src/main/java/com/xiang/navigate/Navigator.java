package com.xiang.navigate;

import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author xiang2333
 */
public class Navigator {
    public static PlayerManager playerManager;
    public static final ArrayList<PlayerNavDestination> DESTINATIONS = new ArrayList<>();
    public static boolean stopNavThread = true;


    public static void addNavDes(PlayerNavDestination des,NewNavCallback processor){
        System.out.println("添加导航点到列表");
        DESTINATIONS.add(des);
        System.out.println("回调更新Timer");
        processor.updateTimer();
    }

    public static void startTimer(){
        System.out.println("启动导航线程");
        new Thread(()->{
            while (!stopNavThread){
                for (PlayerNavDestination destination:DESTINATIONS){
                    if (destination.isPlayerDes()){
                        /*playerManager.getPlayer(destination.playerName).sendMessage(
                                Text.of("正在为:" + destination.playerName + "导航到->" + destination.desPlayerName)
                        );*/
                    }else {
                        /*playerManager.getPlayer(destination.playerName).sendMessage(
                                Text.of("正在为:" + destination.playerName + "导航到->" +"[" + destination.x + "," + destination.y+ "," + destination.z + "]")
                        );*/
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
    public static void pauseTimer(){
        System.out.println("停止导航线程");
        stopNavThread = true;
    }

    public interface NewNavCallback{
        void updateTimer();
    }

}
