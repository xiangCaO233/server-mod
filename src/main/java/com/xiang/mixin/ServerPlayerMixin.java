package com.xiang.mixin;

import com.mojang.datafixers.util.Either;
import com.xiang.ServerUtility;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.util.Colors;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.xiang.ServerUtility.deathsStatisticMap;
import static com.xiang.ServerUtility.moveStatisticMap;

/**
 * @author xiang2333
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin extends PlayerMixin{
    @Shadow @Final public MinecraftServer server;

    /**
     * tick玩家注入
     * @param info
     */
    @Inject(at = @At("TAIL"), method = "tick")
    private void playerTick(CallbackInfo info) {

    }

    /**
     * 玩家死亡注入
     * @param damageSource 伤害来源
     * @param callbackInfo
     */
    @Inject(at = @At("TAIL"), method = "onDeath")
    private void playerDead(DamageSource damageSource, CallbackInfo callbackInfo) {
        //死亡注入
        String playerName = self.getEntityName();
        //增加缓存中玩家死亡次数
        deathsStatisticMap.put(playerName, deathsStatisticMap.get(playerName) + 1);
        //玩家死亡消息
        server.getPlayerManager().broadcast(MutableText.of(TextContent.EMPTY).append(getName()).append("§4趋势了"),false);
    }

}
