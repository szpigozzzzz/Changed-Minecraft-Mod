package net.ltxprogrammer.changed.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.ltxprogrammer.changed.Changed;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin {
    @WrapMethod(method = "load")
    public void updateWithCDFU(CompoundTag tag, Operation<Void> original) {
        if (Changed.dataFixer != null)
            Changed.dataFixer.updateCompoundTag(DataFixTypes.STRUCTURE, tag);
        original.call(tag);
    }
}
