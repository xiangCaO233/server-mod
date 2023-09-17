package com.xiang.mixin;

import com.xiang.ServerUtility;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.xiang.ServerUtility.*;
import static com.xiang.navigate.Navigator.playerManager;

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

    @Shadow
    public abstract Scoreboard getScoreboard();

    @Shadow
    public abstract String getEntityName();

    @Inject(at = @At("TAIL"), method = "tickMovement")
    private void afterPlayerMove(CallbackInfo ci) {
        if (previousX == 0 && previousY == 0 && previousZ == 0) {
            //初次加载
            updatePrePos();
            if (self == null) {
                return;
            }
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
        moveStatisticMap.put(self.getUuid(), moveStatisticMap.get(self.getUuid()) + moveDistance);
    }

    /**
     * 玩家死亡注入
     * @param damageSource 伤害来源
     * @param callbackInfo
     */
    @Inject(at = @At("TAIL"), method = "onDeath")
    private void playerDead(DamageSource damageSource, CallbackInfo callbackInfo) {
        if (self == null) {
            updatePrePos();
        }
        //死亡注入
        //增加缓存中玩家死亡次数
        deathsStatisticMap.put(self.getUuid(), deathsStatisticMap.get(self.getUuid()) + 1);
        //玩家死亡消息
        playerManager.getServer().getPlayerManager().broadcast(MutableText.of(TextContent.EMPTY).append(getName()).append("§4趋势了"),false);
    }

    @Inject(at = @At("TAIL"), method = "addExperience")
    private void onPlayerGetExp(int experience, CallbackInfo ci) {
        if (self == null) {
            updatePrePos();
        }
        //更新计分板
        //增加缓存中玩家经验获取
        expGetCountStatisticMap.put(self.getUuid(), expGetCountStatisticMap.get(self.getUuid()) + experience);

    }

    @Inject(at = @At("TAIL"), method = "damage")
    private void onPlayerTakeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (self == null) {
            updatePrePos();
        }
        //玩家受攻击
        //增加缓存中玩家受到的伤害
        takeDamageStatisticMap.put(self.getUuid(), takeDamageStatisticMap.get(self.getUuid()) + amount);
    }

    /**
     * 更新上次玩家位置
     */
    @Unique
    private void updatePrePos() {
        self = playerManager.getPlayer(getEntityName());
        if (self == null) {
            return;
        }
        previousX = self.getX();
        previousY = self.getY();
        previousZ = self.getZ();
    }
}
