package mys.ticklib.event;

import mys.ticklib.Ticklib;
import mys.ticklib.freeze.FreezeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ticklib.MODID)
public final class FreezeContainerSnapshotEvents {
    private FreezeContainerSnapshotEvents() {
    }

    @SubscribeEvent
    public static void onBreakBlock(BlockEvent.BreakEvent event) {
        if (!FreezeManager.isFrozen()) {
            return;
        }

        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof Container container) {
            FreezeManager.snapshotContainerDrop(level, pos, container);
        }
    }
}
