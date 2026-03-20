package mys.ticklib.freeze;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public record FrozenContainerDrop(ResourceKey<Level> dimension, BlockPos pos, List<ItemStack> items) {
    public FrozenContainerDrop(ResourceKey<Level> dimension, BlockPos pos, List<ItemStack> items) {
        this.dimension = dimension;
        this.pos = pos.immutable();
        this.items = items;
    }

    public static List<ItemStack> deepCopyFromContainer(Container container) {
        List<ItemStack> out = new ArrayList<>(container.getContainerSize());
        for (int i = 0; i < container.getContainerSize(); i++) {
            out.add(container.getItem(i).copy());
        }
        return out;
    }

    public boolean isNotEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return true;
        }
        return false;
    }
}
