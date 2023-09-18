package com.xiang.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.xiang.ServerUtility;
import com.xiang.navigate.Navigator;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ServerUtil {
    public interface ExecuteCommandCallback {
        void back(String back);
    }

    public static void executeCommand(String command) {
        executeCommand(command, null);
    }

    public static void executeCommand(String command, ExecuteCommandCallback callback) {

        CommandManager commandManager = Navigator.playerManager.getServer().getCommandManager();
        CommandDispatcher<ServerCommandSource> commandDispatcher = commandManager.getDispatcher();
        StringBuilder msg = new StringBuilder();
        Object wait = new Object();
        class ServerCommandOutput implements CommandOutput {

            @Override
            public void sendMessage(Text message) {
                synchronized (wait) {
                    wait.notify();
                }
                msg.append(message.getString()).append('\n');
            }

            @Override
            public boolean shouldReceiveFeedback() {
                return true;
            }

            @Override
            public boolean shouldTrackOutput() {
                return false;
            }

            @Override
            public boolean shouldBroadcastConsoleToOps() {
                return false;
            }
        }
        try {

            ServerCommandSource source = Navigator.playerManager.getServer().getCommandSource();

            ServerCommandSource serverCommandSource = new ServerCommandSource(
                    new ServerCommandOutput(),
                    source.getPosition(),
                    source.getRotation(),
                    source.getWorld(),
                    4,
                    "Server",
                    Text.of("LG"),
                    source.getServer(),
                    source.getEntity()
            );
            //tick entities
            ///tick health
            commandDispatcher.execute(command, (callback == null ? source : serverCommandSource));
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        if (callback != null)
            new Thread(() -> {
                try {
                    Thread sleepWatcher = new Thread(() -> {//强制苏醒
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException ignored) {
                        }
                        synchronized (wait) {
                            wait.notify();
                        }
                    });
                    sleepWatcher.start();
                    synchronized (wait) {
                        wait.wait();
                    }
                    sleepWatcher.interrupt();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                callback.back(msg.toString());
            }).start();
    }

    public static void broadcastAll(String s) {
        broadcastAll(s, false);
    }

    public static void broadcastAll(String s, boolean overlay) {
        for (ServerPlayerEntity player :Navigator.playerManager.getPlayerList())
            player.sendMessageToClient(Text.of(s), overlay);
    }

    public static void broadcast(ServerPlayerEntity player, String s, boolean overlay) {
        if(player!=null)
        player.sendMessageToClient(Text.of(s), overlay);
    }

}
