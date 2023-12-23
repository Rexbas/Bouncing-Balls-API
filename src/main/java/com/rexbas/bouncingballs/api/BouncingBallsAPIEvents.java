package com.rexbas.bouncingballs.api;

import com.rexbas.bouncingballs.api.attachment.BounceData;
import com.rexbas.bouncingballs.api.client.renderer.PlayerSitRenderer;
import com.rexbas.bouncingballs.api.item.IBouncingBall;
import com.rexbas.bouncingballs.api.network.BouncingBallsAPINetwork;
import com.rexbas.bouncingballs.api.network.packet.SUpdateBounceCapabilityPacket;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerFlyableFallEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = BouncingBallsAPI.MODID)
public class BouncingBallsAPIEvents {

	@SubscribeEvent
	public static void onJoinLevel(EntityJoinLevelEvent event) {
		if (event.getEntity() instanceof Player player) {
			// Always create bounce data for players
			BounceData bounceData = player.getData(BouncingBallsAPI.AttachmentTypes.BOUNCE_DATA);
			player.setData(BouncingBallsAPI.AttachmentTypes.BOUNCE_DATA, bounceData);
		}
	}

	/**
	 * {@link #onLivingUpdate} is the main event that resets the consecutive bounces in water and calls {@link IBouncingBall#inFluid} for the ball.
	 * When a fall event is not fired this is a backup to reset the consecutive bounces on the ground. For example, when a player is stuck in cobweb.
	 */
	@SubscribeEvent
	public static void onLivingUpdate(LivingTickEvent event) throws Throwable {
		final IBouncingBall ball;
		if (event.getEntity().getOffhandItem().getItem() instanceof IBouncingBall) {
			ball = (IBouncingBall) event.getEntity().getOffhandItem().getItem();
		}
		else if (event.getEntity().getMainHandItem().getItem() instanceof IBouncingBall) {
			ball = (IBouncingBall) event.getEntity().getMainHandItem().getItem();
		}
		else {
			ball = null;
		}

		if (event.getEntity().hasData(BouncingBallsAPI.AttachmentTypes.BOUNCE_DATA)) {
			BounceData bounceData = event.getEntity().getData(BouncingBallsAPI.AttachmentTypes.BOUNCE_DATA);
			boolean inFluid = event.getEntity().level().containsAnyLiquid(event.getEntity().getBoundingBox());
			if (bounceData.getConsecutiveBounces() > 0) {
				if ((event.getEntity().fallDistance == 0 && bounceData.getTicksOnGround() > 3) || bounceData.getTicksInFluid() > 3) {
					bounceData.resetConsecutiveBounces(0);
				}
			}

			if (event.getEntity().onGround()) {
				bounceData.increaseTicksOnGround();
			}
			else {
				bounceData.resetTicksOnGround();
			}

			if (inFluid) {
				bounceData.increaseTicksInFluid();
			}
			else {
				bounceData.resetTicksInFluid();
			}

			if (bounceData.getLastFluid() != null) {
				if (event.getEntity().onGround()) {
					bounceData.setLastFluid(null);
				}
			}
			bounceData.increaseTicksSinceLastReset();
			event.getEntity().setData(BouncingBallsAPI.AttachmentTypes.BOUNCE_DATA, bounceData);
		}
		
		if (ball != null) {
			Method m = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "isAffectedByFluids"); // isAffectedByFluids()
			boolean isAffectedByFluids = (boolean) m.invoke(event.getEntity());
			if (isAffectedByFluids && !event.getEntity().isSwimming()) {
			    FluidState fluidstate = event.getEntity().level().getFluidState(BlockPos.containing(event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ()));
			    fluidstate.getTags().forEach((fluid) -> {
			    	if (event.getEntity().getFluidHeight(fluid) > 0) {
						ball.inFluid(event.getEntity(), fluid);
					}
			    });
			}
		}

