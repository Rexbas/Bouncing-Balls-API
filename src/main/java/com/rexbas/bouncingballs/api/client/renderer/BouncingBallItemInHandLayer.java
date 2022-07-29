package com.rexbas.bouncingballs.api.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.rexbas.bouncingballs.api.item.IBouncingBall;

import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BouncingBallItemInHandLayer<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends ItemInHandLayer<T, M> {

	public BouncingBallItemInHandLayer(RenderLayerParent<T, M> layer, ItemInHandRenderer renderer) {
		super(layer, renderer);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffers, int light, T entity, float p_225628_5_, float p_225628_6_, float partialRenderTick, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
		ItemStack ballStack = null;
		if (entity.getOffhandItem().getItem() instanceof IBouncingBall) {
			ballStack = entity.getOffhandItem();
		}
		else if (entity.getMainHandItem().getItem() instanceof IBouncingBall) {
			ballStack = entity.getMainHandItem();
		}
		
		boolean flag = entity.getMainArm() == HumanoidArm.RIGHT;
		ItemStack stackLeft = flag ? entity.getOffhandItem() : entity.getMainHandItem();
		ItemStack stackRight = flag ? entity.getMainHandItem() : entity.getOffhandItem();
		if (!stackLeft.isEmpty() || !stackRight.isEmpty()) {
			poseStack.pushPose();
			if (this.getParentModel().young) {
				poseStack.translate(0.0D, 0.75D, 0.0D);
				poseStack.scale(0.5F, 0.5F, 0.5F);
			}
			
			if (ballStack.equals(stackRight) && ((IBouncingBall) ballStack.getItem()).shouldSitOnBall(entity)) {
				this.renderBouncingBall(entity, stackRight, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, poseStack, buffers, light);
			}
			else {
				this.renderArmWithItem(entity, stackRight, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, poseStack, buffers, light);
			}
			
			if (ballStack.equals(stackLeft) && ((IBouncingBall) ballStack.getItem()).shouldSitOnBall(entity)) {
				this.renderBouncingBall(entity, stackLeft, ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, poseStack, buffers, light);
			}
			else {
				this.renderArmWithItem(entity, stackLeft, ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, poseStack, buffers, light);
			}
			poseStack.popPose();
		}
	}

	protected void renderBouncingBall(LivingEntity entity, ItemStack stack, ItemTransforms.TransformType transformType, HumanoidArm side, PoseStack poseStack, MultiBufferSource buffers, int light) {
		if (!stack.isEmpty()) {
			poseStack.pushPose();
			poseStack.scale(2, 2, 2);
			poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
			poseStack.mulPose(Vector3f.YP.rotationDegrees(0.0F));
			float rotZ = 135;
			float transX = -0.044f;
			if (side == HumanoidArm.LEFT) {
				rotZ += 90;
				transX *= -1;
			}
			poseStack.translate(transX, -0.25, -0.32);
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(rotZ));

			boolean flag = side == HumanoidArm.LEFT;
			this.itemInHandRenderer.renderItem(entity, stack, transformType, flag, poseStack, buffers, light);
			poseStack.popPose();
		}
	}
}