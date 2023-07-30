package com.xiang.mixin;

import com.mojang.datafixers.util.Either;
import com.xiang.ServerUtility;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.util.Colors;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author xiang2333
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin extends PlayerMixin{
    @Shadow @Final public MinecraftServer server;

    @Shadow public abstract void sendMessageToClient(Text message, boolean overlay);

    @Shadow public abstract Either<PlayerEntity.SleepFailureReason, Unit> trySleep(BlockPos pos);

    @Inject(at = @At("TAIL"), method = "tick")
    private void playerTick(CallbackInfo info) {
        /*scoreboard.addObjective(

        );*/
    }
    @Inject(at = @At("TAIL"), method = "onDeath")
    private void playerDead(DamageSource damageSource, CallbackInfo callbackInfo) {
        //ServerUtility.LOGGER.info(getName() + "$4趋势了");
        //玩家死亡消息

        server.getPlayerManager().broadcast(MutableText.of(TextContent.EMPTY).append(getName()).append("§4趋势了"),false);
        //sendMessageToClient(Text.of(getName() + "$4趋势了"),true);
    }

}