		// TODO data attachments may get opt-in syncing
		if (event.getEntity().hasData(BouncingBallsAPI.AttachmentTypes.BOUNCE_DATA)) {
			BounceData bounceData = event.getEntity().getData(BouncingBallsAPI.AttachmentTypes.BOUNCE_DATA);
			if (bounceData.getMarkedForUpdate()) {
				if (!event.getEntity().level().isClientSide()) {
					BouncingBallsAPINetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(event::getEntity), new SUpdateBounceCapabilityPacket(event.getEntity().getId(), bounceData.serializeNBT()));
				}
				bounceData.setMarkedForUpdate(false);
			}
		}
	}

	/**
	 * {@link #onCreativePlayerFall} is the main event in creative mode that resets the consecutive bounces on the ground and calls {@link IBouncingBall#onFall} for the ball.
	 */
	@SubscribeEvent
	public static void onCreativePlayerFall(PlayerFlyableFallEvent event) {
		BounceData bounceData = event.getEntity().getData(BouncingBallsAPI.AttachmentTypes.BOUNCE_DATA);
		bounceData.resetConsecutiveBounces(event.getDistance());

		if (event.getEntity().getOffhandItem().getItem() instanceof IBouncingBall) {
			((IBouncingBall) event.getEntity().getOffhandItem().getItem()).onFall(event.getEntity(), event.getEntity().getOffhandItem(), event.getDistance());
		}
		else if (event.getEntity().getMainHandItem().getItem() instanceof IBouncingBall) {
			((IBouncingBall) event.getEntity().getMainHandItem().getItem()).onFall(event.getEntity(), event.getEntity().getMainHandItem(), event.getDistance());
		}
	}
	
	/**
	 * {@link #onLivingFall} is the main event in survival mode that resets the consecutive bounces on the ground and calls {@link IBouncingBall#onFall} for the ball.
	 */
	@SubscribeEvent
	public static void onLivingFall(LivingFallEvent event) {
		if (event.getEntity().hasData(BouncingBallsAPI.AttachmentTypes.BOUNCE_DATA)) {
			BounceData bounceData = event.getEntity().getData(BouncingBallsAPI.AttachmentTypes.BOUNCE_DATA);
			bounceData.resetConsecutiveBounces(event.getDistance());
		}

		float multiplier = 1;
		if (event.getEntity().getOffhandItem().getItem() instanceof IBouncingBall) {
			multiplier = ((IBouncingBall) event.getEntity().getOffhandItem().getItem()).onFall(event.getEntity(), event.getEntity().getOffhandItem(), event.getDistance());
		}
		else if (event.getEntity().getMainHandItem().getItem() instanceof IBouncingBall) {
			multiplier = ((IBouncingBall) event.getEntity().getMainHandItem().getItem()).onFall(event.getEntity(), event.getEntity().getMainHandItem(), event.getDistance());
		}
		event.setDamageMultiplier(multiplier);
	}
	
	@SubscribeEvent
	public static void onLivingAttack(LivingAttackEvent event) {
		if (event.getEntity().getOffhandItem().getItem() instanceof IBouncingBall) {
			event.setCanceled(((IBouncingBall) event.getEntity().getOffhandItem().getItem()).onDamage(event.getEntity(), event.getSource(), event.getAmount()));
		}
		else if (event.getEntity().getMainHandItem().getItem() instanceof IBouncingBall) {
			event.setCanceled(((IBouncingBall) event.getEntity().getMainHandItem().getItem()).onDamage(event.getEntity(), event.getSource(), event.getAmount()));
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onPlayerRender(RenderPlayerEvent.Pre event) {
		IBouncingBall ball = null;
		if (event.getEntity().getOffhandItem().getItem() instanceof IBouncingBall) {
			ball = (IBouncingBall) event.getEntity().getOffhandItem().getItem();
		}
		else if (event.getEntity().getMainHandItem().getItem() instanceof IBouncingBall) {
			ball = (IBouncingBall) event.getEntity().getMainHandItem().getItem();
		}
		
		if (ball != null && ball.shouldSitOnBall(event.getEntity())) {
			event.setCanceled(true);
			
			PlayerSitRenderer sitRenderer = new PlayerSitRenderer(event.getRenderer(), (AbstractClientPlayer) event.getEntity());
			sitRenderer.render((AbstractClientPlayer) event.getEntity(), 0, event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
		}
	}
}