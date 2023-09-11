package com.xiang.mixin;

import com.xiang.ServerUtility;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author xiang2333
 */
@Mixin(Block.class)
public class BlockMixin {
    @Inject(at = @At("TAIL"), method = "afterBreak")
    private void playerBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool, CallbackInfo ci) {
        player.sendMessage(
                Text.of(player.getEntityName() + "破坏了->"+state.getBlock().getName())
        );

        ScoreboardObjective objective = player.getScoreboard().getObjective("minedCount");
        if (objective != null) {
            ScoreboardPlayerScore playerScore = player.getScoreboard().getPlayerScore(player.getEntityName(), objective);
            ServerUtility.LOGGER.info("挖掘分数+1");
            // 增加玩家的积分
            playerScore.incrementScore(1); // 1表示每次挖掘增加的分数
        }
        //Scoreboard scoreboard = getScoreboard();
    }
}
