package com.rexbas.bouncingballs.api.capability;

public interface IBounceCapability {
	public void addBounce();
	public void resetConsecutiveBounces();
	public int getConsecutiveBounces();
	public void setStartTickGroundOrLiquid(boolean onGroundOrLiquid);
	public boolean getStartTickGroundOrLiquid();
}