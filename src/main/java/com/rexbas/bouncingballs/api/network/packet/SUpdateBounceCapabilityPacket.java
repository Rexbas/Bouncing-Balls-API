package com.rexbas.bouncingballs.api.network.packet;

import java.util.function.Supplier;

import com.rexbas.bouncingballs.api.capability.BounceCapability;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

public class SUpdateBounceCapabilityPacket {
	
	private CompoundTag nbt;
	private int entityID;
	
	public SUpdateBounceCapabilityPacket(int entityID, CompoundTag nbt) {
		this.entityID = entityID;
		this.nbt = nbt;
	}
	
	public static void encode(SUpdateBounceCapabilityPacket packet, FriendlyByteBuf buf) {
		buf.writeInt(packet.entityID);
		buf.writeNbt(packet.nbt);
	}
	
	public static SUpdateBounceCapabilityPacket decode(FriendlyByteBuf buf) {
		return new SUpdateBounceCapabilityPacket(buf.readInt(), buf.readNbt());
	}
	
	public static void handle(SUpdateBounceCapabilityPacket packet, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Entity entity = Minecraft.getInstance().level.getEntity(packet.entityID);
			if (entity != null) {
				entity.getCapability(BounceCapability.BOUNCE_CAPABILITY).ifPresent(cap -> {
					cap.deserializeNBT(packet.nbt);
				});
			}
		});
		ctx.get().setPacketHandled(true);
	}
}