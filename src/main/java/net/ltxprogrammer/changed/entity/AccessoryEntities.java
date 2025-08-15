package net.ltxprogrammer.changed.entity;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.ability.IAbstractChangedEntity;
import net.ltxprogrammer.changed.data.AccessorySlotType;
import net.ltxprogrammer.changed.data.AccessorySlots;
import net.ltxprogrammer.changed.data.RegistryElementPredicate;
import net.ltxprogrammer.changed.entity.beast.CustomLatexEntity;
import net.ltxprogrammer.changed.init.ChangedRegistry;
import net.ltxprogrammer.changed.network.packet.AccessorySyncPacket;
import net.ltxprogrammer.changed.network.packet.ChangedPacket;
import net.ltxprogrammer.changed.util.Cacheable;
import net.ltxprogrammer.changed.util.EntityUtil;
import net.ltxprogrammer.changed.util.UniversalDist;
import net.ltxprogrammer.changed.world.inventory.AccessoryAccessMenu;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AccessoryEntities extends SimplePreparableReloadListener<List<Pair<RegistryElementPredicate<EntityType<?>>, AccessorySlotType>>> {
    public static final AccessoryEntities INSTANCE = new AccessoryEntities();
    private List<Pair<RegistryElementPredicate<EntityType<?>>, AccessorySlotType>> validEntitiesPreCache = null;
    private final Cacheable<Multimap<EntityType<?>, AccessorySlotType>> validEntities = Cacheable.of(() -> {
        Multimap<EntityType<?>, AccessorySlotType> working = HashMultimap.create();

        validEntitiesPreCache.forEach(pair -> {
            pair.getFirst().getValues().forEach(entityType -> {
                working.put(entityType, pair.getSecond());
            });
        });

        validEntitiesPreCache = null;
        return working;
    }).preResolved(HashMultimap.create());

    public static EntityType<?> getApparentEntityType(LivingEntity entity) {
        entity = EntityUtil.maybeGetOverlaying(entity);
        if (entity instanceof CustomLatexEntity customLatex) {
            return customLatex.getEntityTypeForAccessories();
        }

        return entity.getType();
    }

    public void forceReloadAccessories(LivingEntity entity) {
        AccessorySlots.getForEntity(entity).ifPresent(slots -> {
            final var slotsBefore = slots.getSlotTypes().collect(Collectors.toSet());

            if (!slots.initialize(
                    canEntityTypeUseSlot(getApparentEntityType(entity)),
                    AccessorySlots.defaultInvalidHandler(entity)))
                return;

            final var slotsAfter = slots.getSlotTypes().collect(Collectors.toSet());

            if (!entity.level.isClientSide) {
                Changed.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                        new AccessorySyncPacket(entity.getId(), slots));

                if (!slotsBefore.containsAll(slotsAfter) || !slotsAfter.containsAll(slotsBefore)) {
                    // Available slots changed, reopen menu for player

                    if (entity instanceof ServerPlayer player && player.containerMenu instanceof AccessoryAccessMenu menu && menu.owner == player) {
                        AccessoryAccessMenu.openForPlayer(player);
                    }
                }
            }
        });
    }

    public Predicate<AccessorySlotType> canEntityTypeUseSlot(EntityType<?> entityType) {
        final var allowedSlots = validEntities.get().get(entityType);
        return allowedSlots::contains;
    }

    private List<Pair<RegistryElementPredicate<EntityType<?>>, AccessorySlotType>> processJSONFile(JsonObject root) {
        List<Pair<RegistryElementPredicate<EntityType<?>>, AccessorySlotType>> working = new ArrayList<>();

        root.getAsJsonArray("entities").forEach(entity -> {
            final var predicate = RegistryElementPredicate.parseString(ForgeRegistries.ENTITIES, entity.getAsString());
            predicate.throwIfMissing();

            root.getAsJsonArray("slots").forEach(slot -> {
                working.add(Pair.of(predicate, ChangedRegistry.ACCESSORY_SLOTS.get().getValue(new ResourceLocation(slot.getAsString()))));
            });
        });

        return working;
    }

    @Override
    @NotNull
    public List<Pair<RegistryElementPredicate<EntityType<?>>, AccessorySlotType>> prepare(ResourceManager resources, @Nonnull ProfilerFiller profiler) {
        final var entries = resources.listResources("accessories/entities", filename ->
                ResourceLocation.isValidResourceLocation(filename) &&
                        filename.endsWith(".json"));

        List<Pair<RegistryElementPredicate<EntityType<?>>, AccessorySlotType>> working = new ArrayList<>();

        entries.forEach(filename -> {
            try {
                final Resource content = resources.getResource(filename);

                try {
                    final Reader reader = new InputStreamReader(content.getInputStream(), StandardCharsets.UTF_8);

                    working.addAll(processJSONFile(JsonParser.parseReader(reader).getAsJsonObject()));

                    reader.close();
                } catch (Exception e) {
                    content.close();
                    throw e;
                }

                content.close();
            } catch (Exception e) {
                Changed.LOGGER.error("Failed to load entities for accessories from \"{}\" : {}", filename, e);
            }
        });

        return working;
    }

    @Override
    protected void apply(@NotNull List<Pair<RegistryElementPredicate<EntityType<?>>, AccessorySlotType>> output, @NotNull ResourceManager resources, @NotNull ProfilerFiller profiler) {
        validEntitiesPreCache = output;
        validEntities.clear();
    }

    public static class SyncPacket implements ChangedPacket {
        private final Multimap<EntityType<?>, AccessorySlotType> map;
        private final @Nullable AccessorySyncPacket receiverAccessories;

        protected SyncPacket(Multimap<EntityType<?>, AccessorySlotType> map) {
            this(map, null);
        }

        protected SyncPacket(Multimap<EntityType<?>, AccessorySlotType> map, AccessorySyncPacket receiverAccessories) {
            this.map = map;
            this.receiverAccessories = receiverAccessories;
        }

        public SyncPacket(FriendlyByteBuf buffer) {
            this.map = HashMultimap.create();
            buffer.readMap(FriendlyByteBuf::readInt, mapBuffer -> {
                return mapBuffer.readCollection(HashSet::new, FriendlyByteBuf::readInt);
            }).forEach((key, slots) -> {
                final EntityType<?> entityType = Registry.ENTITY_TYPE.byId(key);
                for (var slotTypeId : slots) {
                    map.put(entityType, ChangedRegistry.ACCESSORY_SLOTS.get().getValue(slotTypeId));
                }
            });
            this.receiverAccessories = buffer.readOptional(AccessorySyncPacket::new).orElse(null);
        }

        @Override
        public void write(FriendlyByteBuf buffer) {
            var intMap = new HashMap<Integer, Set<Integer>>();
            this.map.forEach((entityType, slotType) -> {
                intMap.computeIfAbsent(Registry.ENTITY_TYPE.getId(entityType), id -> new HashSet<>())
                        .add(ChangedRegistry.ACCESSORY_SLOTS.get().getID(slotType));
            });
            buffer.writeMap(intMap, FriendlyByteBuf::writeInt,
                    (setBuffer, intSet) -> setBuffer.writeCollection(intSet, FriendlyByteBuf::writeInt));
            buffer.writeOptional(Optional.ofNullable(this.receiverAccessories), (opt, v) -> v.write(opt));
        }

        @Override
        public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
            var context = contextSupplier.get();

            if (context.getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                AccessoryEntities.INSTANCE.validEntities.clear();
                AccessoryEntities.INSTANCE.validEntities.forceValue(this.map);

                if (this.receiverAccessories != null) {
                    this.receiverAccessories.handle(contextSupplier);
                }

                context.setPacketHandled(true);
            }
        }
    }

    public SyncPacket syncPacket() {
        return new SyncPacket(this.validEntities.get());
    }

    public SyncPacket syncPacket(ServerPlayer receiver) {
        return AccessorySlots.getForEntity(receiver).map(
                slots -> new SyncPacket(this.validEntities.get(), new AccessorySyncPacket(receiver.getId(), slots))
        ).orElseGet(this::syncPacket);
    }
}
