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
import net.minecraft.world.dimension.DimensionType;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.xiang.ServerUtility.*;
import static com.xiang.navigate.Navigator.playerManager;

/**
 * @author xiang2333
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerMixin extends Entity {
    @Shadow public int experienceLevel;
    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract String getEntityName();

    @Shadow public abstract Scoreboard getScoreboard();

    @Shadow public abstract void sendMessage(Text message, boolean overlay);

    @Unique
    double previousX;
    @Unique
    double previousY;
    @Unique
    double previousZ;
    @Unique
    DimensionType lastDimension;

    public PlayerMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At("TAIL"), method = "tickMovement")
    private void afterPlayerMove(CallbackInfo ci) {

        if (previousX == 0 && previousY == 0 && previousZ == 0) {
            //初次加载
            updatePrePos();
            lastDimension = getWorld().getDimension();
        }
        //System.out.println(getWorld().getDimension());
        //计算移动距离
        double moveDistance = 0;

        if(lastDimension == getWorld().getDimension()){
            moveDistance = Math.sqrt(
                    Math.pow(getX() - previousX, 2) +
                            Math.pow(getY() - previousY, 2) +
                            Math.pow(getZ() - previousZ, 2)
            );
        }
        updatePrePos();

        //增加缓存中玩家移动距离
        Double  moveStatistic = moveStatisticMap.get(getUuid());
        if (moveStatistic != null) {
            moveStatisticMap.put(getUuid(), moveStatistic + moveDistance);
        }

/*        double moveSpeed = moveDistance * 20;
        if(moveSpeed >= 30){
            LOGGER.info(getEntityName() + "移动速度异常");
        }
        sendMessage(
                Text.of("速度:" + new BigDecimal(moveSpeed).setScale(2, RoundingMode.HALF_UP)+"m/s")
        );*/

    }

    @Inject(at = @At("TAIL"), method = "addExperience")
    private void onPlayerGetExp(int experience, CallbackInfo ci) {
        //更新计分板
        //增加缓存中玩家经验获取
        Integer expGetCountStatistic = expGetCountStatisticMap.get(getUuid());
        if (expGetCountStatistic != null) {
            expGetCountStatisticMap.put(getUuid(), expGetCountStatistic + 1);
        }
        levelMap.put(getUuid(), experienceLevel);

    }

    @Inject(at = @At("TAIL"), method = "damage")
    private void onPlayerTakeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

        //玩家受攻击
        //增加缓存中玩家受到的伤害
        Float takeDamageStatistic = takeDamageStatisticMap.get(getUuid());
        if (takeDamageStatistic != null) {
            takeDamageStatisticMap.put(getUuid(), takeDamageStatistic + amount);
        }
    }


    /**
     * 更新上次玩家位置
     */
    @Unique
    private void updatePrePos() {
        previousX = getX();
        previousY = getY();
        previousZ = getZ();
    }
}
