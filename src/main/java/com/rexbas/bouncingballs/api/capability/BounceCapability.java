package com.rexbas.bouncingballs.api.capability;

public class BounceCapability implements IBounceCapability {
	
	private int consecutiveBounces;
	public boolean startTickGroundOrLiquid;
	
	public BounceCapability() {
		this.consecutiveBounces = 0;
		this.startTickGroundOrLiquid = false;
	}
	
	@Override
	public void addBounce() {
		this.consecutiveBounces += 1;
	}
	
	@Override
	public void resetConsecutiveBounces() {
		this.consecutiveBounces = 0;
	}
	
	@Override
	public int getConsecutiveBounces() {
		return this.consecutiveBounces;
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