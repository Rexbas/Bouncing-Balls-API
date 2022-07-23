package com.rexbas.bouncingballs.api.capability;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import net.minecraft.fluid.Fluid;
import net.minecraft.tags.ITag;

public class BounceCapability implements IBounceCapability {
	
	private AtomicInteger consecutiveBounces;
	private int ticksSinceLastReset;
	private int ticksOnGround;
	private int ticksInFluid;
	private ITag<Fluid> lastFluid;
	
	public BounceCapability() {
		this.consecutiveBounces = new AtomicInteger(0);
		this.ticksSinceLastReset = 0;
		this.ticksOnGround = 0;
		this.ticksInFluid = 0;
		this.lastFluid = null;
	}
	
	@Override
	public void addBounce() {
		this.consecutiveBounces.incrementAndGet();
		this.ticksOnGround = 0;
		this.ticksInFluid = 0;
	}
	
	@Override
	public void resetConsecutiveBounces(float fallDistance) {
		if (this.consecutiveBounces.get() != 0) {
			this.consecutiveBounces.set(0);
			this.ticksSinceLastReset = 0;
		}
		else if (fallDistance > 3) {
			this.ticksSinceLastReset = 0;
		}
	}
	
	@Override
	public int getConsecutiveBounces() {
		return this.consecutiveBounces.get();
	}
	
	@Override
	public void increaseTicksOnGround() {
		this.ticksOnGround++;
		this.lastFluid = null;
	}
	
	@Override
	public void increaseTicksInFluid() {
		this.ticksInFluid++;
	}
	
	@Override
	public void increaseTicksSinceLastReset() {
		this.ticksSinceLastReset++;
	}
	
	@Override
	public void resetTicksOnGround() {
		this.ticksOnGround = 0;
	}
	
	@Override
	public void resetTicksInFluid() {
		this.ticksInFluid = 0;
	}
	
	@Override
	public int getTicksOnGround() {
		return this.ticksOnGround;
	}
	
	@Override
	public int getTicksInFluid() {
		return this.ticksInFluid;
	}
	
	@Override
	public int getTicksSinceLastReset() {
		return this.ticksSinceLastReset;
	}
	
	@Override
	public void setLastFluid(ITag<Fluid> fluid) {
		this.lastFluid = fluid;
	}
	
	/**
	 * @return The the last fluid the entity has been in. When the entity hits the ground this becomes null.
	 */
	@Override
	@Nullable
	public ITag<Fluid> getLastFluid() {
		return this.lastFluid;
	}
}