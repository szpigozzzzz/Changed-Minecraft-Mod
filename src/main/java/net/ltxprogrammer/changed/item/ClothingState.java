package net.ltxprogrammer.changed.item;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

public class ClothingState extends StateHolder<ClothingItem, ClothingState> {
    protected ClothingState(ClothingItem item, ImmutableMap<Property<?>, Comparable<?>> properties, MapCodec<ClothingState> codec) {
        super(item, properties, codec);
    }
}
