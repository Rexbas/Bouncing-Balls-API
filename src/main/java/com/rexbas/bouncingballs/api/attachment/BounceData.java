package com.rexbas.bouncingballs.api.attachment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

public class BounceData implements INBTSerializable<CompoundTag> {

	private AtomicInteger consecutiveBounces;
	private int ticksSinceLastReset;
	private int ticksOnGround;
	private int ticksInFluid;
	private TagKey<Fluid> lastFluid;
	
	private boolean markedForUpdate;
		
	public BounceData() {
		this.consecutiveBounces = new AtomicInteger(0);
		this.ticksSinceLastReset = 1200;
		this.ticksOnGround = 0;
		this.ticksInFluid = 0;
		this.lastFluid = null;
		this.markedForUpdate = true;
	}
	
	public void addBounce() {
		this.consecutiveBounces.incrementAndGet();
		this.ticksOnGround = 0;
		this.ticksInFluid = 0;
		this.markedForUpdate = true;
	}
	
	public void resetConsecutiveBounces(float fallDistance) {
		if (this.consecutiveBounces.get() != 0) {
			this.consecutiveBounces.set(0);
			this.ticksSinceLastReset = 0;
		}
		else if (fallDistance > 3) {
			this.ticksSinceLastReset = 0;
		}
		this.markedForUpdate = true;
	}
	
	public int getConsecutiveBounces() {
		return this.consecutiveBounces.get();
	}
	
	public void increaseTicksOnGround() {
		this.ticksOnGround++;
		this.lastFluid = null;
	}
	
	public void increaseTicksInFluid() {
		this.ticksInFluid++;
	}
	
	public void increaseTicksSinceLastReset() {
		this.ticksSinceLastReset++;
	}
	
	public void resetTicksOnGround() {
		this.ticksOnGround = 0;
		this.markedForUpdate = true;
	}
	
	public void resetTicksInFluid() {
		this.ticksInFluid = 0;
		this.markedForUpdate = true;
	}
	
	public int getTicksOnGround() {
		return this.ticksOnGround;
	}
	
	public int getTicksInFluid() {
		return this.ticksInFluid;
	}
	
	public int getTicksSinceLastReset() {
		return this.ticksSinceLastReset;
	}
	
	public void setLastFluid(TagKey<Fluid> fluid) {
		this.lastFluid = fluid;
	}
	
	/**
	 * @return The the last fluid the entity has been in. When the entity hits the ground this becomes null.
	 */
	@Nullable
	public TagKey<Fluid> getLastFluid() {
		return this.lastFluid;
	}
	
	public void setMarkedForUpdate(boolean update) {
		this.markedForUpdate = update;
	}
	
	public boolean getMarkedForUpdate() {
		return this.markedForUpdate;
	}
	
	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("consecutiveBounces", this.consecutiveBounces.get());
		nbt.putInt("ticksSinceLastReset", this.ticksSinceLastReset);
		nbt.putInt("ticksOnGround", this.ticksOnGround);
		nbt.putInt("ticksInFluid", this.ticksInFluid);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		this.consecutiveBounces.set(nbt.getInt("consecutiveBounces"));
		this.ticksSinceLastReset = nbt.getInt("ticksSinceLastReset");
		this.ticksOnGround = nbt.getInt("ticksOnGround");
		this.ticksInFluid = nbt.getInt("ticksInFluid");
	}
}