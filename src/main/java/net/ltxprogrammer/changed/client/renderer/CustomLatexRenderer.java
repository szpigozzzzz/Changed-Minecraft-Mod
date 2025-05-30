package net.ltxprogrammer.changed.client.renderer;

import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.client.renderer.layers.GasMaskLayer;
import net.ltxprogrammer.changed.client.renderer.layers.LatexParticlesLayer;
import net.ltxprogrammer.changed.client.renderer.layers.TransfurCapeLayer;
import net.ltxprogrammer.changed.client.renderer.model.CustomLatexModel;
import net.ltxprogrammer.changed.client.renderer.model.PureWhiteLatexWolfModel;
import net.ltxprogrammer.changed.client.renderer.model.armor.*;
import net.ltxprogrammer.changed.entity.ChangedEntity;
import net.ltxprogrammer.changed.entity.beast.CustomLatexEntity;
import net.ltxprogrammer.changed.entity.beast.PureWhiteLatexWolf;
import net.ltxprogrammer.changed.item.AbdomenArmor;
import net.ltxprogrammer.changed.item.QuadrupedalArmor;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomLatexRenderer extends AdvancedHumanoidRenderer<CustomLatexEntity, CustomLatexModel, LatexHumanoidArmorModel<CustomLatexEntity, ?>> {
	public static class CustomArmorPicker extends ArmorModelPicker<CustomLatexEntity> {
		// Upper
		private final Map<ArmorModel, ? extends LatexHumanoidArmorModel<ChangedEntity, ?>> bakedMaleCanine;
		private final Map<ArmorModel, ? extends LatexHumanoidArmorModel<ChangedEntity, ?>> bakedMaleFeline;
		private final Map<ArmorModel, ? extends LatexHumanoidArmorModel<ChangedEntity, ?>> bakedMaleShark;
		private final Map<ArmorModel, ? extends LatexHumanoidArmorModel<ChangedEntity, ?>> bakedMaleDragon;
		private final Map<ArmorModel, ? extends LatexHumanoidArmorModel<ChangedEntity, ?>> bakedFemaleCanine;
		private final Map<ArmorModel, ? extends LatexHumanoidArmorModel<ChangedEntity, ?>> bakedFemaleFeline;
		private final Map<ArmorModel, ? extends LatexHumanoidArmorModel<ChangedEntity, ?>> bakedFemaleShark;
		private final Map<ArmorModel, ? extends LatexHumanoidArmorModel<ChangedEntity, ?>> bakedFemaleDragon;

		private final Map<ArmorModel, ? extends LatexHumanoidArmorModel<ChangedEntity, ?>> bakedMaleMerUpper;
		private final Map<ArmorModel, ? extends LatexHumanoidArmorModel<ChangedEntity, ?>> bakedFemaleMerUpper;

		// Lower
		private final Map<ArmorModel, ? extends LatexHumanoidArmorModel<ChangedEntity, ?>> bakedTaur;
		private final Map<ArmorModel, ? extends LatexHumanoidArmorModel<ChangedEntity, ?>> bakedMer;

		public CustomArmorPicker(EntityModelSet models) {
			this.bakedMaleCanine = ArmorLatexMaleWolfModel.MODEL_SET.createModels(models);
			this.bakedMaleFeline = ArmorLatexMaleCatModel.MODEL_SET.createModels(models);
			this.bakedMaleShark = ArmorLatexMaleSharkModel.MODEL_SET.createModels(models);
			this.bakedMaleDragon = ArmorLatexMaleDragonModel.MODEL_SET.createModels(models);
			this.bakedFemaleCanine = ArmorLatexFemaleWolfModel.MODEL_SET.createModels(models);
			this.bakedFemaleFeline = ArmorLatexFemaleCatModel.MODEL_SET.createModels(models);
			this.bakedFemaleShark = ArmorLatexFemaleSharkModel.MODEL_SET.createModels(models);
			this.bakedFemaleDragon = ArmorLatexFemaleDragonModel.MODEL_SET.createModels(models);

			this.bakedMaleMerUpper = ArmorMermaidSharkUpperBodyModel.MODEL_SET.createModels(models);
			this.bakedFemaleMerUpper = ArmorSirenUpperBodyModel.MODEL_SET.createModels(models);

			this.bakedTaur = ArmorLatexCentaurLowerModel.MODEL_SET.createModels(models);
			this.bakedMer = ArmorSirenAbdomenModel.MODEL_SET.createModels(models);
		}

		@Override
		public LatexHumanoidArmorModel<CustomLatexEntity, ?> getModelForSlot(CustomLatexEntity entity, EquipmentSlot slot) {
			final var set = this.getModelSetForSlot(entity, slot);

			return switch (entity.getLegType()) {
				case BIPEDAL ->	set.get(slot == EquipmentSlot.LEGS ? ArmorModel.ARMOR_INNER : ArmorModel.ARMOR_OUTER);
				case CENTAUR -> set.get(QuadrupedalArmor.useInnerQuadrupedalModel(slot) ? ArmorModel.ARMOR_INNER : ArmorModel.ARMOR_OUTER);
				case MERMAID -> set.get(AbdomenArmor.useInnerAbdomenModel(slot) ? ArmorModel.ARMOR_INNER : ArmorModel.ARMOR_OUTER);
			};
		}

		private Map<ArmorModel, ? extends LatexHumanoidArmorModel<?, ?>> getModelSetByTailAndTorso(CustomLatexEntity.TailType tailType, CustomLatexEntity.TorsoType torsoType) {
			return switch (tailType) {
				case WOLF -> torsoType == CustomLatexEntity.TorsoType.FEMALE ? this.bakedFemaleCanine : this.bakedMaleCanine;
				case CAT -> torsoType == CustomLatexEntity.TorsoType.FEMALE ? this.bakedFemaleFeline : this.bakedMaleFeline;
				case SHARK -> torsoType == CustomLatexEntity.TorsoType.FEMALE ? this.bakedFemaleShark : this.bakedMaleShark;
				case DRAGON -> torsoType == CustomLatexEntity.TorsoType.FEMALE ? this.bakedFemaleDragon : this.bakedMaleDragon;
				default -> Map.of();
			};
		}

		@Override
		public Map<ArmorModel, LatexHumanoidArmorModel<CustomLatexEntity, ?>> getModelSetForSlot(CustomLatexEntity entity, EquipmentSlot slot) {
			Map<ArmorModel, ? extends LatexHumanoidArmorModel<?, ?>> found = Map.of();

			switch (entity.getLegType()) {
				case BIPEDAL -> {
					found = getModelSetByTailAndTorso(entity.getTailType(), entity.getTorsoType());
				}
				case CENTAUR -> {
					found = QuadrupedalArmor.useQuadrupedalModel(slot) ? this.bakedTaur : getModelSetByTailAndTorso(entity.getTailType(), entity.getTorsoType());
				}
				case MERMAID -> {
					found = QuadrupedalArmor.useQuadrupedalModel(slot) ? this.bakedMer : (entity.getTorsoType() == CustomLatexEntity.TorsoType.FEMALE ? this.bakedFemaleMerUpper : this.bakedMaleMerUpper);
				}
			}

			return (Map<ArmorModel, LatexHumanoidArmorModel<CustomLatexEntity, ?>>) Map.copyOf(found);
		}

		@Override
		public void prepareAndSetupModels(CustomLatexEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
			Stream.of(EquipmentSlot.values()).filter(slot -> slot.getType() == EquipmentSlot.Type.ARMOR)
					.map(slot -> this.getModelSetForSlot(entity, slot))
					.collect(Collectors.toSet())
					.forEach(set -> {
						final var inner = set.get(ArmorModel.ARMOR_INNER);
						inner.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
						inner.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

						final var outer = set.get(ArmorModel.ARMOR_OUTER);
						outer.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
						outer.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
					});
		}
	}

	public CustomLatexRenderer(EntityRendererProvider.Context context) {
		super(context, new CustomLatexModel(context.bakeLayer(CustomLatexModel.LAYER_LOCATION)),
				new CustomArmorPicker(context.getModelSet()), 0.5f);
		this.addLayer(new LatexParticlesLayer<>(this, getModel()));
		this.addLayer(TransfurCapeLayer.normalCape(this, context.getModelSet()));
		this.addLayer(GasMaskLayer.forSnouted(this, context.getModelSet()));
	}

	@Override
	public ResourceLocation getTextureLocation(CustomLatexEntity p_114482_) {
		return Changed.modResource("textures/custom_latex.png");
	}
}