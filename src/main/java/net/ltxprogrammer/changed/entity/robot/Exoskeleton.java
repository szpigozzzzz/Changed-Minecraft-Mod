package net.ltxprogrammer.changed.entity.robot;

import com.mojang.datafixers.util.Pair;
import net.ltxprogrammer.changed.block.AbstractLargePanel;
import net.ltxprogrammer.changed.block.IRobotCharger;
import net.ltxprogrammer.changed.block.NineSection;
import net.ltxprogrammer.changed.data.AccessorySlots;
import net.ltxprogrammer.changed.entity.TransfurCause;
import net.ltxprogrammer.changed.entity.TransfurContext;
import net.ltxprogrammer.changed.entity.variant.TransfurVariant;
import net.ltxprogrammer.changed.entity.variant.TransfurVariantInstance;
import net.ltxprogrammer.changed.init.*;
import net.ltxprogrammer.changed.item.BenignPants;
import net.ltxprogrammer.changed.item.ExoskeletonItem;
import net.ltxprogrammer.changed.process.ProcessTransfur;
import net.ltxprogrammer.changed.util.EntityUtil;
import net.ltxprogrammer.changed.util.ItemUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

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

    public ItemStack getDropItem() {
        return new ItemStack(ChangedItems.EXOSKELETON.get());
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

    protected boolean targetIsBenignLatex(LivingEntity entity) {
        return EntityUtil.maybeGetOverlaying(entity).getType().is(ChangedTags.EntityTypes.BENIGN_LATEXES);
    }

    protected boolean targetHasBenignPants(LivingEntity entity) {
        return EntityUtil.maybeGetOverlaying(entity).getType().is(ChangedTags.EntityTypes.HUMANOIDS) &&
                AccessorySlots.isWearing(entity, stack -> stack.is(ChangedItems.BENIGN_PANTS.get()));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new ExoskeletonAttackGoal(this, 0.4, false));
        this.goalSelector.addGoal(2, new ExoskeletonWanderGoal(this, 0.3, 120, false));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true, this::targetIsBenignLatex));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true, this::targetHasBenignPants));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0f)
                .add(Attributes.ARMOR, 20.0f)
                .add(Attributes.MOVEMENT_SPEED, 0.5D);
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState state) {
        this.playSound(ChangedSounds.EXOSKELETON_STEP, 0.3F, this.random.nextFloat(0.8f, 0.95f));
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

    public static class ExoskeletonAttackGoal extends MeleeAttackGoal {
        protected final Exoskeleton exoskeleton;

        public ExoskeletonAttackGoal(Exoskeleton exoskeleton, double speedModifier, boolean visualPersistence) {
            super(exoskeleton, speedModifier, visualPersistence);

            this.exoskeleton = exoskeleton;
        }

        @Override
        protected double getAttackReachSqr(LivingEntity target) {
            if (exoskeleton.targetHasBenignPants(target))
                return super.getAttackReachSqr(target) * 1.5;
            else return super.getAttackReachSqr(target) * 0.75;
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target, double distanceSquared) {
            if (exoskeleton.targetIsBenignLatex(target)) {
                if (ProcessTransfur.getPlayerTransfurVariantSafe(EntityUtil.playerOrNull(target)).map(TransfurVariantInstance::isTransfurring).orElse(false))
                    return; // Wait for player to TF

                double reachSqr = this.getAttackReachSqr(target) * 0.75;

                if (distanceSquared <= reachSqr && this.getTicksUntilNextAttack() <= 0) {
                    this.resetAttackCooldown();

                    if (AccessorySlots.tryReplaceSlot(target, ChangedAccessorySlots.FULL_BODY.get(), exoskeleton.getDropItem()))
                        exoskeleton.discard();
                    else
                        exoskeleton.setTarget(null);
                }
            }

            else if (exoskeleton.targetHasBenignPants(target)) {
                double reachSqr = this.getAttackReachSqr(target) * 1.5;

                if (distanceSquared <= reachSqr && this.getTicksUntilNextAttack() <= 0) {
                    this.resetAttackCooldown();
                    this.mob.swing(InteractionHand.MAIN_HAND); // TODO: laser

                    ItemUtil.isWearingItem(target, ChangedItems.BENIGN_PANTS.get()).ifPresent(slottedItem -> {
                        if (ProcessTransfur.progressTransfur(target, 11.0f, BenignPants.getBenignTransfurVariant(target), TransfurContext.hazard(TransfurCause.WAIST_HAZARD)))
                            slottedItem.itemStack().shrink(1);
                    });
                }
            }

            else {
                // Re-evaluate nearby entities
                exoskeleton.setTarget(null);
            }
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !exoskeleton.isCharging() && !exoskeleton.isLowBattery();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !exoskeleton.isCharging() && !exoskeleton.isLowBattery();
        }
    }

    public static class ExoskeletonWanderGoal extends RandomStrollGoal {
        protected final Exoskeleton exoskeleton;

        public ExoskeletonWanderGoal(Exoskeleton exoskeleton, double speedModifier, int interval, boolean checkNoAction) {
            super(exoskeleton, speedModifier, interval, checkNoAction);

            this.exoskeleton = exoskeleton;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !exoskeleton.isCharging() && !exoskeleton.isLowBattery();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !exoskeleton.isCharging() && !exoskeleton.isLowBattery();
        }
    }
}
