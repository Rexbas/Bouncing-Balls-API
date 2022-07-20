package com.rexbas.bouncingballs.api;

import com.rexbas.bouncingballs.api.capability.BounceCapabilityProvider;
import com.rexbas.bouncingballs.api.client.renderer.SitRenderer;
import com.rexbas.bouncingballs.api.item.IBouncingBall;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
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
	
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent event) {
		event.player.getCapability(BounceCapabilityProvider.BOUNCE_CAPABILITY).ifPresent(cap -> {
			if (event.phase == TickEvent.Phase.START) {
				cap.setStartTickOnGround(event.player.isOnGround());
			}
			else if (event.phase == TickEvent.Phase.END) {
				if (cap.getConsecutiveBounces() > 0 && event.player.isOnGround() && cap.getStartTickOnGround()) {
					cap.resetConsecutiveBounces();
					
					if (event.player.getOffhandItem().getItem() instanceof IBouncingBall) {
						((IBouncingBall) event.player.getOffhandItem().getItem()).onFall(event.player, event.player.getOffhandItem(), 0);
					}
					else if (event.player.getMainHandItem().getItem() instanceof IBouncingBall) {
						((IBouncingBall) event.player.getMainHandItem().getItem()).onFall(event.player, event.player.getMainHandItem(), 0);
					}
					event.player.hurtMarked = true;
				}
			}
		});
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
	
	// TODO -> RenderLivingEvent<LivingEntity, EntityModel<T>>
	
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

			SitRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> sitRenderer = new SitRenderer<>(event.getRenderer());
			sitRenderer.render((AbstractClientPlayerEntity) event.getEntityLiving(), 0, event.getPartialRenderTick(), event.getMatrixStack(), event.getBuffers(), event.getLight());
		}
	}
}