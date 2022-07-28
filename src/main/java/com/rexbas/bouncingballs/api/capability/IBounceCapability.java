package com.rexbas.bouncingballs.api.capability;

import javax.annotation.Nullable;

import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public interface IBounceCapability extends ICapabilitySerializable<CompoundNBT> {
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
	public void setLastFluid(ITag<Fluid> fluid);
	@Nullable
	public ITag<Fluid> getLastFluid();
	public void setMarkedForUpdate(boolean update);
	public boolean getMarkedForUpdate();
}