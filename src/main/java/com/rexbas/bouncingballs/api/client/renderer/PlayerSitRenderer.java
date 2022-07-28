package com.rexbas.bouncingballs.api.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerSitRenderer extends SitRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {
	
	// Based on PlayerRenderer
	public PlayerSitRenderer(LivingRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> livingRenderer, AbstractClientPlayerEntity entity) {
		super(livingRenderer, entity);
	}
	
	@Override
	public void render(AbstractClientPlayerEntity entity, float unknownUnused, float partialRenderTick, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light) {
		this.setModelProperties(entity);
		super.render(entity, unknownUnused, partialRenderTick, matrixStack, buffers, light);
	}
	
	private void setModelProperties(AbstractClientPlayerEntity entity) {
		PlayerModel<AbstractClientPlayerEntity> playermodel = this.getModel();
		if (entity.isSpectator()) {
			playermodel.setAllVisible(false);
			playermodel.head.visible = true;
			playermodel.hat.visible = true;
		} else {
			playermodel.setAllVisible(true);
			playermodel.hat.visible = entity.isModelPartShown(PlayerModelPart.HAT);
			playermodel.jacket.visible = entity.isModelPartShown(PlayerModelPart.JACKET);
			playermodel.leftPants.visible = entity.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
			playermodel.rightPants.visible = entity.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
			playermodel.leftSleeve.visible = entity.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
			playermodel.rightSleeve.visible = entity.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
			playermodel.crouching = entity.isCrouching();
			BipedModel.ArmPose bipedmodel$armpose = PlayerRenderer.getArmPose(entity, Hand.MAIN_HAND);
			BipedModel.ArmPose bipedmodel$armpose1 = PlayerRenderer.getArmPose(entity, Hand.OFF_HAND);
			if (bipedmodel$armpose.isTwoHanded()) {
				bipedmodel$armpose1 = entity.getOffhandItem().isEmpty() ? BipedModel.ArmPose.EMPTY : BipedModel.ArmPose.ITEM;
			}

			if (entity.getMainArm() == HandSide.RIGHT) {
				playermodel.rightArmPose = bipedmodel$armpose;
				playermodel.leftArmPose = bipedmodel$armpose1;
			} else {
				playermodel.rightArmPose = bipedmodel$armpose1;
				playermodel.leftArmPose = bipedmodel$armpose;
			}
		}
	}
	
	@Override
	protected void scale(AbstractClientPlayerEntity entity, MatrixStack matrixStack, float partialRenderTick) {
		matrixStack.scale(0.9375F, 0.9375F, 0.9375F);
	}

	@Override
	protected void renderNameTag(AbstractClientPlayerEntity entity, ITextComponent textComponent, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light) {
		double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
		matrixStack.pushPose();
		if (d0 < 100.0D) {
			Scoreboard scoreboard = entity.getScoreboard();
			ScoreObjective scoreobjective = scoreboard.getDisplayObjective(2);
			if (scoreobjective != null) {
				Score score = scoreboard.getOrCreatePlayerScore(entity.getScoreboardName(), scoreobjective);
				super.renderNameTag(entity, (new StringTextComponent(Integer.toString(score.getScore()))).append(" ").append(scoreobjective.getDisplayName()), matrixStack, buffers, light);
				matrixStack.translate(0.0D, (double) (9.0F * 1.15F * 0.025F), 0.0D);
			}
		}

		super.renderNameTag(entity, textComponent, matrixStack, buffers, light);
		matrixStack.popPose();
	}

	@Override
	protected void setupRotations(AbstractClientPlayerEntity entity, MatrixStack matrixStack, float p_225621_3_, float p_225621_4_, float p_225621_5_) {
		float f = entity.getSwimAmount(p_225621_5_);
		if (entity.isFallFlying()) {
			super.setupRotations(entity, matrixStack, p_225621_3_, p_225621_4_, p_225621_5_);
			float f1 = (float) entity.getFallFlyingTicks() + p_225621_5_;
			float f2 = MathHelper.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
			if (!entity.isAutoSpinAttack()) {
				matrixStack.mulPose(Vector3f.XP.rotationDegrees(f2 * (-90.0F - entity.xRot)));
			}

			Vector3d vector3d = entity.getViewVector(p_225621_5_);
			Vector3d vector3d1 = entity.getDeltaMovement();
			double d0 = Entity.getHorizontalDistanceSqr(vector3d1);
			double d1 = Entity.getHorizontalDistanceSqr(vector3d);
			if (d0 > 0.0D && d1 > 0.0D) {
				double d2 = (vector3d1.x * vector3d.x + vector3d1.z * vector3d.z) / Math.sqrt(d0 * d1);
				double d3 = vector3d1.x * vector3d.z - vector3d1.z * vector3d.x;
				matrixStack.mulPose(Vector3f.YP.rotation((float) (Math.signum(d3) * Math.acos(d2))));
			}
		} else if (f > 0.0F) {
			super.setupRotations(entity, matrixStack, p_225621_3_, p_225621_4_, p_225621_5_);
			float f3 = entity.isInWater() ? -90.0F - entity.xRot : -90.0F;
			float f4 = MathHelper.lerp(f, 0.0F, f3);
			matrixStack.mulPose(Vector3f.XP.rotationDegrees(f4));
			if (entity.isVisuallySwimming()) {
				matrixStack.translate(0.0D, -1.0D, (double) 0.3F);
			}
		} else {
			super.setupRotations(entity, matrixStack, p_225621_3_, p_225621_4_, p_225621_5_);
		}
	}
}