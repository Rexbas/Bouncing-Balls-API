package com.rexbas.bouncingballs.api.capability;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class BounceCapability implements ICapabilityProvider, IBounceCapability {
	
	public static final Capability<IBounceCapability> BOUNCE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

	private final LazyOptional<IBounceCapability> INSTANCE = LazyOptional.of(BounceCapability::new);

	private AtomicInteger consecutiveBounces;
	private int ticksSinceLastReset;
	private int ticksOnGround;
	private int ticksInFluid;
	private TagKey<Fluid> lastFluid;
	
	private boolean markedForUpdate;
		
	public BounceCapability() {
		this.consecutiveBounces = new AtomicInteger(0);
		this.ticksSinceLastReset = 0;
		this.ticksOnGround = 0;
		this.ticksInFluid = 0;
		this.lastFluid = null;
		this.markedForUpdate = true;
	}
	
	@Override
	public void addBounce() {
		this.consecutiveBounces.incrementAndGet();
		this.ticksOnGround = 0;
		this.ticksInFluid = 0;
		this.markedForUpdate = true;
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
		this.markedForUpdate = true;
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
		this.markedForUpdate = true;
	}
	
	@Override
	public void resetTicksInFluid() {
		this.ticksInFluid = 0;
		this.markedForUpdate = true;
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
	public void setLastFluid(TagKey<Fluid> fluid) {
		this.lastFluid = fluid;
	}
	
	/**
	 * @return The the last fluid the entity has been in. When the entity hits the ground this becomes null.
	 */
	@Override
	@Nullable
	public TagKey<Fluid> getLastFluid() {
		return this.lastFluid;
	}
	
	@Override
	public void setMarkedForUpdate(boolean update) {
		this.markedForUpdate = update;
	}
	
	@Override
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
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == BOUNCE_CAPABILITY ? INSTANCE.cast() : LazyOptional.empty();
	}
}