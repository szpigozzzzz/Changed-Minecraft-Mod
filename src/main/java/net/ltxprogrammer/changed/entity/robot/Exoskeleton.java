package net.ltxprogrammer.changed.entity.robot;

import com.mojang.datafixers.util.Pair;
import net.ltxprogrammer.changed.data.AccessorySlots;
import net.ltxprogrammer.changed.init.ChangedAccessorySlots;
import net.ltxprogrammer.changed.init.ChangedItems;
import net.ltxprogrammer.changed.item.ExoskeletonItem;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class Exoskeleton extends AbstractRobot {
    public static Optional<Pair<ItemStack, ExoskeletonItem<?>>> getEntityExoskeleton(LivingEntity entity) {
        return AccessorySlots.getForEntity(entity)
                .flatMap(slots -> slots.getItem(ChangedAccessorySlots.FULL_BODY.get()))
                .map(stack -> {
                    if (stack.getItem() instanceof ExoskeletonItem<?> exo)
                        return Pair.of(stack, exo);
                    return null;
                });
    }

    public Exoskeleton(EntityType<? extends Exoskeleton> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
    }

    public ItemLike getDropItem() {
        return ChangedItems.EXOSKELETON.get();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0f)
                .add(Attributes.ARMOR, 20.0f)
                .add(Attributes.MOVEMENT_SPEED, 0.15D);
    }

    @Override
    protected void playStepSound(BlockPos p_20135_, BlockState p_20136_) {
        // Omitted
    }

    @Override
    public boolean isAffectedByWater() {
        return true;
    }

    @Override
    public SoundEvent getRunningSound() {
        return null;
    }

    @Override
    public int getRunningSoundDuration() {
        return 100;
    }

    @Override
    public float getMaxDamage() {
        return 40f;
    }

    @Override
    public ChargerType getChargerType() {
        return ChargerType.EXOSKELETON;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        boolean moved = AccessorySlots.getForEntity(player).map(slots ->
                slots.moveToSlot(ChangedAccessorySlots.FULL_BODY.get(), new ItemStack(ChangedItems.EXOSKELETON.get()))).orElse(false);
        if (moved) {
            if (!this.level.isClientSide)
                this.discard();
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        return super.mobInteract(player, hand);
    }
}
