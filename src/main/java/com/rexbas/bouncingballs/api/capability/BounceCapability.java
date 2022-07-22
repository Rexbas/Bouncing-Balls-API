package com.rexbas.bouncingballs.api.capability;

import java.util.concurrent.atomic.AtomicInteger;

public class BounceCapability implements IBounceCapability {
	
	private AtomicInteger consecutiveBounces;
	public boolean startTickGroundOrLiquid;
	
	public BounceCapability() {
		this.consecutiveBounces = new AtomicInteger(0);
		this.startTickGroundOrLiquid = false;
	}
	
	@Override
	public void addBounce() {
		this.consecutiveBounces.incrementAndGet();
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
	public void setStartTickGroundOrLiquid(boolean onGroundOrLiquid) {
		this.startTickGroundOrLiquid = onGroundOrLiquid;
	}

	@Override
	public boolean getStartTickGroundOrLiquid() {
		return this.startTickGroundOrLiquid;
	}
}