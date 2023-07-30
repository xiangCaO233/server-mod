package com.xiang.mixin;

import com.xiang.ServerUtility;
import net.minecraft.network.ClientConnection;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author xiang2333
 */
@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(at = @At("TAIL"), method = "onPlayerConnect")
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        player.getScoreboard().setObjectiveSlot(
                3,ServerUtility.deathCountObj
        );

    }
}
