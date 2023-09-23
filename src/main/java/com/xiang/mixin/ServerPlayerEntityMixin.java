package com.xiang.mixin;

import com.mojang.authlib.GameProfile;
import com.xiang.alona.AlonaThread;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiling.jfr.sample.FileIoSample;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.xiang.ServerUtility.*;
import static com.xiang.ServerUtility.takeDamageStatisticMap;
import static com.xiang.navigate.Navigator.playerManager;

/**
 * @author xiang2333
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Shadow
    public abstract ServerStatHandler getStatHandler();

    @Shadow public abstract void sendMessage(Text message, boolean overlay);

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void onTick(CallbackInfo ci) {
        Integer value = onlineStatisticMap.get(getUuid());
        if (value != null) {
            onlineStatisticMap.put(getUuid(),  getStatHandler().getStat(Stats.CUSTOM, Stats.PLAY_TIME) / 20);
        }
        //getPitch()
    }

    /**
     * 玩家死亡注入
     *
     * @param damageSource 伤害来源
     * @param callbackInfo
     */
    @Inject(at = @At("TAIL"), method = "onDeath")
    private void playerDead(DamageSource damageSource, CallbackInfo callbackInfo) {
        //死亡注入
        //增加缓存中玩家死亡次数
        Integer value = deathsStatisticMap.get(this.getUuid());
        if (value != null) {
            deathsStatisticMap.put(this.getUuid(), value + 1);
        }
        //玩家死亡消息
        sendMessage(Text.of("§c[死亡坐标]"+"§a->"+"§b(x:"+ (int)getX() + ",y:"+(int)getY()+",z:"+(int)getZ()+")"));
        //playerManager.getServer().getPlayerManager().broadcast(MutableText.of(TextContent.EMPTY).append("§b§n").append((value == null ? "[bot]" : "") + getEntityName()).append(" §r§4趋势了"), false);
        AlonaThread.sendGroupMessage("[IH]: " + (value == null ? "[bot]" : "") + getEntityName() + " 趋势了");
    }
}
