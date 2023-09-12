package com.xiang.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.xiang.ServerUtility.*;

/**
 * @author xiang2333
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerMixin {
    @Unique
    double previousX;
    @Unique
    double previousY;
    @Unique
    double previousZ;
    @Unique
    PlayerEntity self;

    @Shadow
    public abstract Text getName();

    @Shadow
    public abstract PlayerInventory getInventory();

    @Shadow public abstract Scoreboard getScoreboard();

    @Shadow public abstract String getEntityName();

    @Inject(at = @At("TAIL"), method = "tickMovement")
    private void afterPlayerMove(CallbackInfo ci) {
        if (previousX == 0 && previousY == 0 && previousZ == 0) {
            //初次加载
            updatePrePos();
        }
        //计算移动距离
        double moveDistance = Math.sqrt(
                Math.pow(self.getX() - previousX, 2) +
                        Math.pow(self.getY() - previousY, 2) +
                        Math.pow(self.getZ() - previousZ, 2)
        );
        updatePrePos();
        String playerName = self.getEntityName();
        //增加缓存中玩家移动距离
        if (moveStatisticMap.containsKey(playerName)) {
            moveStatisticMap.put(playerName, moveStatisticMap.get(playerName) + moveDistance);
        } else {
            moveStatisticMap.put(playerName, moveDistance);
        }
        //更新计分板
        ScoreboardObjective objective = getScoreboard().getObjective("moveDistance");
        if (objective != null) {
            ScoreboardPlayerScore playerScore = getScoreboard().getPlayerScore(getEntityName(), objective);
            // 设置玩家的积分
            playerScore.setScore(moveStatisticMap.get(playerName).intValue());
        }
    }

    @Inject(at = @At("TAIL"), method = "addExperience")
    private void onPlayerGetExp(int experience, CallbackInfo ci) {
        //更新计分板
        ScoreboardObjective objective = getScoreboard().getObjective("expGetCount");
        if (objective != null) {
            ScoreboardPlayerScore playerScore = getScoreboard().getPlayerScore(getEntityName(), objective);
            // 增加玩家的积分
            playerScore.incrementScore(experience);
        }
    }

    @Inject(at = @At("TAIL"), method = "damage")
    private void onPlayerTakeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        //玩家受攻击
        //增加缓存中玩家造成的伤害
        String playerName = getEntityName();
        if (takeDamageStatisticMap.containsKey(playerName)) {
            takeDamageStatisticMap.put(playerName, takeDamageStatisticMap.get(playerName) + amount);
        } else {
            takeDamageStatisticMap.put(playerName, amount);
        }
        //更新计分板
        ScoreboardObjective objective = getScoreboard().getObjective("takeDamage");
        if (objective != null) {
            ScoreboardPlayerScore playerScore = getScoreboard().getPlayerScore(playerName, objective);
            // 设置玩家的积分
            playerScore.setScore(takeDamageStatisticMap.get(playerName).intValue());
        }
    }

    /**
     * 更新上次玩家位置
     */
    @Unique
    private void updatePrePos() {
        self = getInventory().player;
        previousX = self.getX();
        previousY = self.getY();
        previousZ = self.getZ();
    }
}
