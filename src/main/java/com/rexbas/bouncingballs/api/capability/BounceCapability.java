package com.rexbas.bouncingballs.api.capability;

import java.util.concurrent.atomic.AtomicInteger;

public class BounceCapability implements IBounceCapability {
	
	private AtomicInteger consecutiveBounces;
	private int ticksOnGround;
	private int ticksInLiquid;
	
	public BounceCapability() {
		this.consecutiveBounces = new AtomicInteger(0);
		this.ticksOnGround = 0;
		this.ticksInLiquid = 0;
	}
	
	@Override
	public void addBounce() {
		this.consecutiveBounces.incrementAndGet();
		this.ticksOnGround = 0;
		this.ticksInLiquid = 0;
	}
	
	@Override
	public void resetConsecutiveBounces() {
		this.consecutiveBounces.set(0);
	}
	
	@Override
	public int getConsecutiveBounces() {
		return this.consecutiveBounces.get();
	}
	
	@Override
	public void increaseTicksOnGround() {
		this.ticksOnGround++;
	}
	
	@Override
	public void increaseTicksInLiquid() {
		this.ticksInLiquid++;
	}
	
	@Override
	public void resetTicksOnGround() {
		this.ticksOnGround = 0;
	}
	
	@Override
	public void resetTicksInLiquid() {
		this.ticksInLiquid = 0;
	}
	
	@Override
	public int getTicksOnGround() {
		return this.ticksOnGround;
	}
	
	@Override
	public int getTicksInLiquid() {
		return this.ticksInLiquid;
	}
}