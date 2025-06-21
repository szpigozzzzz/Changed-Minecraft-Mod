package net.ltxprogrammer.changed.client.renderer.model;

import net.ltxprogrammer.changed.block.StasisChamber;
import net.ltxprogrammer.changed.client.ClientLivingEntityExtender;
import net.ltxprogrammer.changed.util.EntityUtil;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public interface LeglessModel {
    ModelPart getAbdomen();

    public static boolean shouldLeglessSit(LivingEntity entity) {
        return StasisChamber.isEntityCaptured(EntityUtil.maybeGetUnderlying(entity));
    }
}
