package net.ltxprogrammer.changed.client.renderer.animate.tail;

import net.ltxprogrammer.changed.client.renderer.animate.HumanoidAnimator;
import net.ltxprogrammer.changed.entity.ChangedEntity;
import net.ltxprogrammer.changed.client.renderer.model.AdvancedHumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DragonTailCreativeFlyAnimator<T extends ChangedEntity, M extends AdvancedHumanoidModel<T>> extends AbstractTailAnimator<T, M> {
    public static final float SWAY_RATE = 0.33333334F * 0.25F;
    public static final float SWAY_SCALE = 0.10F;

    public DragonTailCreativeFlyAnimator(ModelPart tail, List<ModelPart> tailJoints) {
        super(tail, tailJoints);
    }

    @Override
    public HumanoidAnimator.AnimateStage preferredStage() {
        return HumanoidAnimator.AnimateStage.CREATIVE_FLY;
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        tail.yRot = Mth.lerp(core.flyAmount, tail.yRot, 0.0f);
        tail.xRot = Mth.lerp(core.flyAmount, tail.xRot, Mth.DEG_TO_RAD * -40.0f);
    }
}
