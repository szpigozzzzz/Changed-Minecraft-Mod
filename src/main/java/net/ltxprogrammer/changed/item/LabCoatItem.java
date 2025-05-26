package net.ltxprogrammer.changed.item;

import net.ltxprogrammer.changed.data.AccessorySlotContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

public class LabCoatItem extends ClothingItem {
    public LabCoatItem() {
        this.registerDefaultState(this.stateDefinition.any().setValue(CLOSED, false));
    }

    @Override
    protected void createClothingStateDefinition(StateDefinition.Builder<ClothingItem, ClothingState> builder) {
        super.createClothingStateDefinition(builder);
        builder.add(CLOSED);
    }

    @Override
    public void accessoryInteract(AccessorySlotContext<?> slotContext) {
        super.accessoryInteract(slotContext);
        this.setClothingState(slotContext.stack(), this.getClothingState(slotContext.stack()).cycle(CLOSED));
        SoundEvent changeSound = this.getEquipSound();
        if (changeSound != null)
            slotContext.wearer().playSound(changeSound, 1F, 1F);
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        ResourceLocation itemId = stack.getItem().getRegistryName();
        if (this.getClothingState(stack).getValue(CLOSED))
            return String.format("%s:textures/models/%s_closed.png", itemId.getNamespace(), itemId.getPath());
        else
            return String.format("%s:textures/models/%s.png", itemId.getNamespace(), itemId.getPath());
    }
}
