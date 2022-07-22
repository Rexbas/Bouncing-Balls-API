package com.rexbas.bouncingballs.api;

import java.lang.reflect.Method;

import com.rexbas.bouncingballs.api.capability.BounceCapabilityProvider;
import com.rexbas.bouncingballs.api.client.renderer.PlayerSitRenderer;
import com.rexbas.bouncingballs.api.item.IBouncingBall;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@Mod.EventBusSubscriber(modid = BouncingBallsAPI.MODID)
public class BouncingBallsAPIEvents {
	
	@SubscribeEvent
	public static void attachtCapability(AttachCapabilitiesEvent<Entity> event) {	
		if (event.getObject() instanceof PlayerEntity) {
			event.addCapability(new ResourceLocation(BouncingBallsAPI.MODID, "capability.bounce"), new BounceCapabilityProvider());
		}
	}
		
	@SubscribeEvent
	public static void onLivingUpdate(LivingUpdateEvent event) throws Throwable {
		final IBouncingBall ball;
		if (event.getEntityLiving().getOffhandItem().getItem() instanceof IBouncingBall) {
			ball = (IBouncingBall) event.getEntityLiving().getOffhandItem().getItem();
		}
		else if (event.getEntityLiving().getMainHandItem().getItem() instanceof IBouncingBall) {
			ball = (IBouncingBall) event.getEntityLiving().getMainHandItem().getItem();
		}
		else {
			ball = null;
		}
		
		event.getEntityLiving().getCapability(BounceCapabilityProvider.BOUNCE_CAPABILITY).ifPresent(cap -> {
			if (cap.getConsecutiveBounces() > 0) {
				if (event.getEntityLiving().fallDistance == 0 && (event.getEntityLiving().isOnGround() || event.getEntityLiving().level.containsAnyLiquid(event.getEntityLiving().getBoundingBox()))) {
					cap.resetConsecutiveBounces();
					
					if (ball != null && event.getEntityLiving().isOnGround()) {
						ball.onFall(event.getEntityLiving(), event.getEntityLiving().getOffhandItem(), 0);
						event.getEntityLiving().hurtMarked = true;
					}
				}
			}
		});
		
		if (ball != null) {
			Method m = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "func_241208_cS_"); // isAffectedByFluids
			boolean isAffectedByFluids = (boolean) m.invoke(event.getEntityLiving());
			if (isAffectedByFluids && !event.getEntityLiving().isSwimming()) {
				for (ITag<Fluid> fluid : FluidTags.getWrappers()) {
					if (event.getEntityLiving().getFluidHeight(fluid) > 0) {
						ball.inLiquid(event.getEntityLiving(), fluid);
						break;
					}
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