package net.ltxprogrammer.changed.item;

import net.ltxprogrammer.changed.data.AccessorySlots;
import net.ltxprogrammer.changed.init.ChangedSounds;
import net.ltxprogrammer.changed.init.ChangedTabs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public class ClothingItem extends Item implements Wearable, Clothing, ExtendedItemProperties {
    public static BooleanProperty CLOSED = BooleanProperty.create("closed");

    public StateDefinition<ClothingItem, ClothingState> stateDefinition;
    public ClothingState defaultClothingState;

    public ClothingItem() {
        super(new Properties().tab(ChangedTabs.TAB_CHANGED_ITEMS).durability(5));
        StateDefinition.Builder<ClothingItem, ClothingState> builder = new StateDefinition.Builder<>(this);
        this.createClothingStateDefinition(builder);
        this.stateDefinition = builder.create(ClothingItem::defaultClothingState, ClothingState::new);
        this.registerDefaultState(this.stateDefinition.any());
        DispenserBlock.registerBehavior(this, AccessoryItem.DISPENSE_ITEM_BEHAVIOR);
    }

    protected void createClothingStateDefinition(StateDefinition.Builder<ClothingItem, ClothingState> builder) {

    }

    public ClothingState defaultClothingState() {
        return this.defaultClothingState;
    }

    @SuppressWarnings("unchecked")
    public ClothingState getClothingState(ItemStack stack) {
        var compoundTag = stack.getTag();
        if (compoundTag == null)
            return this.defaultClothingState();

        var stateData = compoundTag.getCompound("state");
        AtomicReference<ClothingState> evaluatedState = new AtomicReference<>(this.defaultClothingState());
        stateData.getAllKeys().forEach(propertyName -> {
            Property property = this.stateDefinition.getProperty(propertyName);
            if (property == null)
                return;

            property.getValue(stateData.getString(propertyName)).ifPresent(value -> {
                evaluatedState.set(evaluatedState.get().setValue(property, (Comparable)value));
            });
        });
        return evaluatedState.getAcquire();
    }

    @SuppressWarnings("unchecked")
    public void setClothingState(ItemStack stack, ClothingState state) {
        CompoundTag tag = new CompoundTag();
        state.getProperties().forEach(property -> {
            tag.putString(property.getName(), ((Property) property).getName(state.getValue((Property) property)));
        });

        stack.getOrCreateTag().put("state", tag);
    }

    protected final void registerDefaultState(ClothingState clothingState) {
        this.defaultClothingState = clothingState;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public SoundEvent getEquipSound() {
        return ChangedSounds.EQUIP3;
    }

    @Override
    public SoundEvent getBreakSound(ItemStack itemStack) {
        return ChangedSounds.SLASH10;
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        ResourceLocation itemId = stack.getItem().getRegistryName();
        //return String.format("%s:textures/models/%s_%s.png", itemId.getNamespace(), itemId.getPath(), Mth.clamp(stack.getDamageValue() - 1, 0, 4));
        return String.format("%s:textures/models/%s.png", itemId.getNamespace(), itemId.getPath());
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        return AccessorySlots.getForEntity(player).map(slots -> {
            var copy = stack.copy();
            if (slots.quickMoveStack(stack)) {
                AccessorySlots.equipEventAndSound(player, copy);
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }

            return InteractionResultHolder.pass(stack);
        }).orElse(InteractionResultHolder.pass(stack));
    }
}
