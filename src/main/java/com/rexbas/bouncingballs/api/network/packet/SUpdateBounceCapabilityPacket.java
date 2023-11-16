package com.rexbas.bouncingballs.api.network.packet;

import com.rexbas.bouncingballs.api.capability.BounceCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.NetworkEvent;

public class SUpdateBounceCapabilityPacket {
	
	private final CompoundTag nbt;
	private final int entityID;
	
	public SUpdateBounceCapabilityPacket(int entityID, CompoundTag nbt) {
		this.entityID = entityID;
		this.nbt = nbt;
	}
	
	public static void encoder(SUpdateBounceCapabilityPacket packet, FriendlyByteBuf buf) {
		buf.writeInt(packet.entityID);
		buf.writeNbt(packet.nbt);
	}
	
	public static SUpdateBounceCapabilityPacket decoder(FriendlyByteBuf buf) {
		return new SUpdateBounceCapabilityPacket(buf.readInt(), buf.readNbt());
	}
	
	public static void messageConsumer(SUpdateBounceCapabilityPacket packet, NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			Entity entity = Minecraft.getInstance().level.getEntity(packet.entityID);
			if (entity != null) {
				entity.getCapability(BounceCapability.BOUNCE_CAPABILITY).ifPresent(cap -> {
					cap.deserializeNBT(packet.nbt);
				});
			}
		});
		ctx.setPacketHandled(true);
	}
}