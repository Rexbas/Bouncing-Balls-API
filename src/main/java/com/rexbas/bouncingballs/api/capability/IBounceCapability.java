package com.rexbas.bouncingballs.api.capability;

public interface IBounceCapability {
	public void addBounce();
	public void resetConsecutiveBounces();
	public int getConsecutiveBounces();
	public void increaseTicksOnGround();
	public void increaseTicksInLiquid();
	public void resetTicksOnGround();
	public void resetTicksInLiquid();
	public int getTicksOnGround();
	public int getTicksInLiquid();
}