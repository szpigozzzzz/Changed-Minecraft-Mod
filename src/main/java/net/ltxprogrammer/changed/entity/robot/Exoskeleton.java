package net.ltxprogrammer.changed.entity.robot;

import com.mojang.datafixers.util.Pair;
import net.ltxprogrammer.changed.block.AbstractLargePanel;
import net.ltxprogrammer.changed.block.IRobotCharger;
import net.ltxprogrammer.changed.block.NineSection;
import net.ltxprogrammer.changed.data.AccessorySlots;
import net.ltxprogrammer.changed.init.ChangedAccessorySlots;
import net.ltxprogrammer.changed.init.ChangedItems;
import net.ltxprogrammer.changed.item.ExoskeletonItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
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
    public EntityDimensions getDimensions(Pose pose) {
        if (this.isCharging() && pose == Pose.SLEEPING)
            return super.getDimensions(Pose.STANDING);
        return super.getDimensions(pose);
    }

    @Override
    public float getEyeHeight(Pose pose) {
        return this.isCharging() ? this.getSleepOffset() : super.getEyeHeight(pose);
    }

    protected float getSleepOffset() {
        return 2.0F / 16.0F;
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

    @Override
    public void tick() {
        super.tick();

        if (this.isCharging() && this.getSleepingPos().isPresent()) {
            final var panel = level.getBlockState(this.getSleepingPos().get());
            if (panel.getBlock() instanceof AbstractLargePanel) {
                final var section = panel.getValue(AbstractLargePanel.SECTION);
                final var facing = panel.getValue(AbstractLargePanel.FACING);
                final var entityPos = section.getRelative(this.getSleepingPos().get(), facing, NineSection.BOTTOM_MIDDLE);

                this.setPos(entityPos.getX() + 0.5D, entityPos.getY(), entityPos.getZ() + 0.5D);
                this.setOldPosAndRot();
            }
        }
    }

    @Override
    public boolean isPushable() {
        return super.isPushable() && !this.isCharging();
    }

    @Override
    public boolean isNoGravity() {
        return super.isNoGravity() || this.isCharging();
    }

    @Override
    public void setCharging(boolean value) {
        super.setCharging(value);

        this.setPose(value ? Pose.SLEEPING : Pose.STANDING);
        this.refreshDimensions();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DATA_ID_CHARGING.equals(accessor)) {
            this.refreshDimensions();
        }
    }
}
