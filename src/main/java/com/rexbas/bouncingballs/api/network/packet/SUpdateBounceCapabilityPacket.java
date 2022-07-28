package com.rexbas.bouncingballs.api.network.packet;

import java.util.function.Supplier;

import com.rexbas.bouncingballs.api.capability.BounceCapability;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SUpdateBounceCapabilityPacket {
	
	private CompoundNBT nbt;
	private int entityID;
	
	public SUpdateBounceCapabilityPacket(int entityID, CompoundNBT nbt) {
		this.entityID = entityID;
		this.nbt = nbt;
	}
	
	public static void encode(SUpdateBounceCapabilityPacket packet, PacketBuffer buf) {
		buf.writeInt(packet.entityID);
		buf.writeNbt(packet.nbt);
	}
	
	public static SUpdateBounceCapabilityPacket decode(PacketBuffer buf) {
		return new SUpdateBounceCapabilityPacket(buf.readInt(), buf.readNbt());
	}
	
	public static void handle(SUpdateBounceCapabilityPacket packet, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Entity entity = Minecraft.getInstance().level.getEntity(packet.entityID);
			entity.getCapability(BounceCapability.BOUNCE_CAPABILITY).ifPresent(cap -> {
				cap.deserializeNBT(packet.nbt);
			});
		});
		ctx.get().setPacketHandled(true);
	}
}