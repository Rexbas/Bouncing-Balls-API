package com.rexbas.bouncingballs.api;

import com.rexbas.bouncingballs.api.capability.BounceCapability;
import com.rexbas.bouncingballs.api.capability.IBounceCapability;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(BouncingBallsAPI.MODID)
public class BouncingBallsAPI {
	public static final String MODID = "bouncingballs_api";
	
	public BouncingBallsAPI() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

		BouncingBallsSounds.SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
    public void setup(final FMLCommonSetupEvent event) {    	
    	CapabilityManager.INSTANCE.register(IBounceCapability.class, new IStorage<IBounceCapability>() {

			@Override
			public INBT writeNBT(Capability<IBounceCapability> capability, IBounceCapability instance, Direction side) {
				return null;
			}

			@Override
			public void readNBT(Capability<IBounceCapability> capability, IBounceCapability instance, Direction side, INBT nbt) {}
			
		}, BounceCapability::new);
	}
    
    @Mod.EventBusSubscriber(modid = BouncingBallsAPI.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class BouncingBallsSounds {
    	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BouncingBallsAPI.MODID);
    	public static final RegistryObject<SoundEvent> BOUNCE = SOUNDS.register("bounce", () -> new SoundEvent(new ResourceLocation(BouncingBallsAPI.MODID, "bounce")));
    }
}