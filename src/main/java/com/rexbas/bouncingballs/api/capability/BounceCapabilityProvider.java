package com.rexbas.bouncingballs.api.capability;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class BounceCapabilityProvider implements ICapabilityProvider {
	
	@CapabilityInject(IBounceCapability.class)
	public static final Capability<IBounceCapability> BOUNCE_CAPABILITY = null;

	private final LazyOptional<IBounceCapability> INSTANCE = LazyOptional.of(BounceCapability::new);

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == BOUNCE_CAPABILITY ? INSTANCE.cast() : LazyOptional.empty();
	}
}