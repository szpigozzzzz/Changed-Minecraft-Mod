package net.ltxprogrammer.changed.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.ltxprogrammer.changed.client.renderer.model.ExoskeletonModel;
import net.ltxprogrammer.changed.entity.robot.Exoskeleton;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ExoskeletonRenderer extends MobRenderer<Exoskeleton, ExoskeletonModel> {
    public ExoskeletonRenderer(EntityRendererProvider.Context context) {
        super(context, new ExoskeletonModel(context.bakeLayer(ExoskeletonModel.LAYER_LOCATION_SUIT)), 0.4f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Exoskeleton entity) {
        return model.getTexture(entity);
    }

    @Override
    protected void setupRotations(Exoskeleton exoskeleton, PoseStack poseStack, float bob, float bodyYRot, float partialTicks) {
        super.setupRotations(exoskeleton, poseStack, bob, bodyYRot, partialTicks);
        if (exoskeleton.isCharging() && exoskeleton.getSleepingPos().isPresent()) {
            poseStack.translate(0.0D, 1 / 16.0, 0.0D);
        }

    }

    @Override
    protected float getFlipDegrees(Exoskeleton exoskeleton) {
        return exoskeleton.isCharging() ? 0.0F : super.getFlipDegrees(exoskeleton);
    }

    @Override
    public void render(Exoskeleton exoskeleton, float yRot, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        float f = (float)exoskeleton.getHurtTime() - partialTicks;
        float f1 = exoskeleton.getDamage() - partialTicks;
        if (f1 < 0.0F) {
            f1 = 0.0F;
        }

        if (f > 0.0F) {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(Mth.sin(f) * f * f1 / 10.0F * (float)exoskeleton.getHurtDir()));
        }

        super.render(exoskeleton, yRot, partialTicks, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
