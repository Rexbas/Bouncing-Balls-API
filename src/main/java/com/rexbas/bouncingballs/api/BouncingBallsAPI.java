package com.rexbas.bouncingballs.api;

import com.rexbas.bouncingballs.api.network.BouncingBallsAPINetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

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

	public static class BouncingBallsSounds {
		public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BouncingBallsAPI.MODID);
		public static final RegistryObject<SoundEvent> BOUNCE = SOUNDS.register("bounce", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BouncingBallsAPI.MODID, "bounce")));
	}
}