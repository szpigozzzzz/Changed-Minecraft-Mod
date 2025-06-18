package net.ltxprogrammer.changed.init;

import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.ability.IAbstractChangedEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public class ChangedDamageSources {
    public static class TransfurDamageSource extends DamageSource {
        protected final LivingEntity entity;

        public TransfurDamageSource(String name, LivingEntity entity) {
            super(name);
            this.entity = entity;
            this.bypassArmor();
        }

        public Entity getEntity() {
            return this.entity;
        }

        public String toString() {
            return "TransfurDamageSource (" + this.entity + ")";
        }
    }

    public static final String TRANSFUR_NAME = Changed.modResourceStr("transfur");
    public static DamageSource entityTransfur(LivingEntity source) {
        return new TransfurDamageSource(TRANSFUR_NAME, source);
    }

    public static DamageSource entityTransfur(@Nullable IAbstractChangedEntity source) {
        return new TransfurDamageSource(TRANSFUR_NAME, source == null ? null : source.getEntity());
    }

    public static final String ABSORB_NAME = Changed.modResourceStr("absorb");
    public static DamageSource entityAbsorb(LivingEntity source) {
        return new TransfurDamageSource(ABSORB_NAME, source);
    }

    public static DamageSource entityAbsorb(@Nullable IAbstractChangedEntity source) {
        return new TransfurDamageSource(ABSORB_NAME, source == null ? null : source.getEntity());
    }

    public static final DamageSource BLOODLOSS = (new DamageSource(Changed.modResourceStr("bloodloss"))).bypassArmor();
    public static final DamageSource ELECTROCUTION = (new DamageSource(Changed.modResourceStr("electrocution"))).bypassArmor();
    public static final DamageSource WHITE_LATEX = (new DamageSource(Changed.modResourceStr("white_latex"))).bypassArmor().bypassMagic();
    public static final DamageSource LATEX_FLUID = (new DamageSource(Changed.modResourceStr("latex_fluid"))).bypassArmor().bypassMagic();
    public static final DamageSource PALE = (new DamageSource(Changed.modResourceStr("pale"))).bypassArmor().bypassMagic();
    public static final DamageSource FAN = new DamageSource(Changed.modResourceStr("fan"));
}
