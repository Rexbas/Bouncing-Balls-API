package com.rexbas.bouncingballs.api.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.rexbas.bouncingballs.api.item.BouncingBall;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;

/**
 * To render an entity in the sitting position with a bouncing ball it is needed to replace the rendering as long as {@link BouncingBall#shouldSitOnBall} returns true.
 * To do that the entity renderer needs to be replaced by a renderer that somewhere extends {@link SitRenderer}.
 * {@link SitRenderer} is the same as {@link LivingEntityRenderer} but forces the entity in a sitting position.
 * If an entity already has a renderer that (indirectly or directly) extends {@link LivingEntityRenderer} then all the renderer classes have to be replaced until and including the class that extends {@link LivingEntityRenderer}.
 * The last class (the one that extends {@link LivingEntityRenderer}) will then instead extend from {@link SitRenderer}.
 * If any child classes do not override the methods that are used in {@link SitRenderer#render} then it might not be necessary.
 * A {@link SitRenderer} can be rendered by canceling the {@link RenderLivingEvent.Pre} and rendering the renderer that extends {@link SitRenderer} when {@link BouncingBall#shouldSitOnBall} returns true.
 */
@OnlyIn(Dist.CLIENT)
public class SitRenderer<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends LivingEntityRenderer<T, M> {
	
	private final ResourceLocation TEXTURE;

	/**
	 * A new {@link LivingEntityRenderer} that forces the entity in a sitting position.
	 * Will use the information from the {@link LivingEntityRenderer} like the layers.
	 * 
	 * @param livingRenderer	The default renderer.
	 * @param entity			The entity that is being rendered.
	 */
	public SitRenderer(LivingEntityRenderer<T, M> livingRenderer, T entity) {
		super(new EntityRendererProvider.Context(livingRenderer.entityRenderDispatcher, null, null, null, null, null, livingRenderer.font), livingRenderer.getModel(), livingRenderer.shadowRadius);
		this.TEXTURE = livingRenderer.getTextureLocation(entity);
		for (RenderLayer<T, M> layerrenderer : livingRenderer.layers) {
			if (layerrenderer instanceof PlayerItemInHandLayer) {
				continue;
			} else if (layerrenderer instanceof ItemInHandLayer) {
				// Replace the ItemInHandLayer with the BouncingBallItemInHandLayer
				this.layers.add(new BouncingBallItemInHandLayer<>(this, ((ItemInHandLayer<T, M>) layerrenderer).itemInHandRenderer));
			} else {
				this.layers.add(layerrenderer);
			}
		}
	}

	// Based on LivingEntityRenderer
	@Override
	public void render(T entity, float unknownUnused, float partialRenderTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
		poseStack.pushPose();		
		this.getModel().attackTime = this.getAttackAnim(entity, partialRenderTick);

		boolean shouldSit = true;
		this.getModel().riding = shouldSit;
		this.getModel().young = entity.isBaby();
		float f = Mth.rotLerp(partialRenderTick, entity.yBodyRotO, entity.yBodyRot);
		float f1 = Mth.rotLerp(partialRenderTick, entity.yHeadRotO, entity.yHeadRot);
		float f2 = f1 - f;
		if (shouldSit && entity.getVehicle() instanceof LivingEntity) {
			LivingEntity livingentity = (LivingEntity) entity.getVehicle();
			f = Mth.rotLerp(partialRenderTick, livingentity.yBodyRotO, livingentity.yBodyRot);
			f2 = f1 - f;
			float f3 = Mth.wrapDegrees(f2);
			if (f3 < -85.0F) {
				f3 = -85.0F;
			}

			if (f3 >= 85.0F) {
				f3 = 85.0F;
			}

			f = f1 - f3;
			if (f3 * f3 > 2500.0F) {
				f += f3 * 0.2F;
			}

			f2 = f1 - f;
		}

		float f6 = Mth.lerp(partialRenderTick, entity.xRotO, entity.getXRot());
		if (entity.getPose() == Pose.SLEEPING) {
			Direction direction = entity.getBedOrientation();
			if (direction != null) {
				float f4 = entity.getEyeHeight(Pose.STANDING) - 0.1F;
				poseStack.translate((double) ((float) (-direction.getStepX()) * f4), 0.0D, (double) ((float) (-direction.getStepZ()) * f4));
			}
		}

		float f7 = this.getBob(entity, partialRenderTick);
		this.setupRotations(entity, poseStack, f7, f, partialRenderTick);
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		this.scale(entity, poseStack, partialRenderTick);
		poseStack.translate(0.0D, (double) -1.501F, 0.0D);
		float f8 = 0.0F;
		float f5 = 0.0F;
		if (!shouldSit && entity.isAlive()) {
			f8 = Mth.lerp(partialRenderTick, entity.animationSpeedOld, entity.animationSpeed);
			f5 = entity.animationPosition - entity.animationSpeed * (1.0F - partialRenderTick);
			if (entity.isBaby()) {
				f5 *= 3.0F;
			}

			if (f8 > 1.0F) {
				f8 = 1.0F;
			}
		}

		this.getModel().prepareMobModel(entity, f5, f8, partialRenderTick);
		this.getModel().setupAnim(entity, f5, f8, f7, f2, f6);
		
		Minecraft minecraft = Minecraft.getInstance();
		boolean flag = this.isBodyVisible(entity);
		boolean flag1 = !flag && !entity.isInvisibleTo(minecraft.player);
		boolean flag2 = minecraft.shouldEntityAppearGlowing(entity);
		RenderType rendertype = this.getRenderType(entity, flag, flag1, flag2);
		if (rendertype != null) {
			VertexConsumer vertexconsumer = buffers.getBuffer(rendertype);
	        int i = getOverlayCoords(entity, this.getWhiteOverlayProgress(entity, partialRenderTick));
			this.getModel().renderToBuffer(poseStack, vertexconsumer, light, i, 1.0F, 1.0F, 1.0F, flag1 ? 0.15F : 1.0F);
		}

		if (!entity.isSpectator()) {
			for (RenderLayer<T, M> layerrenderer : this.layers) {
				layerrenderer.render(poseStack, buffers, light, entity, f5, f8, partialRenderTick, f7, f2, f6);	
			}
		}
		
		poseStack.popPose();
		
		// From EntityRenderer
		var renderNameTagEvent = new net.minecraftforge.client.event.RenderNameTagEvent(entity, entity.getDisplayName(), this, poseStack, buffers, light, partialRenderTick);
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(renderNameTagEvent);
		if (renderNameTagEvent.getResult() != net.minecraftforge.eventbus.api.Event.Result.DENY && (renderNameTagEvent.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW || this.shouldShowName(entity))) {
			this.renderNameTag(entity, renderNameTagEvent.getContent(), poseStack, buffers, light);
		}
		
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post<T, M>(entity, this, partialRenderTick, poseStack, buffers, light));
	}
	
	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return this.TEXTURE;
	}
}