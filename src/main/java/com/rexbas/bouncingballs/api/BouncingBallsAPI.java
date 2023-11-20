package com.rexbas.bouncingballs.api;

import com.rexbas.bouncingballs.api.network.BouncingBallsAPINetwork;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@Mod(BouncingBallsAPI.MODID)
public class BouncingBallsAPI {
	public static final String MODID = "bouncingballs_api";

	public BouncingBallsAPI(IEventBus modEventBus) {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

		BouncingBallsSounds.SOUNDS.register(modEventBus);
	}

	public void setup(final FMLCommonSetupEvent event) {
		BouncingBallsAPINetwork.init();
	}

	public static class BouncingBallsSounds {
		public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, BouncingBallsAPI.MODID);
		public static final Supplier<SoundEvent> BOUNCE = SOUNDS.register("bounce", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BouncingBallsAPI.MODID, "bounce")));
	}
}