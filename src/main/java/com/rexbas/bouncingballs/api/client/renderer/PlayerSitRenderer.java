package com.rexbas.bouncingballs.api.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerSitRenderer extends SitRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	
	// Based on PlayerRenderer
	public PlayerSitRenderer(LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> livingRenderer, AbstractClientPlayer entity) {
		super(livingRenderer, entity);
		for (RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> layerrenderer : livingRenderer.layers) {
			if (layerrenderer instanceof PlayerItemInHandLayer) {
				// Replace the PlayerItemInHandLayer with the BouncingBallPlayerItemInHandLayer
				this.layers.add(new BouncingBallPlayerItemInHandLayer<>(this, ((PlayerItemInHandLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) layerrenderer).itemInHandRenderer));
			}
		}
	}

	@Override
	public void render(AbstractClientPlayer entity, float unknownUnused, float partialRenderTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
		this.setModelProperties(entity);
		super.render(entity, unknownUnused, partialRenderTick, poseStack, buffers, light);
	}
	
	private void setModelProperties(AbstractClientPlayer entity) {
		PlayerModel<AbstractClientPlayer> playermodel = this.getModel();
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
			HumanoidModel.ArmPose bipedmodel$armpose = PlayerRenderer.getArmPose(entity, InteractionHand.MAIN_HAND);
			HumanoidModel.ArmPose bipedmodel$armpose1 = PlayerRenderer.getArmPose(entity, InteractionHand.OFF_HAND);
			if (bipedmodel$armpose.isTwoHanded()) {
				bipedmodel$armpose1 = entity.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
			}

			if (entity.getMainArm() == HumanoidArm.RIGHT) {
				playermodel.rightArmPose = bipedmodel$armpose;
				playermodel.leftArmPose = bipedmodel$armpose1;
			} else {
				playermodel.rightArmPose = bipedmodel$armpose1;
				playermodel.leftArmPose = bipedmodel$armpose;
			}
		}
	}
	
	@Override
	protected void scale(AbstractClientPlayer entity, PoseStack poseStack, float partialRenderTick) {
		poseStack.scale(0.9375F, 0.9375F, 0.9375F);
	}

	@Override
	protected void renderNameTag(AbstractClientPlayer entity, Component textComponent, PoseStack poseStack, MultiBufferSource buffers, int light) {
		double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
		poseStack.pushPose();
		if (d0 < 100.0D) {
			Scoreboard scoreboard = entity.getScoreboard();
			Objective objective = scoreboard.getDisplayObjective(2);
			if (objective != null) {
				Score score = scoreboard.getOrCreatePlayerScore(entity.getScoreboardName(), objective);
				super.renderNameTag(entity, (Component.literal(Integer.toString(score.getScore()))).append(" ").append(objective.getDisplayName()), poseStack, buffers, light);
				poseStack.translate(0.0D, (double) (9.0F * 1.15F * 0.025F), 0.0D);
			}
		}

		super.renderNameTag(entity, textComponent, poseStack, buffers, light);
		poseStack.popPose();
	}

	@Override
	protected void setupRotations(AbstractClientPlayer entity, PoseStack poseStack, float p_117804_, float p_117805_, float p_117806_) {
		float f = entity.getSwimAmount(p_117806_);
		if (entity.isFallFlying()) {
			super.setupRotations(entity, poseStack, p_117804_, p_117805_, p_117806_);
			float f1 = (float) entity.getFallFlyingTicks() + p_117806_;
			float f2 = Mth.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
			if (!entity.isAutoSpinAttack()) {
				poseStack.mulPose(Vector3f.XP.rotationDegrees(f2 * (-90.0F - entity.getXRot())));
			}

			Vec3 vec3 = entity.getViewVector(p_117806_);
			Vec3 vec31 = entity.getDeltaMovement();
			double d0 = vec31.horizontalDistanceSqr();
			double d1 = vec3.horizontalDistanceSqr();
			if (d0 > 0.0D && d1 > 0.0D) {
				double d2 = (vec31.x * vec3.x + vec31.z * vec3.z) / Math.sqrt(d0 * d1);
				double d3 = vec31.x * vec3.z - vec31.z * vec3.x;
				poseStack.mulPose(Vector3f.YP.rotation((float) (Math.signum(d3) * Math.acos(d2))));
			}
		} else if (f > 0.0F) {
			super.setupRotations(entity, poseStack, p_117804_, p_117805_, p_117806_);
			float f3 = entity.isInWater() ? -90.0F - entity.getXRot() : -90.0F;
			float f4 = Mth.lerp(f, 0.0F, f3);
			poseStack.mulPose(Vector3f.XP.rotationDegrees(f4));
			if (entity.isVisuallySwimming()) {
				poseStack.translate(0.0D, -1.0D, (double) 0.3F);
			}
		} else {
			super.setupRotations(entity, poseStack, p_117804_, p_117805_, p_117806_);
		}
	}
}