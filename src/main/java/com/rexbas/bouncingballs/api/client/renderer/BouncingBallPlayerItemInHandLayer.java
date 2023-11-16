package com.rexbas.bouncingballs.api.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BouncingBallPlayerItemInHandLayer<T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> extends BouncingBallItemInHandLayer<T, M> {

	public BouncingBallPlayerItemInHandLayer(RenderLayerParent<T, M> layer, ItemInHandRenderer renderer) {
		super(layer, renderer);
	}
	
	@Override
	protected void renderArmWithItem(LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext, HumanoidArm side, PoseStack poseStack, MultiBufferSource buffers, int light) {
		if (stack.is(Items.SPYGLASS) && entity.getUseItem() == stack && entity.swingTime == 0) {
			this.renderArmWithSpyglass(entity, stack, side, poseStack, buffers, light);
		} else {
			super.renderArmWithItem(entity, stack, displayContext, side, poseStack, buffers, light);
		}
	}
	
	private void renderArmWithSpyglass(LivingEntity entity, ItemStack stack, HumanoidArm side, PoseStack poseStack, MultiBufferSource buffers, int light) {
		poseStack.pushPose();
		ModelPart modelpart = this.getParentModel().getHead();
		float f = modelpart.xRot;
		modelpart.xRot = Mth.clamp(modelpart.xRot, (-(float) Math.PI / 6F), ((float) Math.PI / 2F));
		modelpart.translateAndRotate(poseStack);
		modelpart.xRot = f;
		CustomHeadLayer.translateToHead(poseStack, false);
		boolean flag = side == HumanoidArm.LEFT;
		poseStack.translate((double) ((flag ? -2.5F : 2.5F) / 16.0F), -0.0625D, 0.0D);
		this.itemInHandRenderer.renderItem(entity, stack, ItemDisplayContext.HEAD, false, poseStack, buffers, light);
		poseStack.popPose();
	}
}