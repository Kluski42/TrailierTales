package net.frozenblock.trailiertales.block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.EnumSet;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.trailiertales.block.entity.ClayDecoratedPotBlockEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.minecraft.world.level.block.entity.PotDecorations;

@Environment(EnvType.CLIENT)
public class ClayDecoratedPotRenderer implements BlockEntityRenderer<ClayDecoratedPotBlockEntity> {
	private static final String NECK = "neck";
	private static final String FRONT = "front";
	private static final String BACK = "back";
	private static final String LEFT = "left";
	private static final String RIGHT = "right";
	private static final String TOP = "top";
	private static final String BOTTOM = "bottom";
	private final ModelPart neck;
	private final ModelPart frontSide;
	private final ModelPart backSide;
	private final ModelPart leftSide;
	private final ModelPart rightSide;
	private final ModelPart top;
	private final ModelPart bottom;

	public ClayDecoratedPotRenderer(BlockEntityRendererProvider.Context context) {
		ModelPart modelPart = context.bakeLayer(ModelLayers.DECORATED_POT_BASE);
		this.neck = modelPart.getChild(NECK);
		this.top = modelPart.getChild(TOP);
		this.bottom = modelPart.getChild(BOTTOM);
		ModelPart modelPart2 = context.bakeLayer(ModelLayers.DECORATED_POT_SIDES);
		this.frontSide = modelPart2.getChild(FRONT);
		this.backSide = modelPart2.getChild(BACK);
		this.leftSide = modelPart2.getChild(LEFT);
		this.rightSide = modelPart2.getChild(RIGHT);
	}

	public static LayerDefinition createBaseLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		CubeDeformation cubeDeformation = new CubeDeformation(0.2F);
		CubeDeformation cubeDeformation2 = new CubeDeformation(-0.1F);
		partDefinition.addOrReplaceChild(
			NECK,
			CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(4.0F, 17.0F, 4.0F, 8.0F, 3.0F, 8.0F, cubeDeformation2)
				.texOffs(0, 5)
				.addBox(5.0F, 20.0F, 5.0F, 6.0F, 1.0F, 6.0F, cubeDeformation),
			PartPose.offsetAndRotation(0.0F, 37.0F, 16.0F, (float) Math.PI, 0.0F, 0.0F)
		);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(-14, 13).addBox(0.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F);
		partDefinition.addOrReplaceChild(TOP, cubeListBuilder, PartPose.offsetAndRotation(1.0F, 16.0F, 1.0F, 0.0F, 0.0F, 0.0F));
		partDefinition.addOrReplaceChild(BOTTOM, cubeListBuilder, PartPose.offsetAndRotation(1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F));
		return LayerDefinition.create(meshDefinition, 32, 32);
	}

	public static LayerDefinition createSidesLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(1, 0).addBox(0.0F, 0.0F, 0.0F, 14.0F, 16.0F, 0.0F, EnumSet.of(Direction.NORTH));
		partDefinition.addOrReplaceChild(BACK, cubeListBuilder, PartPose.offsetAndRotation(15.0F, 16.0F, 1.0F, 0.0F, 0.0F, (float) Math.PI));
		partDefinition.addOrReplaceChild(LEFT, cubeListBuilder, PartPose.offsetAndRotation(1.0F, 16.0F, 1.0F, 0.0F, (float) (-Math.PI / 2), (float) Math.PI));
		partDefinition.addOrReplaceChild(RIGHT, cubeListBuilder, PartPose.offsetAndRotation(15.0F, 16.0F, 15.0F, 0.0F, (float) (Math.PI / 2), (float) Math.PI));
		partDefinition.addOrReplaceChild(FRONT, cubeListBuilder, PartPose.offsetAndRotation(1.0F, 16.0F, 15.0F, (float) Math.PI, 0.0F, 0.0F));
		return LayerDefinition.create(meshDefinition, 16, 16);
	}

	private static Material getSideMaterial(Optional<Item> optional) {
		if (optional.isPresent()) {
			Material material = Sheets.getDecoratedPotMaterial(DecoratedPotPatterns.getPatternFromItem((Item) optional.get()));
			if (material != null) {
				return material;
			}
		}

		return Sheets.DECORATED_POT_SIDE;
	}

	public void render(ClayDecoratedPotBlockEntity clayDecoratedPotBlockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
		matrices.pushPose();
		Direction direction = clayDecoratedPotBlockEntity.getDirection();
		matrices.translate(0.5, 0.0, 0.5);
		matrices.mulPose(Axis.YP.rotationDegrees(180.0F - direction.toYRot()));
		matrices.translate(-0.5, 0.0, -0.5);

		VertexConsumer vertexConsumer = Sheets.DECORATED_POT_BASE.buffer(vertexConsumers, RenderType::entitySolid);
		this.neck.render(matrices, vertexConsumer, light, overlay);
		this.top.render(matrices, vertexConsumer, light, overlay);
		this.bottom.render(matrices, vertexConsumer, light, overlay);
		PotDecorations potDecorations = clayDecoratedPotBlockEntity.getDecorations();
		this.renderSide(this.frontSide, matrices, vertexConsumers, light, overlay, getSideMaterial(potDecorations.front()));
		this.renderSide(this.backSide, matrices, vertexConsumers, light, overlay, getSideMaterial(potDecorations.back()));
		this.renderSide(this.leftSide, matrices, vertexConsumers, light, overlay, getSideMaterial(potDecorations.left()));
		this.renderSide(this.rightSide, matrices, vertexConsumers, light, overlay, getSideMaterial(potDecorations.right()));
		matrices.popPose();
	}

	private void renderSide(ModelPart part, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, Material material) {
		part.render(matrices, material.buffer(vertexConsumers, RenderType::entitySolid), light, overlay);
	}
}
