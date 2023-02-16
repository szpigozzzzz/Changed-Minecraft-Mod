package net.ltxprogrammer.changed.entity.beast;

import net.ltxprogrammer.changed.entity.Gender;
import net.ltxprogrammer.changed.entity.HairStyle;
import net.ltxprogrammer.changed.entity.TransfurMode;
import net.ltxprogrammer.changed.init.ChangedParticles;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class LatexMantaRayFemale extends AbstractLatexMantaRay {
    public LatexMantaRayFemale(EntityType<? extends LatexMantaRayFemale> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Override
    public Gender getGender() {
        return Gender.FEMALE;
    }

    @Override
    public TransfurMode getTransfurMode() {
        return TransfurMode.ABSORPTION;
    }

    @Override
    public HairStyle getDefaultHairStyle() {
        return HairStyle.BALD;
    }

    @Override
    public ChangedParticles.Color3 getHairColor() {
        return ChangedParticles.Color3.getColor("#6f7696");
    }

    @Override
    public boolean isVisuallySwimming() {
        if (this.getUnderlyingPlayer() != null && this.getUnderlyingPlayer().isEyeInFluid(FluidTags.WATER))
            return true;
        return this.isEyeInFluid(FluidTags.WATER) || super.isVisuallySwimming();
    }
}
