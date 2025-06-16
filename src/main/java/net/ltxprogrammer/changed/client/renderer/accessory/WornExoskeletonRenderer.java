package net.ltxprogrammer.changed.client.renderer.accessory;

import com.mojang.blaze3d.vertex.PoseStack;
import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.client.renderer.LatexHumanRenderer;
import net.ltxprogrammer.changed.client.renderer.model.ExoskeletonModel;
import net.ltxprogrammer.changed.data.AccessorySlotContext;
import net.ltxprogrammer.changed.entity.robot.Exoskeleton;
import net.ltxprogrammer.changed.util.Cacheable;
import net.ltxprogrammer.changed.util.EntityUtil;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class WornExoskeletonRenderer implements AccessoryRenderer {
    private final Cacheable<ExoskeletonModel> suitModel;
    private final Cacheable<ExoskeletonModel.ReplacementLimbs> suitLegsModel;

    public WornExoskeletonRenderer(EntityModelSet modelSet, ModelLayerLocation model, ModelLayerLocation legsModel) {
        this.suitModel = Cacheable.of(() -> {
            try {
                return new ExoskeletonModel(modelSet.bakeLayer(model));
            } catch (Exception ex) {
                ex.printStackTrace();
                Changed.LOGGER.error("Failed to initialize exoskeleton model. This error is likely from a mod incompatibility.");
                return null;
            }
        });
        this.suitLegsModel = Cacheable.of(() -> {
            try {
                return new ExoskeletonModel.ReplacementLimbs(modelSet.bakeLayer(legsModel));
            } catch (Exception ex) {
                ex.printStackTrace();
                Changed.LOGGER.error("Failed to initialize exoskeleton model. This error is likely from a mod incompatibility.");
                return null;
            }
        });
    }

    public ExoskeletonModel getModel() {
        return suitModel.get();
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(AccessorySlotContext<T> slotContext, PoseStack pose, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        T wearer = slotContext.wearer();
        ItemStack stack = slotContext.stack();

        var suitModel = this.suitModel.getOrThrow();

        pose.pushPose();
        /*ModelPart modelpart = this.getParentModel().getHead();
        if (this.getParentModel() instanceof AdvancedHumanoidModelInterface<?,?> modelInterface)
            modelInterface.scaleForHead(pose);

        modelpart.translateAndRotate(pose);*/
        suitModel.prepareMobModel(stack, limbSwing, limbSwingAmount, partialTicks);
        suitModel.matchWearersAnim(renderLayerParent.getModel(), stack);
        suitModel.renderToBuffer(pose, renderTypeBuffer.getBuffer(suitModel.renderType(suitModel.getTexture(stack))), light, LivingEntityRenderer.getOverlayCoords(wearer, 0.0F), 1.0f, 1.0f, 1.0f, 1.0f);

        if (renderLayerParent instanceof LatexHumanRenderer || renderLayerParent instanceof PlayerRenderer) {
            var legsModel = this.suitLegsModel.getOrThrow();
            //legsModel.prepareMobModel(exoskeleton, limbSwing, limbSwingAmount, partialTicks);
            legsModel.matchWearersAnim(renderLayerParent.getModel(), stack);
            legsModel.renderToBuffer(pose, renderTypeBuffer.getBuffer(legsModel.renderType(legsModel.getTexture(wearer))), light, LivingEntityRenderer.getOverlayCoords(wearer, 0.0F), 1.0f, 1.0f, 1.0f, 1.0f);
        }

        pose.popPose();
    }
}
