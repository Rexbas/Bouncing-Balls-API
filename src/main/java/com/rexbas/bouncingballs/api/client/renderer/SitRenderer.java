package com.rexbas.bouncingballs.api.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SitRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements IEntityRenderer<T, M> {
	
	protected LivingRenderer<T, M> livingRenderer;

	public SitRenderer(LivingRenderer<T, M> livingRenderer) {
		super(livingRenderer.getDispatcher());
		this.livingRenderer = livingRenderer;
	}

	// Based on LivingRenderer
	@Override
	public void render(T entity, float unknownUnused, float partialRenderTick, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light) {
		matrixStack.pushPose();
		this.livingRenderer.getModel().attackTime = this.livingRenderer.getAttackAnim(entity, partialRenderTick);

		boolean shouldSit = true;
		this.livingRenderer.getModel().riding = shouldSit;
		this.livingRenderer.getModel().young = entity.isBaby();
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

		float f7 = this.livingRenderer.getBob(entity, partialRenderTick);
		this.livingRenderer.setupRotations(entity, matrixStack, f7, f, partialRenderTick);
		matrixStack.scale(-1.0F, -1.0F, 1.0F);
		this.livingRenderer.scale(entity, matrixStack, partialRenderTick);
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

		this.livingRenderer.getModel().prepareMobModel(entity, f5, f8, partialRenderTick);
		this.livingRenderer.getModel().setupAnim(entity, f5, f8, f7, f2, f6);
		Minecraft minecraft = Minecraft.getInstance();
		boolean flag = this.livingRenderer.isBodyVisible(entity);
		boolean flag1 = !flag && !entity.isInvisibleTo(minecraft.player);
		boolean flag2 = minecraft.shouldEntityAppearGlowing(entity);
		RenderType rendertype = this.livingRenderer.getRenderType(entity, flag, flag1, flag2);
		if (rendertype != null) {
			IVertexBuilder ivertexbuilder = buffers.getBuffer(rendertype);
			int i = LivingRenderer.getOverlayCoords(entity, this.livingRenderer.getWhiteOverlayProgress(entity, partialRenderTick));
			this.livingRenderer.getModel().renderToBuffer(matrixStack, ivertexbuilder, light, i, 1.0F, 1.0F, 1.0F, flag1 ? 0.15F : 1.0F);
		}

		if (!entity.isSpectator()) {
			for (LayerRenderer<T, M> layerrenderer : this.livingRenderer.layers) {
				layerrenderer.render(matrixStack, buffers, light, entity, f5, f8, partialRenderTick, f7, f2, f6);
			}
		}

		matrixStack.popPose();
		super.render(entity, unknownUnused, partialRenderTick, matrixStack, buffers, light);
	    net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post<T, M>(entity, this.livingRenderer, partialRenderTick, matrixStack, buffers, light));
	}
	
	@Override
	public M getModel() {
		return this.livingRenderer.getModel();
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return this.livingRenderer.getTextureLocation(entity);
	}
}