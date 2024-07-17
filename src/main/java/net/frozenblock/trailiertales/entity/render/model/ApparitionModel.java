package net.frozenblock.trailiertales.entity.render.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.lib.entity.api.rendering.FrozenRenderType;
import net.frozenblock.lib.entity.impl.client.rendering.ModelPartInvertInterface;
import net.frozenblock.trailiertales.entity.Apparition;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class ApparitionModel<T extends Apparition> extends HierarchicalModel<T> {
	private final ModelPart root;
	public final ModelPart core;
	public final ModelPart inner;
	public final ModelPart outline;
	public final ModelPart outer;
	private final AlphaFunction<T> coreAlphaFunction;
	private final AlphaFunction<T> outerAlphaFunction;
	private final DrawSelector<T, ApparitionModel<T>> drawSelector;
	private final List<ModelPart> modelParts;
	private final List<ModelPart> coreParts;
	private final List<ModelPart> outerParts;

	private float coreTransparency;
	private float outerTransparency;
	private float flicker;

	public ApparitionModel(@NotNull ModelPart root) {
		this(
			FrozenRenderType::apparitionOuterCull,
			root,
			Apparition::getTransparency,
			Apparition::getOuterTransparency,
			ApparitionModel::getParts
		);
	}

	public ApparitionModel(
		Function<ResourceLocation, RenderType> function,
		@NotNull ModelPart root,
		AlphaFunction<T> coreAlpha,
		AlphaFunction<T> outerAlpha,
		DrawSelector<T, ApparitionModel<T>> drawSelector
	) {
		super(function);
		this.root = root;
		this.core = root.getChild("core");
		this.inner = this.core.getChild("inner");
		this.outline = this.core.getChild("outline");
		this.outer = root.getChild("outer");

		ModelPartInvertInterface.class.cast(this.outline).frozenLib$setInverted(true);

		this.coreAlphaFunction = coreAlpha;
		this.outerAlphaFunction = outerAlpha;
		this.drawSelector = drawSelector;
		this.modelParts = ImmutableList.of(this.core, this.inner, this.outline, this.outer);
		this.coreParts = ImmutableList.of(this.core, this.inner, this.outline);
		this.outerParts = ImmutableList.of(this.outer);
	}

	public List<ModelPart> getParts() {
		return this.modelParts;
	}

	public List<ModelPart> getCoreParts() {
		return this.coreParts;
	}

	public List<ModelPart> getOuterParts() {
		return this.outerParts;
	}

	@NotNull
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();

		PartDefinition core = meshdefinition.getRoot().addOrReplaceChild("core", CubeListBuilder.create(), PartPose.offset(0F, 17F, 0F));

		core.addOrReplaceChild("inner", CubeListBuilder.create()
			.texOffs(0, 28).addBox(-5F, -5F, -5F, 10F, 10F, 10F), PartPose.ZERO);
		core.addOrReplaceChild("outline", CubeListBuilder.create()
				.texOffs(0, 48).addBox(-5.5F, -5.5F, -5.5F, 11F, 11F, 11F), PartPose.ZERO);

		meshdefinition.getRoot().addOrReplaceChild("outer", CubeListBuilder.create()
			.texOffs(0, 0).addBox(-7F, -7F, -7F, 14F, 14F, 14F),  PartPose.offset(0F, 17F, 0F));

		return LayerDefinition.create(meshdefinition, 80, 80);
	}

	@Override
	@NotNull
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		animationProgress = animationProgress + (limbAngle * 3.5F);
		this.core.yRot = headYaw * ((float) Math.PI / 180F);
		this.core.xRot = headPitch * ((float) Math.PI / 180F);

		float tighten = 1F - limbDistance * 0.75F;
		this.outer.yRot *= tighten;
		this.outer.zRot *= tighten;

		//SQUASH & STRETCH
		float sinIdle = (float) (Math.sin(animationProgress * 0.3F) * 0.1F) * tighten;
		float squash = sinIdle + 1F;

		this.outer.xScale = squash;
		this.outer.zScale = squash;
		this.outer.yScale = -sinIdle + 1;
	}

	@Override
	public void prepareMobModel(@NotNull T entity, float limbAngle, float limbDistance, float tickDelta) {
		this.coreTransparency = this.coreAlphaFunction.apply(entity, tickDelta);
		this.outerTransparency = this.outerAlphaFunction.apply(entity, tickDelta);
		this.flicker = entity.getFlicker(tickDelta);
		this.outer.yRot = entity.getItemYRot(tickDelta);
		this.outer.zRot = entity.getItemZRot(tickDelta);
	}

	@Override
	public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer buffer, int packedLight, int packedOverlay, int colorBad) {
		poseStack.pushPose();
		this.onlyDrawSelectedParts();
		int coreTransparency = FastColor.ARGB32.colorFromFloat(this.coreTransparency * this.flicker, 1F, 1F, 1F);
		if (coreTransparency != 0) {
			this.core.render(poseStack, buffer, 15728640, packedOverlay, coreTransparency);
		}
		int outerTransparency = FastColor.ARGB32.colorFromFloat(this.outerTransparency * this.flicker, 1F, 1F, 1F);
		if (outerTransparency != 0) {
			this.outer.render(poseStack, buffer, 15728640, packedOverlay, outerTransparency);
		}
		this.resetDrawForAllParts();
		poseStack.popPose();
	}

	private void onlyDrawSelectedParts() {
		List<ModelPart> list = this.drawSelector.getPartsToDraw(this);
		this.root().getAllParts().forEach(modelPart -> modelPart.skipDraw = true);
		list.forEach(modelPart -> modelPart.skipDraw = false);
	}

	private void resetDrawForAllParts() {
		this.root().getAllParts().forEach(modelPart -> modelPart.skipDraw = false);
	}


	@Environment(EnvType.CLIENT)
	public interface AlphaFunction<T extends Apparition> {
		float apply(T apparition, float tickDelta);
	}

	@Environment(EnvType.CLIENT)
	public interface DrawSelector<T extends Apparition, M extends EntityModel<T>> {
		List<ModelPart> getPartsToDraw(M entityModel);
	}
}
