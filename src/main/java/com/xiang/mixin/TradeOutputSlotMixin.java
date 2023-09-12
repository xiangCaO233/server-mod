package com.xiang.mixin;

import com.xiang.ServerUtility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.screen.slot.TradeOutputSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * @author xiang2333
 */
@Mixin(TradeOutputSlot.class)
public class TradeOutputSlotMixin {
    @Inject(at=@At("TAIL"),method = "onTakeItem")
    private void onPlayerTrade(PlayerEntity player, ItemStack stack, CallbackInfo ci){
        //玩家交易后
        ScoreboardObjective objective = player.getScoreboard().getObjective("tradeCount");
        if (objective != null) {
            ScoreboardPlayerScore playerScore = player.getScoreboard().getPlayerScore(player.getEntityName(), objective);
            // 增加玩家的积分
            playerScore.incrementScore(1);
        }
    }

}
