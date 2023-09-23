package com.xiang.alona;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

import static com.xiang.navigate.Navigator.playerManager;

public class AlonaThread extends Thread {


    public static boolean shutdown = false;
    public static boolean isConnect = false;

    public AlonaThread() {
        setName("AlonaThread");
    }

    URI uri;

    public enum MessageType {
        SEND_MESSAGE,//发送消息
        SEND_IMG,//发送图片
    }

    public static WebSocketClient ws;

    //给群里发送消息
    public static void sendGroupMessage(String msg) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", MessageType.SEND_MESSAGE.name());
        jsonObject.addProperty("data", msg);
        //System.out.println("发送群消息: " + msg);
        wsSendMessage(jsonObject);
    }

    //获得数据
    public static void wsGetMessage(JsonObject msg) {

        switch (MessageType.valueOf(msg.get("type").getAsString())) {
            case SEND_MESSAGE -> {

                //System.out.println("收到群里消息:" + msg.getString("data"));

                playerManager.broadcast(MutableText.of(new LiteralTextContent(msg.get("data").getAsString())), false);
                //Garden.server.spigot().broadcast(new TextComponent(msg.getString("data")));
                break;
            }
            case SEND_IMG -> {
                //LifeGardenMod.server.getPlayerManager().broadcast(MutableText.of(new LiteralTextContent(msg.getString("data"))), false);
                //Garden.server.spigot().broadcast(new TextComponent(msg.getString("data")));
                //playerManager.getServer().getCommandManager().execute(null, "/chatimage send " + msg.get("title").getAsString() + " " + msg.get("url").getAsString());
                ///chatimage send nmsl https://blog.kituin.fun/img/bg.png

                break;
            }
        }
    }

    //发送数据
    public static void wsSendMessage(JsonObject msg) {
        if (ws.getReadyState().equals(ReadyState.OPEN))
            ws.send(new Gson().toJson(msg));
    }

    static class WebSocket extends WebSocketClient {

        public WebSocket(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {

        }

        @Override
        public void onMessage(String message) {
            wsGetMessage((JsonObject) new JsonParser().parse(message));
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {

        }

        @Override
        public void onError(Exception ex) {

        }
    }

    @Override
    public void run() {

        try {
            uri = new URI("ws://127.0.0.1:3827");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }


        ws = new WebSocket(uri);
        ws.connect();


        while (!shutdown) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                //throw new RuntimeException(e);
            }
            isConnect = ws.isOpen();
            try {
                if (!isConnect) {
                    System.out.println("连接寄了，自动重连");
                    try {
                        ws.reconnect();
                    } catch (Exception e) {
                        System.out.println("重连失败");
                    }
                }
            } catch (Exception ignored) {

            }
        }
        ws.close();


    }
}
