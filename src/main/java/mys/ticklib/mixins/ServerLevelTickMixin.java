package mys.ticklib.mixins;

import mys.ticklib.freeze.FreezeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.level.ServerLevel;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelTickMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void timefreeze$cancelLevelTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (!FreezeManager.shouldRunGameElements()) {
            ci.cancel();
        }
    }
}