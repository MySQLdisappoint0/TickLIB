package mys.ticklib.event;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
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

    public static List<ItemStack> deepCopy(List<ItemStack> src) {
        List<ItemStack> out = new ArrayList<>(src.size());
        for (ItemStack stack : src) {
            out.add(stack.copy());
        }
        return out;
    }

    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
