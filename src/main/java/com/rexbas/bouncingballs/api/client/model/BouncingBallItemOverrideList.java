package com.rexbas.bouncingballs.api.client.model;

import javax.annotation.Nullable;

import com.rexbas.bouncingballs.api.item.IBouncingBall;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class BouncingBallItemOverrideList extends ItemOverrideList {
	
	private IBakedModel activeModel;
	
	public BouncingBallItemOverrideList(ModelResourceLocation activeModelLocation) {
		this.activeModel = Minecraft.getInstance().getModelManager().getModel(activeModelLocation);
	}
      
	@Nullable
	@Override
	public IBakedModel resolve(IBakedModel original, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
		if (entity != null) {
    		IBouncingBall ball = null;
    		
    		if (stack.equals(entity.getOffhandItem(), false)) {
    			ball = (IBouncingBall) entity.getOffhandItem().getItem();
    		}
    		else if (stack.equals(entity.getMainHandItem(), false) && !(entity.getOffhandItem().getItem() instanceof IBouncingBall)) {
    			ball = (IBouncingBall) entity.getMainHandItem().getItem();
    		}
			
			if (ball != null && ball.shouldSitOnBall(entity)) {
				return activeModel;
			}
		}		
		return original;
	}
}