package com.rexbas.bouncingballs.api.network;

import com.rexbas.bouncingballs.api.BouncingBallsAPI;
import com.rexbas.bouncingballs.api.network.packet.SUpdateBounceCapabilityPacket;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

public class BouncingBallsAPINetwork {

	public static final String PROTOCOL_VERSION = "1"; // Increase when packets change
	
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(BouncingBallsAPI.MODID, "network"),
			() -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	
	public static void init() {
		int ID = 0;
		CHANNEL.registerMessage(ID++, SUpdateBounceCapabilityPacket.class, SUpdateBounceCapabilityPacket::encode, SUpdateBounceCapabilityPacket::decode, SUpdateBounceCapabilityPacket::handle);
	}
}