package com.xiang.mixin;

import com.xiang.alona.AlonaThread;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements EntityTrackingListener, ServerPlayPacketListener {
    @Shadow public abstract ServerPlayerEntity getPlayer();

    //拦截退出消息
    @Redirect(method = "onDisconnected",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"))
    public void replaceBroadcast(PlayerManager instance, Text message, boolean overlay) {

    }

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        //System.out.println("聊天消息: " + packet.chatMessage());
        AlonaThread.sendGroupMessage("[IH] [" + getPlayer().getEntityName() + "]: " + packet.chatMessage());

    }

/*


    @Redirect(method = "onPlayerMove",
            at = @At(value = "INVOKE",
                    target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;[Ljava/lang/Object;)V"))
    public void onPlayerMove(Logger instance, String s, Object[] objects) {
        LOGGER.warn("{} moved too quickly! {},{},{}", objects);

        ServerUtil.broadcastAll("§c[警告] §e玩家 §b" + objects[0] + " §e移动过快！速度: §b" + ((int) ((Math.abs((double) objects[1]) + Math.abs((double) objects[2]) + Math.abs((double) objects[3])) * 100) / 100f) + "m/s");
    }
    @Redirect(method = "onPlayerMove",
            at = @At(value = "INVOKE",
                    target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"))
    public void onPlayerMove(Logger instance, String s, Object o) {
        LOGGER.warn("{} moved wrongly!", o);
        ServerUtil.broadcastAll("§c[警告] §e玩家 §b" + o + " §e移动异常！");
    }
*/

}
