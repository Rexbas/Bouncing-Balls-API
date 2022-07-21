package com.rexbas.bouncingballs.api;

import com.rexbas.bouncingballs.api.capability.BounceCapabilityProvider;
import com.rexbas.bouncingballs.api.client.renderer.PlayerSitRenderer;
import com.rexbas.bouncingballs.api.item.IBouncingBall;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BouncingBallsAPI.MODID)
public class BouncingBallsAPIEvents {
	
	@SubscribeEvent
	public static void attachtCapability(AttachCapabilitiesEvent<Entity> event) {	
		if (event.getObject() instanceof PlayerEntity) {
			event.addCapability(new ResourceLocation(BouncingBallsAPI.MODID, "capability.bounce"), new BounceCapabilityProvider());
		}
	}
	
	// TODO -> LivingTickEvent
	
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent event) {
		final IBouncingBall ball;
		if (event.player.getOffhandItem().getItem() instanceof IBouncingBall) {
			ball = (IBouncingBall) event.player.getOffhandItem().getItem();
		}
		else if (event.player.getMainHandItem().getItem() instanceof IBouncingBall) {
			ball = (IBouncingBall) event.player.getMainHandItem().getItem();
		}
		else {
			ball = null;
		}

		event.player.getCapability(BounceCapabilityProvider.BOUNCE_CAPABILITY).ifPresent(cap -> {
			if (event.phase == TickEvent.Phase.START) {
				cap.setStartTickGroundOrLiquid(event.player.isOnGround() || event.player.level.containsAnyLiquid(event.player.getBoundingBox()));
			}
			else if (event.phase == TickEvent.Phase.END) {
				if (cap.getConsecutiveBounces() > 0 && (event.player.isOnGround() || event.player.level.containsAnyLiquid(event.player.getBoundingBox())) && cap.getStartTickGroundOrLiquid()) {
					cap.resetConsecutiveBounces();
					
					if (ball != null && event.player.isOnGround()) {
						ball.onFall(event.player, event.player.getOffhandItem(), 0);
						event.player.hurtMarked = true;
					}
				}
			}
		});
		
		if (ball != null && event.player.isAffectedByFluids() && !event.player.isSwimming()) {
			for (ITag<Fluid> fluid : FluidTags.getWrappers()) {
				if (event.player.getFluidHeight(fluid) > 0) {
					ball.inLiquid(event.player, fluid);
					break;
				}
			}
		}
	}

	@SubscribeEvent
	public static void onCreativePlayerFall(PlayerFlyableFallEvent event) {
		event.getPlayer().getCapability(BounceCapabilityProvider.BOUNCE_CAPABILITY).ifPresent(cap -> {
			cap.resetConsecutiveBounces();
		});
		
		if (event.getPlayer().getOffhandItem().getItem() instanceof IBouncingBall) {
			((IBouncingBall) event.getPlayer().getOffhandItem().getItem()).onFall(event.getPlayer(), event.getPlayer().getOffhandItem(), event.getDistance());
		}
		else if (event.getPlayer().getMainHandItem().getItem() instanceof IBouncingBall) {
			((IBouncingBall) event.getPlayer().getMainHandItem().getItem()).onFall(event.getPlayer(), event.getPlayer().getMainHandItem(), event.getDistance());
		}
		event.getPlayer().hurtMarked = true;
	}
	
	@SubscribeEvent
	public static void onLivingFall(LivingFallEvent event) {
		event.getEntityLiving().getCapability(BounceCapabilityProvider.BOUNCE_CAPABILITY).ifPresent(cap -> {
			cap.resetConsecutiveBounces();
		});
				
		float multiplier = 1;
		if (event.getEntityLiving().getOffhandItem().getItem() instanceof IBouncingBall) {
			multiplier = ((IBouncingBall) event.getEntityLiving().getOffhandItem().getItem()).onFall(event.getEntityLiving(), event.getEntityLiving().getOffhandItem(), event.getDistance());
		}
		else if (event.getEntityLiving().getMainHandItem().getItem() instanceof IBouncingBall) {
			multiplier = ((IBouncingBall) event.getEntityLiving().getMainHandItem().getItem()).onFall(event.getEntityLiving(), event.getEntityLiving().getMainHandItem(), event.getDistance());
		}
		event.setDamageMultiplier(multiplier);
		event.getEntityLiving().hurtMarked = true;
	}
	
	@SubscribeEvent
	public static void onLivingAttack(LivingAttackEvent event) {
		if (event.getEntityLiving().getOffhandItem().getItem() instanceof IBouncingBall) {
			event.setCanceled(((IBouncingBall) event.getEntityLiving().getOffhandItem().getItem()).onDamage(event.getEntityLiving(), event.getSource(), event.getAmount()));
		}
		else if (event.getEntityLiving().getMainHandItem().getItem() instanceof IBouncingBall) {
			event.setCanceled(((IBouncingBall) event.getEntityLiving().getMainHandItem().getItem()).onDamage(event.getEntityLiving(), event.getSource(), event.getAmount()));
		}
	}
		
	@SubscribeEvent
	public static void onPlayerRender(RenderPlayerEvent.Pre event) {
		IBouncingBall ball = null;
		if (event.getPlayer().getOffhandItem().getItem() instanceof IBouncingBall) {
			ball = (IBouncingBall) event.getPlayer().getOffhandItem().getItem();
		}
		else if (event.getPlayer().getMainHandItem().getItem() instanceof IBouncingBall) {
			ball = (IBouncingBall) event.getPlayer().getMainHandItem().getItem();
		}
		
		if (ball != null && ball.shouldSitOnBall(event.getPlayer())) {
			event.setCanceled(true);

			PlayerSitRenderer sitRenderer = new PlayerSitRenderer(event.getRenderer(), (AbstractClientPlayerEntity) event.getPlayer());
			sitRenderer.render((AbstractClientPlayerEntity) event.getPlayer(), 0, event.getPartialRenderTick(), event.getMatrixStack(), event.getBuffers(), event.getLight());
		}
	}
}