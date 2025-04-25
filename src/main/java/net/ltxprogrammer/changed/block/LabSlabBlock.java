package net.ltxprogrammer.changed.block;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class LabSlabBlock extends SlabBlock {
    public LabSlabBlock(Supplier<BlockState> type, Properties properties) {
        super(properties.requiresCorrectToolForDrops());
    }

    @Override
    public List<ItemStack> getDrops(BlockState blockState, LootContext.Builder builder) {
        return new ArrayList<>(Collections.singleton(this.asItem().getDefaultInstance()));
    }
}
