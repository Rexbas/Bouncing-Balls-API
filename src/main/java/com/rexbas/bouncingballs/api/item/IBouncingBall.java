package com.rexbas.bouncingballs.api.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface IBouncingBall {
	public boolean canBounce(LivingEntity entity);
	public boolean shouldSitOnBall(LivingEntity entity);
	public void bounce(LivingEntity entity, float motionY);
	public float onFall(LivingEntity entity, ItemStack stack, float fallDistance);
	// TODO on damage
}