package com.xiang.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
    private void playerTick(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool, CallbackInfo ci) {
        player.sendMessage(
                Text.of(player.getEntityName() + "破坏了->"+state.getBlock().getName())
        );
        //Scoreboard scoreboard = getScoreboard();
    }
}
