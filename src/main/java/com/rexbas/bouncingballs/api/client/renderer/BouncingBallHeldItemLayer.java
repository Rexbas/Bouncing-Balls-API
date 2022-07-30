package com.rexbas.bouncingballs.api.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.rexbas.bouncingballs.api.item.IBouncingBall;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.IHasArm;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BouncingBallHeldItemLayer<T extends LivingEntity, M extends EntityModel<T> & IHasArm> extends HeldItemLayer<T, M> {

	public BouncingBallHeldItemLayer(IEntityRenderer<T, M> renderer) {
		super(renderer);
	}

	@Override
	public void render(MatrixStack matrixStack, IRenderTypeBuffer buffers, int light, T entity, float p_225628_5_, float p_225628_6_, float partialRenderTick, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
		ItemStack ballStack = null;
		if (entity.getOffhandItem().getItem() instanceof IBouncingBall) {
			ballStack = entity.getOffhandItem();
		}
		else if (entity.getMainHandItem().getItem() instanceof IBouncingBall) {
			ballStack = entity.getMainHandItem();
		}
		
		boolean flag = entity.getMainArm() == HandSide.RIGHT;
		ItemStack stackLeft = flag ? entity.getOffhandItem() : entity.getMainHandItem();
		ItemStack stackRight = flag ? entity.getMainHandItem() : entity.getOffhandItem();
		if (!stackLeft.isEmpty() || !stackRight.isEmpty()) {
			matrixStack.pushPose();
			if (this.getParentModel().young) {
				matrixStack.translate(0.0D, 0.75D, 0.0D);
				matrixStack.scale(0.5F, 0.5F, 0.5F);
			}
			
			if (ballStack != null && ballStack.equals(stackRight) && ((IBouncingBall) ballStack.getItem()).shouldSitOnBall(entity)) {
				this.renderBouncingBall(entity, stackRight, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, matrixStack, buffers, light);
			}
			else {
				this.renderArmWithItem(entity, stackRight, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, matrixStack, buffers, light);
			}
			
			if (ballStack != null && ballStack.equals(stackLeft) && ((IBouncingBall) ballStack.getItem()).shouldSitOnBall(entity)) {
				this.renderBouncingBall(entity, stackLeft, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, matrixStack, buffers, light);
			}
			else {
				this.renderArmWithItem(entity, stackLeft, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, matrixStack, buffers, light);
			}
			matrixStack.popPose();
		}
	}

	protected void renderBouncingBall(LivingEntity entity, ItemStack stack, ItemCameraTransforms.TransformType transformType, HandSide side, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light) {
		if (!stack.isEmpty()) {
			matrixStack.pushPose();
			matrixStack.scale(2, 2, 2);
			matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
			matrixStack.mulPose(Vector3f.YP.rotationDegrees(0.0F));
			float rotZ = 135;
			float transX = -0.044f;
			if (side == HandSide.LEFT) {
				rotZ += 90;
				transX *= -1;
			}
			matrixStack.translate(transX, -0.25, -0.32);
			matrixStack.mulPose(Vector3f.ZP.rotationDegrees(rotZ));

			boolean flag = side == HandSide.LEFT;
			Minecraft.getInstance().getItemInHandRenderer().renderItem(entity, stack, transformType, flag, matrixStack, buffers, light);
			matrixStack.popPose();
		}
	}
}