package com.xiang.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static com.xiang.ServerUtility.*;

/**
 * 方块类注入
 *
 * @author xiang2333
 */
@Mixin(Block.class)
public class BlockMixin {

    @Inject(at = @At("TAIL"), method = "afterBreak")
    private void playerBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool, CallbackInfo ci) {
        //更新玩家挖掘计分板
        //增加缓存中玩家挖掘次数
        minedCountStatisticMap.put(player.getUuid(), minedCountStatisticMap.get(player.getUuid()) + 1);

    }


    @Inject(at = @At("TAIL"), method = "onPlaced")
    private void playerPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack, CallbackInfo ci) {
        //更新玩家放置计分板
        if (placer instanceof PlayerEntity player) {
            //更新玩家放置计分板
            //增加缓存中玩家放置次数
            placedCountStatisticMap.put(player.getUuid(), placedCountStatisticMap.get(player.getUuid()) + 1);

            /*ScoreboardObjective objective = player.getScoreboard().getObjective("placedCount");
            if (objective != null) {
                ScoreboardPlayerScore playerScore = player.getScoreboard().getPlayerScore(player.getEntityName(), objective);
                // 增加玩家的积分
                playerScore.incrementScore(1); // 1表示每次放置增加的分数
            }*/
        }

    }
}
