package mys.ticklib.mixins;

import mys.ticklib.freeze.FreezeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedStoneWireBlock.class)
public abstract class RedStoneWireBlockMixin {
    @Inject(method = "updatePowerStrength", at = @At("HEAD"), cancellable = true)
    private void freeze$cancelPowerUpdate(Level level, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (FreezeManager.isFrozen() && level instanceof ServerLevel serverLevel) {
            FreezeManager.queueNeighborUpdate(serverLevel, pos);
            ci.cancel();
        }
    }

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void freeze$cancelSurvive(BlockState pState, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (FreezeManager.isFrozen() && level instanceof ServerLevel serverLevel) {
            FreezeManager.queueNeighborUpdate(serverLevel, pos);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canSurviveOn", at = @At("HEAD"), cancellable = true)
    private void freeze$cancelSurviveOn(BlockGetter level, BlockPos pos, BlockState pState, CallbackInfoReturnable<Boolean> cir) {
        if (FreezeManager.isFrozen() && level instanceof ServerLevel serverLevel) {
            FreezeManager.queueNeighborUpdate(serverLevel, pos);
            cir.setReturnValue(true);
        }
    }
}

