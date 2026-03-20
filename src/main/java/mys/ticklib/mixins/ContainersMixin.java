package mys.ticklib.mixins;

import mys.ticklib.freeze.FreezeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Containers.class)
public abstract class ContainersMixin {

    @Inject(
            method = "dropContents(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/Container;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void freeze$snapshotAndCancelContainerDrop(Level level, BlockPos pos, Container container, CallbackInfo ci) {
        // 解冻重放时不拦
        if (FreezeManager.isReplayingContainerDrops()) {
            return;
        }

        if (!FreezeManager.isFrozen()) {
            return;
        }

        // 在真正准备散落时快照
        FreezeManager.snapshotContainerDropFromContainersHook(level, pos, container);

        // 冻结期间吞掉原版掉落
        if (FreezeManager.hasPendingContainerDrop(level, pos)) {
            ci.cancel();
        }
    }
}
