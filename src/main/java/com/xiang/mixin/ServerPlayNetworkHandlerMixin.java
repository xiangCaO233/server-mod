package com.xiang.mixin;

import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements EntityTrackingListener, ServerPlayPacketListener {
    //拦截退出消息
    @Redirect(method = "onDisconnected",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"))
    public void replaceBroadcast(PlayerManager instance, Text message, boolean overlay) {

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
