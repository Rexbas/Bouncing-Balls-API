package com.rexbas.bouncingballs.api.capability;

public class BounceCapability implements IBounceCapability {
	
	private int consecutiveBounces;
	public boolean startTickOnGround;
	
	public BounceCapability() {
		this.consecutiveBounces = 0;
		this.startTickOnGround = false;
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
	public void setStartTickOnGround(boolean onGround) {
		this.startTickOnGround = onGround;
	}

	@Override
	public boolean getStartTickOnGround() {
		return this.startTickOnGround;
	}
}