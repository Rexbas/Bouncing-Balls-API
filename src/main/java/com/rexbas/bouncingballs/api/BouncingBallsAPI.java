package com.rexbas.bouncingballs.api;

import com.rexbas.bouncingballs.api.attachment.BounceData;
import com.rexbas.bouncingballs.api.network.BouncingBallsAPINetwork;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

@Mod(BouncingBallsAPI.MODID)
public class BouncingBallsAPI {
	public static final String MODID = "bouncingballs_api";

	public BouncingBallsAPI(IEventBus modEventBus) {
		modEventBus.addListener(this::setup);

		BouncingBallsSounds.SOUNDS.register(modEventBus);
		AttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
	}

	public void setup(final FMLCommonSetupEvent event) {
		BouncingBallsAPINetwork.init();
	}

	public static class BouncingBallsSounds {
		public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, BouncingBallsAPI.MODID);
		public static final Supplier<SoundEvent> BOUNCE = SOUNDS.register("bounce", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BouncingBallsAPI.MODID, "bounce")));
	}

	public static class AttachmentTypes {
		public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, BouncingBallsAPI.MODID);
		public static final Supplier<AttachmentType<BounceData>> BOUNCE_DATA = ATTACHMENT_TYPES.register(
				"bounce_data", () -> AttachmentType.builder(BounceData::new).build()
		);
	}
}