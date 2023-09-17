package com.xiang.mixin;

import com.mojang.authlib.GameProfile;
import com.xiang.ServerUtility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
public abstract class PlayerMixin extends Entity {
    @Unique
    double previousX;
    @Unique
    double previousY;
    @Unique
    double previousZ;
    @Unique
    PlayerEntity self;

    public PlayerMixin(EntityType<?> type, World world) {
        super(type, world);
    }


    @Shadow
    public abstract PlayerInventory getInventory();

    @Shadow
    public abstract Scoreboard getScoreboard();

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
        String playerName = getEntityName();
        //增加缓存中玩家移动距离
        moveStatisticMap.put(getUuid(), moveStatisticMap.get(getUuid()) + moveDistance);
    }

    @Inject(at = @At("TAIL"), method = "addExperience")
    private void onPlayerGetExp(int experience, CallbackInfo ci) {

        //更新计分板
        //增加缓存中玩家经验获取
        expGetCountStatisticMap.put(getUuid(), expGetCountStatisticMap.get(getUuid()) + experience);

    }

    @Inject(at = @At("TAIL"), method = "damage")
    private void onPlayerTakeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

        //玩家受攻击
        //增加缓存中玩家受到的伤害
        takeDamageStatisticMap.put(getUuid(), takeDamageStatisticMap.get(getUuid()) + amount);
    }

    /**
     * 更新上次玩家位置
     */
    @Unique
    private void updatePrePos() {
        previousX = self.getX();
        previousY = self.getY();
        previousZ = self.getZ();
    }
}
