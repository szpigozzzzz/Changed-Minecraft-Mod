package net.ltxprogrammer.changed.mixin.compatibility.Vivecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import net.ltxprogrammer.changed.client.FormRenderHandler;
import net.ltxprogrammer.changed.client.renderer.layers.FirstPersonLayer;
import net.ltxprogrammer.changed.extension.RequiredMods;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.render.VRArmRenderer;

@Mixin(value = VRArmRenderer.class, remap = false)
@RequiredMods("vivecraft")
public abstract class VRArmRendererMixin extends PlayerRenderer {
    private VRArmRendererMixin(EntityRendererProvider.Context p_174557_, boolean p_174558_) {
        super(p_174557_, p_174558_);
    }

    @Unique
    private boolean renderTransfurredArmPre(ControllerType side, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                                            AbstractClientPlayer playerIn, ModelPart rendererArmIn, ModelPart rendererArmwearIn) {
        return FormRenderHandler.maybeRenderHand((VRArmRenderer)(Object)this, matrixStackIn, bufferIn, combinedLightIn, playerIn, rendererArmIn, rendererArmwearIn);
    }

    @Unique
    private void renderTransfurredArmPost(ControllerType side, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                                          AbstractClientPlayer playerIn, ModelPart rendererArmIn, ModelPart rendererArmwearIn) {
        for (var layer : layers) {
            if (layer instanceof FirstPersonLayer firstPersonLayer)
                firstPersonLayer.renderFirstPersonOnArms(
                        matrixStackIn, bufferIn, combinedLightIn, playerIn, getModel().rightArm != rendererArmIn ? HumanoidArm.LEFT : HumanoidArm.RIGHT,
                        new PoseStack(), Minecraft.getInstance().getDeltaFrameTime());
        }
    }

    @Inject(method = "renderItem", at = @At("HEAD"), require = 0, cancellable = true)
    private void renderItemOverride(ControllerType side, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                                   AbstractClientPlayer playerIn, ModelPart rendererArmIn, ModelPart rendererArmwearIn, CallbackInfo callback) {
        if (renderTransfurredArmPre(side, matrixStackIn, bufferIn, combinedLightIn, playerIn, rendererArmIn, rendererArmwearIn))
            callback.cancel();
    }

    @Inject(method = "renderHand", at = @At("HEAD"), require = 0, cancellable = true)
    private void renderHandOverride(ControllerType side, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                                   AbstractClientPlayer playerIn, ModelPart rendererArmIn, ModelPart rendererArmwearIn, CallbackInfo callback) {
        if (renderTransfurredArmPre(side, matrixStackIn, bufferIn, combinedLightIn, playerIn, rendererArmIn, rendererArmwearIn))
            callback.cancel();
    }

    @Inject(method = "renderItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V"), require = 0, cancellable = true)
    private void renderItemLayers(ControllerType side, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                                   AbstractClientPlayer playerIn, ModelPart rendererArmIn, ModelPart rendererArmwearIn, CallbackInfo callback) {
        renderTransfurredArmPost(side, matrixStackIn, bufferIn, combinedLightIn, playerIn, rendererArmIn, rendererArmwearIn);
    }

    @Inject(method = "renderHand", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V"), require = 0, cancellable = true)
    private void renderHandLayers(ControllerType side, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                                   AbstractClientPlayer playerIn, ModelPart rendererArmIn, ModelPart rendererArmwearIn, CallbackInfo callback) {
        renderTransfurredArmPost(side, matrixStackIn, bufferIn, combinedLightIn, playerIn, rendererArmIn, rendererArmwearIn);
    }
}
