package net.ltxprogrammer.changed.item;

import net.ltxprogrammer.changed.data.AccessorySlotContext;
import net.ltxprogrammer.changed.data.AccessorySlots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.Predicate;

public interface AccessoryItem {
    public static Predicate<Entity> hasSlotAvailable(ItemStack itemStack) {
        return entity -> {
            if (entity instanceof LivingEntity livingEntity) {
                return AccessorySlots.getForEntity(livingEntity).map(slots -> {
                    return slots.hasSpaceFor(itemStack);
                }).orElse(false);
            }

            return false;
        };
    }

    public static boolean dispenseAccessory(BlockSource source, ItemStack itemStack) {
        BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
        List<LivingEntity> list = source.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(blockpos), EntitySelector.NO_SPECTATORS.and(hasSlotAvailable(itemStack)));
        if (list.isEmpty()) {
            return false;
        } else {
            LivingEntity livingentity = list.get(0);
            return AccessorySlots.getForEntity(livingentity).map(slots -> {
                var copy = itemStack.copy();
                if (slots.quickMoveStack(itemStack)) {
                    AccessorySlots.equipEventAndSound(livingentity, copy);
                    return true;
                }

                return false;
            }).orElse(false);
        }
    }

    public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
        protected ItemStack execute(BlockSource source, ItemStack itemStack) {
            return AccessoryItem.dispenseAccessory(source, itemStack) ? itemStack : super.execute(source, itemStack);
        }
    };

    default void accessoryBreak(AccessorySlotContext<?> slotContext) {}
    default void accessoryInteract(AccessorySlotContext<?> slotContext) {}
}
