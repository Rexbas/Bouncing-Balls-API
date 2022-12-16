package com.rexbas.bouncingballs.api;

import com.rexbas.bouncingballs.api.network.BouncingBallsAPINetwork;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(BouncingBallsAPI.MODID)
public class BouncingBallsAPI {
	public static final String MODID = "bouncingballs_api";
	
	public BouncingBallsAPI() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

		BouncingBallsSounds.SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
    public void setup(final FMLCommonSetupEvent event) {  
    	BouncingBallsAPINetwork.init();
    }
    
    @Mod.EventBusSubscriber(modid = BouncingBallsAPI.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class BouncingBallsSounds {
    	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BouncingBallsAPI.MODID);
    	public static final RegistryObject<SoundEvent> BOUNCE = SOUNDS.register("bounce", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BouncingBallsAPI.MODID, "bounce")));
    }
}