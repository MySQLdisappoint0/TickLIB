package mys.ticklib.mixins;

import mys.ticklib.freeze.FreezeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ConstantValue")
@Mixin(Level.class)
public abstract class LevelNeighborFreezeMixin {

    @Inject(method = "updateNeighborsAt", at = @At("HEAD"), cancellable = true)
    private void timefreeze$updateNeighborsAt(BlockPos pos, Block sourceBlock, CallbackInfo ci) {
        if (FreezeManager.isFrozen() && (Object) this instanceof ServerLevel serverLevel) {
            FreezeManager.queueNeighborUpdate(serverLevel, pos);
            ci.cancel();
        }
    }

    @Inject(method = "updateNeighborsAtExceptFromFacing", at = @At("HEAD"), cancellable = true)
    private void timefreeze$updateNeighborsAtExceptFromFacing(BlockPos pos, Block sourceBlock, Direction skipSide, CallbackInfo ci) {
        if (FreezeManager.isFrozen() && (Object) this instanceof ServerLevel serverLevel) {
            FreezeManager.queueNeighborUpdate(serverLevel, pos);
            ci.cancel();
        }
    }

    @Inject(
            method = "neighborChanged(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void timefreeze$neighborChanged1(BlockPos pos, Block sourceBlock, BlockPos sourcePos, CallbackInfo ci) {
        if (FreezeManager.isFrozen() && (Object) this instanceof ServerLevel serverLevel) {
            FreezeManager.queueNeighborUpdate(serverLevel, pos);
            ci.cancel();
        }
    }

    @Inject(
            method = "neighborChanged(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void timefreeze$neighborChanged2(BlockState state, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify, CallbackInfo ci) {
        if (FreezeManager.isFrozen() && (Object) this instanceof ServerLevel serverLevel) {
            FreezeManager.queueNeighborUpdate(serverLevel, pos);
            ci.cancel();
        }
    }

    @Inject(method = "neighborShapeChanged", at = @At("HEAD"), cancellable = true)
    private void timefreeze$neighborShapeChanged(Direction direction, BlockState state, BlockPos pos, BlockPos neighborPos, int flags, int depth, CallbackInfo ci) {
        if (FreezeManager.isFrozen() && (Object) this instanceof ServerLevel serverLevel) {
            FreezeManager.queueNeighborUpdate(serverLevel, pos);
            ci.cancel();
        }
    }
}