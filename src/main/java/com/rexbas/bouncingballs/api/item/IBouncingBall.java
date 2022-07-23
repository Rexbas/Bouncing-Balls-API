package com.rexbas.bouncingballs.api.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.util.DamageSource;

public interface IBouncingBall {
	public boolean canBounce(LivingEntity entity);
	public boolean shouldSitOnBall(LivingEntity entity);
	public void bounce(LivingEntity entity, float motionY);
	public float onFall(LivingEntity entity, ItemStack stack, float fallDistance);
	public boolean onDamage(LivingEntity entity, DamageSource damageSource, float amount);
	public void inFluid(LivingEntity entity, ITag<Fluid> fluid);
}