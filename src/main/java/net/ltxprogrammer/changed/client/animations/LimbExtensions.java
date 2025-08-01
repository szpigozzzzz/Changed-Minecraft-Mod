package net.ltxprogrammer.changed.client.animations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.client.renderer.animate.legless.AbstractLeglessAnimator;
import net.ltxprogrammer.changed.client.renderer.animate.tail.AbstractTailAnimator;
import net.ltxprogrammer.changed.client.renderer.animate.wing.AbstractWingAnimatorV2;
import net.ltxprogrammer.changed.client.renderer.animate.wing.BeeWingInitAnimator;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LimbExtensions {
    // TODO write and post gather event
    private static final Map<ResourceLocation, LimbExtension> EXTENSIONS = new HashMap<>();

    public static DataResult<LimbExtension> fromSerial(ResourceLocation name) {
        return EXTENSIONS.keySet().stream().filter(type -> type.equals(name))
                .findFirst().map(EXTENSIONS::get).map(DataResult::success).orElseGet(() -> DataResult.error(name + " is not a recognized LimbExtension"));
    }

    public static ResourceLocation toSerial(LimbExtension extension) {
        return EXTENSIONS.entrySet().stream().filter(type -> type.getValue() == extension)
                .findFirst().map(Map.Entry::getKey).orElseThrow(() -> new IllegalArgumentException("Attempted to serialize unregistered LimbExtension"));
    }

    public static @Nullable ResourceLocation toSerialOrNull(LimbExtension extension) {
        return EXTENSIONS.entrySet().stream().filter(type -> type.getValue() == extension)
                .findFirst().map(Map.Entry::getKey).orElse(null);
    }

    private static LimbExtension register(String name, LimbExtension extension) {
        EXTENSIONS.put(Changed.modResource(name), extension);
        return extension;
    }

    public static final LimbExtension LEFT_EAR = register("left_ear",
            LimbExtension.simple(Set.of(Limb.HEAD, Limb.HEAD2, Limb.HEAD3), "LeftEar"));
    public static final LimbExtension RIGHT_EAR = register("right_ear",
            LimbExtension.simple(Set.of(Limb.HEAD, Limb.HEAD2, Limb.HEAD3), "RightEar"));
    public static final LimbExtension TAIL = register("tail",
            LimbExtension.<AbstractTailAnimator<?,?>>forAnimator(Set.of(Limb.TORSO, Limb.LOWER_TORSO), AbstractTailAnimator.class,
                    (animator, limb, limbRoot, param) -> {
                if (param == null)
                    return animator.tail;

                try {
                    int index = Integer.parseInt(param);
                    if (index == 0)
                        return animator.tail;
                    else if (animator.tailJoints.size() >= index)
                        return animator.tailJoints.get(index - 1);
                } catch (Exception ignored) {}

                if (param.equals("root"))
                    return animator.tail;
                else if (param.equals("primary") && !animator.tailJoints.isEmpty())
                    return animator.tailJoints.get(0);
                else if (param.equals("secondary") && animator.tailJoints.size() >= 2)
                    return animator.tailJoints.get(1);
                else if (param.equals("tertiary") && animator.tailJoints.size() >= 3)
                    return animator.tailJoints.get(2);
                else if (param.equals("quaternary") && animator.tailJoints.size() >= 4)
                    return animator.tailJoints.get(3);

                return null;
            }));
    public static final LimbExtension LEGLESS = register("legless",
            LimbExtension.<AbstractLeglessAnimator<?,?>>forAnimator(Set.of(Limb.ABDOMEN), AbstractLeglessAnimator.class,
                    (animator, limb, limbRoot, param) -> {
                        if (param == null)
                            return animator.lowerAbdomen;

                        try {
                            int index = Integer.parseInt(param);
                            if (index == 0)
                                return animator.lowerAbdomen;
                            if (index == 1)
                                return animator.tail;
                            else if (animator.tailJoints.size() >= index)
                                return animator.tailJoints.get(index - 2);
                        } catch (Exception ignored) {}

                        if (param.equals("root"))
                            return animator.lowerAbdomen;
                        else if (param.equals("primary"))
                            return animator.tail;
                        else if (param.equals("secondary") && !animator.tailJoints.isEmpty())
                            return animator.tailJoints.get(0);
                        else if (param.equals("tertiary") && animator.tailJoints.size() >= 2)
                            return animator.tailJoints.get(1);
                        else if (param.equals("quaternary") && animator.tailJoints.size() >= 3)
                            return animator.tailJoints.get(2);

                        return null;
                    }));
    public static final LimbExtension LEFT_DRAGON_WING = register("left_dragon_wing",
            LimbExtension.<AbstractWingAnimatorV2<?,?>>forAnimator(Set.of(Limb.TORSO), AbstractWingAnimatorV2.class,
                    (animator, limb, limbRoot, param) -> {
                        if (param == null)
                            return animator.leftWingRoot;

                        try {
                            int index = Integer.parseInt(param);
                            return switch (index) {
                                case 0 -> animator.leftWingRoot;
                                case 1 -> animator.leftWingBone1;
                                case 2 -> animator.leftWingBone2;
                                default -> null;
                            };
                        } catch (Exception ignored) {}

                        return switch (param) {
                            case "root" -> animator.leftWingRoot;
                            case "bone1" -> animator.leftWingBone1;
                            case "bone2" -> animator.leftWingBone2;
                            default -> null;
                        };
                    }));
    public static final LimbExtension RIGHT_DRAGON_WING = register("right_dragon_wing",
            LimbExtension.<AbstractWingAnimatorV2<?,?>>forAnimator(Set.of(Limb.TORSO), AbstractWingAnimatorV2.class,
                    (animator, limb, limbRoot, param) -> {
                        if (param == null)
                            return animator.rightWingRoot;

                        try {
                            int index = Integer.parseInt(param);
                            return switch (index) {
                                case 0 -> animator.rightWingRoot;
                                case 1 -> animator.rightWingBone1;
                                case 2 -> animator.rightWingBone2;
                                default -> null;
                            };
                        } catch (Exception ignored) {}

                        return switch (param) {
                            case "root" -> animator.rightWingRoot;
                            case "bone1" -> animator.rightWingBone1;
                            case "bone2" -> animator.rightWingBone2;
                            default -> null;
                        };
                    }));
    public static final LimbExtension LEFT_INSECT_WING = register("left_insect_wing",
            LimbExtension.<BeeWingInitAnimator<?,?>>forAnimator(Set.of(Limb.TORSO), BeeWingInitAnimator.class,
                    (animator, limb, limbRoot, param) -> {
                        return animator.leftWingRoot;
                    }));
    public static final LimbExtension RIGHT_INSECT_WING = register("right_insect_wing",
            LimbExtension.<BeeWingInitAnimator<?,?>>forAnimator(Set.of(Limb.TORSO), BeeWingInitAnimator.class,
                    (animator, limb, limbRoot, param) -> {
                        return animator.rightWingRoot;
                    }));

    public static final Codec<LimbExtension> CODEC = ResourceLocation.CODEC.comapFlatMap(LimbExtensions::fromSerial, LimbExtensions::toSerial);
}
