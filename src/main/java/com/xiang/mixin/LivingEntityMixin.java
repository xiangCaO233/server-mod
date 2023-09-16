package com.xiang.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.xiang.ServerUtility.damageStatisticMap;
import static com.xiang.ServerUtility.killCountStatisticMap;

/**
 * @author xiang2333
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin{


    @Inject(at=@At("TAIL"),method = "damage")
    private void entityOnDamaged(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
        //实体受伤
        Entity sourceAttacker = source.getAttacker();
        if (sourceAttacker != null) {
            if (sourceAttacker instanceof PlayerEntity player){
                //玩家攻击
                //增加缓存中玩家造成的伤害
                String playerName = player.getEntityName();
                damageStatisticMap.put(playerName, damageStatisticMap.get(playerName) + amount);
            }
        }
    }
    @Inject(at=@At("TAIL"),method = "onDeath")
    private void entityOnDamaged(DamageSource damageSource, CallbackInfo ci){
        //实体死亡
        Entity sourceAttacker = damageSource.getAttacker();
        if (sourceAttacker != null) {
            if (sourceAttacker instanceof PlayerEntity player){
                //玩家攻击
                //更新击杀计分板
                String playerName = player.getEntityName();
                killCountStatisticMap.put(playerName, killCountStatisticMap.get(playerName) + 1);

            }
        }
    }

}
