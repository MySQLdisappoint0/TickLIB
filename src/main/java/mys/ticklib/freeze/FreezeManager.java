package mys.ticklib.freeze;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FreezeManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    // 每个维度一份待补发更新的位置
    private static final Map<ResourceKey<Level>, Set<BlockPos>> PENDING_NEIGHBOR_UPDATES = new HashMap<>();
    private static final Map<FrozenDropKey, FrozenContainerDrop> PENDING_CONTAINER_DROPS = new HashMap<>();
    private static boolean frozen = false;
    private static int stepTicks = 0;
    /**
     * 解冻时设为 true，避免调用 Containers.dropContents(...) 被 Mixin 拦截
     */
    private static boolean replayingContainerDrops = false;

    private FreezeManager() {
    }

    public static boolean isFrozen() {
        return frozen;
    }

    public static void setFrozen(boolean value) {
        boolean oldStatus = frozen;
        frozen = value;

        // 冻结 -> 解冻，补发积压更新
        if (oldStatus && !value) {
            flushAllPending();
            flushAllPendingContainerDrops();
        }
    }

    public static boolean isReplayingContainerDrops() {
        return replayingContainerDrops;
    }

    public static void step(int ticks) {
        if (ticks > 0) {
            stepTicks += ticks;
        }
    }

    /**
     * 这一tick是否允许推进游戏元素
     */
    public static boolean shouldRunGameElements() {
        if (!frozen) {
            return true;
        }
        if (stepTicks > 0) {
            stepTicks--;
            return true;
        }
        return false;
    }

    /**
     * 统一快照入口：直接从 Containers.dropContents(...) 的 mixin 调用
     */
    public static void snapshotContainerDropFromContainersHook(Level level, BlockPos pos, Container container) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        FrozenDropKey key = new FrozenDropKey(serverLevel.dimension(), pos);

        // 避免重复记录同一个位置
        if (PENDING_CONTAINER_DROPS.containsKey(key)) {
            return;
        }

        FrozenContainerDrop snapshot = new FrozenContainerDrop(
                serverLevel.dimension(),
                pos,
                FrozenContainerDrop.deepCopyFromContainer(container)
        );

        if (snapshot.isNotEmpty()) {
            PENDING_CONTAINER_DROPS.put(key, snapshot);
        }
    }

    public static void flushAllPendingContainerDrops() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            PENDING_CONTAINER_DROPS.clear();
            return;
        }

        List<FrozenContainerDrop> snapshots = new ArrayList<>(PENDING_CONTAINER_DROPS.values());
        PENDING_CONTAINER_DROPS.clear();

        replayingContainerDrops = true;
        try {
            for (FrozenContainerDrop snapshot : snapshots) {
                ServerLevel level = server.getLevel(snapshot.dimension());
                if (level == null) continue;
                if (!level.isLoaded(snapshot.pos())) continue;

                NonNullList<ItemStack> items =
                        NonNullList.withSize(snapshot.items().size(), ItemStack.EMPTY);

                for (int i = 0; i < snapshot.items().size(); i++) {
                    items.set(i, snapshot.items().get(i).copy());
                }

                // 解冻时按原位置重放掉落
                Containers.dropContents(level, snapshot.pos(), items);
            }
        } finally {
            replayingContainerDrops = false;
        }
    }

    public static void snapshotContainerDrop(ServerLevel level, BlockPos pos, Container container) {
        FrozenDropKey key = new FrozenDropKey(level.dimension(), pos);

        // 避免重复快照同一个位置
        if (PENDING_CONTAINER_DROPS.containsKey(key)) {
            return;
        }

        List<ItemStack> copied = FrozenContainerDrop.deepCopyFromContainer(container);

        FrozenContainerDrop snapshot = new FrozenContainerDrop(
                level.dimension(),
                pos,
                copied
        );

        if (snapshot.isNotEmpty()) {
            PENDING_CONTAINER_DROPS.put(key, snapshot);
        }
    }

    public static boolean hasPendingContainerDrop(Level level, BlockPos pos) {
        return PENDING_CONTAINER_DROPS.containsKey(new FrozenDropKey(level.dimension(), pos));
    }

    public static void queueNeighborUpdate(ServerLevel level, BlockPos pos) {
        Set<BlockPos> set = PENDING_NEIGHBOR_UPDATES
                .computeIfAbsent(
                        level.dimension(),
                        k -> new HashSet<>()
                );
        LOGGER.debug("Received a Source NeighborUpdate[Level={},BlockPos={}]", level, pos);

        set.add(pos.immutable());
        for (var dir : Direction.values()) {
            var neighborBlock = pos.relative(dir).immutable();
            set.add(neighborBlock);
            LOGGER.debug("Looking for the NeighborBlock, Found BlockPos={}", neighborBlock);
        }

    }

    public static void flushAllPending() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            PENDING_NEIGHBOR_UPDATES.clear();
            return;
        }

        for (Map.Entry<ResourceKey<Level>, Set<BlockPos>> entry : PENDING_NEIGHBOR_UPDATES.entrySet()) {
            ServerLevel level = server.getLevel(entry.getKey());
            if (level == null) continue;

            // 先复制一下，避免更新过程中又写回map
            List<BlockPos> positions = new ArrayList<>(entry.getValue());

            // 强制当前位置重算 shape / survival
            for (BlockPos pos : positions) {
                if (!level.isLoaded(pos)) continue;

                BlockState oldState = level.getBlockState(pos);
                BlockState newState = Block.updateFromNeighbourShapes(oldState, level, pos);

                if (newState != oldState) {
                    Block.updateOrDestroy(oldState, newState, level, pos, Block.UPDATE_ALL);
                }
            }

            // 给周围补发邻居更新
            for (BlockPos pos : positions) {
                if (!level.isLoaded(pos)) continue;

                BlockState state = level.getBlockState(pos);
                level.updateNeighborsAt(pos, state.getBlock());
            }
        }

        PENDING_NEIGHBOR_UPDATES.clear();
    }

    /*
     * 这个Record用来保存掉落位置
     */
    private record FrozenDropKey(ResourceKey<Level> dimension, BlockPos pos) {
        private FrozenDropKey {
            pos = pos.immutable();
        }
    }
}