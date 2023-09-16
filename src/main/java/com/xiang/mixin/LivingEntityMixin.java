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
                if (damageStatisticMap.containsKey(playerName)) {
                    damageStatisticMap.put(playerName, damageStatisticMap.get(playerName) + amount);
                } else {
                    damageStatisticMap.put(playerName, amount);
                }
                //更新计分板
                ScoreboardObjective objective = player.getScoreboard().getObjective("damage");
                if (objective != null) {
                    ScoreboardPlayerScore playerScore = player.getScoreboard().getPlayerScore(playerName, objective);
                    // 设置玩家的积分
                    playerScore.setScore(damageStatisticMap.get(playerName).intValue());
                }
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
                //更新计分板
                ScoreboardObjective objective = player.getScoreboard().getObjective("killCount");
                if (objective != null) {
                    ScoreboardPlayerScore playerScore = player.getScoreboard().getPlayerScore(player.getEntityName(), objective);
                    // 增加玩家的积分
                    playerScore.incrementScore(1);
                }
            }
        }
    }

}
