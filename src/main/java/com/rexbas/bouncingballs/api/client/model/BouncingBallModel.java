package com.rexbas.bouncingballs.api.client.model;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;

public class BouncingBallModel implements IBakedModel {
	
	private IBakedModel model;
	private BouncingBallItemOverrideList itemOverrideList;
	
	public BouncingBallModel(IBakedModel model, BouncingBallItemOverrideList itemOverrideList) {
		this.model = model;
		this.itemOverrideList = itemOverrideList;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, Random rand) {
		return this.model.getQuads(state, direction, rand);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return this.model.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return this.model.isGui3d();
	}

	@Override
	public boolean usesBlockLight() {
		return this.model.usesBlockLight();
	}

	@Override
	public boolean isCustomRenderer() {
		return this.model.isCustomRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return this.model.getParticleIcon();
	}

	@Override
	public ItemOverrideList getOverrides() {
		return this.itemOverrideList;
	}
	
	@Override
	public boolean doesHandlePerspectives() {
		return this.model.doesHandlePerspectives();
	}
	
	@Override
    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack mat) {
		return this.model.handlePerspective(cameraTransformType, mat);
    }
}