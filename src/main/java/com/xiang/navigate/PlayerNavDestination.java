package com.xiang.navigate;

/**
 * @author xiang2333
 */
public class PlayerNavDestination {
    String playerName;
    String desPlayerName;
    double x;
    double y;
    double z;

    public PlayerNavDestination(String playerName, String desPlayerName) {
        System.out.println("新建导航点");
        this.playerName = playerName;
        this.desPlayerName = desPlayerName;
    }

    public PlayerNavDestination(String playerName, double x, double y, double z) {
        this.playerName = playerName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean isPlayerDes(){
        return desPlayerName != null;
    }
}
