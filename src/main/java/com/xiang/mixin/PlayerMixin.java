package com.xiang.mixin;

import com.xiang.ServerUtility;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author xiang2333
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerMixin {
    @Shadow public abstract Text getName();

    @Shadow public abstract void sendMessage(Text message, boolean overlay);

    /*@Inject(at = @At("TAIL"), method = "onDeath")
    private void playerDead(DamageSource damageSource, CallbackInfo callbackInfo) {
        ServerUtility.LOGGER.info(getName() + "$4趋势了");
        sendMessage(
                Text.of(
                        getName() + "$4趋势了"
                ),true
        );
        get
    }*/
}
