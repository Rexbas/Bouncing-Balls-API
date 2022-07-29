package com.rexbas.bouncingballs.api.capability;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public interface IBounceCapability extends ICapabilitySerializable<CompoundTag> {
	public void addBounce();
	public void resetConsecutiveBounces(float fallDistance);
	public int getConsecutiveBounces();
	public void increaseTicksOnGround();
	public void increaseTicksInFluid();
	public void increaseTicksSinceLastReset();
	public void resetTicksOnGround();
	public void resetTicksInFluid();
	public int getTicksOnGround();
	public int getTicksInFluid();
	public int getTicksSinceLastReset();
	public void setLastFluid(Tag<Fluid> fluid);
	@Nullable
	public Tag<Fluid> getLastFluid();
	public void setMarkedForUpdate(boolean update);
	public boolean getMarkedForUpdate();
}