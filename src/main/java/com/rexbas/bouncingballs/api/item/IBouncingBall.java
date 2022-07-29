package com.rexbas.bouncingballs.api.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public interface IBouncingBall {
	public boolean canBounce(LivingEntity entity);
	public boolean shouldSitOnBall(LivingEntity entity);
	public void bounce(LivingEntity entity, float motionY);
	public float onFall(LivingEntity entity, ItemStack stack, float fallDistance);
	public boolean onDamage(LivingEntity entity, DamageSource damageSource, float amount);
	public void inFluid(LivingEntity entity, TagKey<Fluid> fluid);
}