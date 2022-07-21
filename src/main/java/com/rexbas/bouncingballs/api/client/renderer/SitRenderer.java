package com.rexbas.bouncingballs.api.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.rexbas.bouncingballs.api.item.BouncingBall;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.IHasArm;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;

/**
 * To render an entity in the sitting position with a bouncing ball it is needed to replace the rendering as long as {@link BouncingBall#shouldSitOnBall} returns true.
 * To do that the entity renderer needs to be replaced by a renderer that somewhere extends {@link SitRenderer}.
 * {@link SitRenderer} is the same as {@link LivingRenderer} but forces the entity in a sitting position.
 * If an entity already has a renderer that (indirectly or directly) extends {@link LivingRenderer} then all the renderer classes have to be replaced until and including the class that extends {@link LivingRenderer}.
 * The last class (the one that extends {@link LivingRenderer}) will then instead extend from {@link SitRenderer}.
 * If any child classes do not override the methods that are used in {@link SitRenderer#render} then it might not be necessary.
 * A {@link SitRenderer} can be rendered by canceling the {@link RenderLivingEvent.Pre} and rendering the renderer that extends {@link SitRenderer} when {@link BouncingBall#shouldSitOnBall} returns true.
 */
@OnlyIn(Dist.CLIENT)
public class SitRenderer<T extends LivingEntity, M extends EntityModel<T> & IHasArm> extends LivingRenderer<T, M> {
	
	private final ResourceLocation TEXTURE;

	/**
	 * A new {@link LivingRenderer} that forces the entity in a sitting position.
	 * Will use the information from the {@link LivingRenderer} like the layers.
	 * 
	 * @param livingRenderer	The default renderer.
	 * @param entity			The entity that is being rendered.
	 */
	public SitRenderer(LivingRenderer<T, M> livingRenderer, T entity) {
		super(livingRenderer.getDispatcher(), livingRenderer.getModel(), livingRenderer.shadowRadius);
		this.TEXTURE = livingRenderer.getTextureLocation(entity);
		
		for (LayerRenderer<T, M> layerrenderer : livingRenderer.layers) {
			if (layerrenderer instanceof HeldItemLayer) {
				// Replace the HeldItemLayer with the BouncingBallHeldItemLayer
				this.layers.add(new BouncingBallHeldItemLayer<>(this));
			} else {
				this.layers.add(layerrenderer);
			}
		}
	}

	// Based on LivingRenderer
	@Override
	public void render(T entity, float unknownUnused, float partialRenderTick, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light) {
		matrixStack.pushPose();		
		this.getModel().attackTime = this.getAttackAnim(entity, partialRenderTick);

		boolean shouldSit = true;
		this.getModel().riding = shouldSit;
		this.getModel().young = entity.isBaby();
		float f = MathHelper.rotLerp(partialRenderTick, entity.yBodyRotO, entity.yBodyRot);
		float f1 = MathHelper.rotLerp(partialRenderTick, entity.yHeadRotO, entity.yHeadRot);
		float f2 = f1 - f;
		if (shouldSit && entity.getVehicle() instanceof LivingEntity) {
			LivingEntity livingentity = (LivingEntity) entity.getVehicle();
			f = MathHelper.rotLerp(partialRenderTick, livingentity.yBodyRotO, livingentity.yBodyRot);
			f2 = f1 - f;
			float f3 = MathHelper.wrapDegrees(f2);
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

		float f6 = MathHelper.lerp(partialRenderTick, entity.xRotO, entity.xRot);
		if (entity.getPose() == Pose.SLEEPING) {
			Direction direction = entity.getBedOrientation();
			if (direction != null) {
				float f4 = entity.getEyeHeight(Pose.STANDING) - 0.1F;
				matrixStack.translate((double) ((float) (-direction.getStepX()) * f4), 0.0D, (double) ((float) (-direction.getStepZ()) * f4));
			}
		}

		float f7 = this.getBob(entity, partialRenderTick);
		this.setupRotations(entity, matrixStack, f7, f, partialRenderTick);
		matrixStack.scale(-1.0F, -1.0F, 1.0F);
		this.scale(entity, matrixStack, partialRenderTick);
		matrixStack.translate(0.0D, (double) -1.501F, 0.0D);
		float f8 = 0.0F;
		float f5 = 0.0F;
		if (!shouldSit && entity.isAlive()) {
			f8 = MathHelper.lerp(partialRenderTick, entity.animationSpeedOld, entity.animationSpeed);
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
			IVertexBuilder ivertexbuilder = buffers.getBuffer(rendertype);
			int i = LivingRenderer.getOverlayCoords(entity, this.getWhiteOverlayProgress(entity, partialRenderTick));
			this.getModel().renderToBuffer(matrixStack, ivertexbuilder, light, i, 1.0F, 1.0F, 1.0F, flag1 ? 0.15F : 1.0F);
		}

		if (!entity.isSpectator()) {
			for (LayerRenderer<T, M> layerrenderer : this.layers) {
				layerrenderer.render(matrixStack, buffers, light, entity, f5, f8, partialRenderTick, f7, f2, f6);	
			}
		}

		matrixStack.popPose();

		// From EntityRenderer
		net.minecraftforge.client.event.RenderNameplateEvent renderNameplateEvent = new net.minecraftforge.client.event.RenderNameplateEvent(entity, entity.getDisplayName(), this, matrixStack, buffers, light, partialRenderTick);
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(renderNameplateEvent);
		if (renderNameplateEvent.getResult() != net.minecraftforge.eventbus.api.Event.Result.DENY && (renderNameplateEvent.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW || this.shouldShowName(entity))) {
			this.renderNameTag(entity, renderNameplateEvent.getContent(), matrixStack, buffers, light);
		}
		
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post<T, M>(entity, this, partialRenderTick, matrixStack, buffers, light));
	}
	
	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return this.TEXTURE;
	}
}