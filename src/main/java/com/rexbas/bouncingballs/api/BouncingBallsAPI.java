package com.rexbas.bouncingballs.api;

import com.rexbas.bouncingballs.api.capability.BounceCapability;
import com.rexbas.bouncingballs.api.capability.BounceCapabilityProvider;
import com.rexbas.bouncingballs.api.capability.IBounceCapability;
import com.rexbas.bouncingballs.api.client.renderer.SitRenderer;
import com.rexbas.bouncingballs.api.item.IBouncingBall;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(BouncingBallsAPI.MODID)
public class BouncingBallsAPI {
	public static final String MODID = "bouncingballs_api";
	
	public BouncingBallsAPI() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

		BouncingBallsSounds.SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
    public void setup(final FMLCommonSetupEvent event) {    	
    	CapabilityManager.INSTANCE.register(IBounceCapability.class, new IStorage<IBounceCapability>() {

			@Override
			public INBT writeNBT(Capability<IBounceCapability> capability, IBounceCapability instance, Direction side) {
				return null;
			}

			@Override
			public void readNBT(Capability<IBounceCapability> capability, IBounceCapability instance, Direction side, INBT nbt) {}
			
		}, BounceCapability::new);
	}
    
    
    @Mod.EventBusSubscriber(modid = BouncingBallsAPI.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class BouncingBallsSounds {
    	
    	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BouncingBallsAPI.MODID);
    	public static final RegistryObject<SoundEvent> BOUNCE = SOUNDS.register("bounce", () -> new SoundEvent(new ResourceLocation(BouncingBallsAPI.MODID, "bounce")));
    }
    
    @Mod.EventBusSubscriber(modid = BouncingBallsAPI.MODID)
    public static class Events {
    	
    	@SubscribeEvent
    	public static void attachtCapability(AttachCapabilitiesEvent<Entity> event) {	
    		if (event.getObject() instanceof PlayerEntity) {
    			event.addCapability(new ResourceLocation(BouncingBallsAPI.MODID, "capability.bounce"), new BounceCapabilityProvider());
    		}
    	}
    	
    	@SubscribeEvent
    	public static void tick(PlayerTickEvent event) {
    		event.player.getCapability(BounceCapabilityProvider.BOUNCE_CAPABILITY).ifPresent(cap -> {
    			if (event.phase == TickEvent.Phase.START) {
    				cap.setStartTickOnGround(event.player.isOnGround());
    			}
    			else if (event.phase == TickEvent.Phase.END) {
    				if (cap.getConsecutiveBounces() > 0 && event.player.isOnGround() && cap.getStartTickOnGround()) {
    					cap.resetConsecutiveBounces();
    					
    					if (event.player.getMainHandItem().getItem() instanceof IBouncingBall) {
    						((IBouncingBall) event.player.getMainHandItem().getItem()).onFall(event.player, event.player.getMainHandItem(), 0);
    					}
    					else if (event.player.getOffhandItem().getItem() instanceof IBouncingBall) {
    						((IBouncingBall) event.player.getOffhandItem().getItem()).onFall(event.player, event.player.getOffhandItem(), 0);
    					}
    					event.player.hurtMarked = true;
    				}
    			}
    		});
    	}
    	
    	@SubscribeEvent
    	public static void onCreativePlayerFall(PlayerFlyableFallEvent event) {
    		if (event.getEntity() instanceof PlayerEntity) {
    			PlayerEntity player = (PlayerEntity) event.getEntity();
    			player.getCapability(BounceCapabilityProvider.BOUNCE_CAPABILITY).ifPresent(cap -> {
    				cap.resetConsecutiveBounces();
    			});
    			
    			if (player.getMainHandItem().getItem() instanceof IBouncingBall) {
    				((IBouncingBall) player.getMainHandItem().getItem()).onFall(player, player.getMainHandItem(), event.getDistance());
    			}
    			else if (player.getOffhandItem().getItem() instanceof IBouncingBall) {
    				((IBouncingBall) player.getOffhandItem().getItem()).onFall(player, player.getOffhandItem(), event.getDistance());
    			}
    			player.hurtMarked = true;
    		}
    	}
    	
    	@SubscribeEvent
    	public static void onPlayerFall(LivingFallEvent event) {
    		if (event.getEntity() instanceof PlayerEntity) {
    			PlayerEntity player = (PlayerEntity) event.getEntity();
    			player.getCapability(BounceCapabilityProvider.BOUNCE_CAPABILITY).ifPresent(cap -> {
    				cap.resetConsecutiveBounces();
    			});
    			
    			float multiplier = 1;
    			if (player.getMainHandItem().getItem() instanceof IBouncingBall) {
    				multiplier = ((IBouncingBall) player.getMainHandItem().getItem()).onFall(player, player.getMainHandItem(), event.getDistance());
    			}
    			else if (player.getOffhandItem().getItem() instanceof IBouncingBall) {
    				multiplier = ((IBouncingBall) player.getOffhandItem().getItem()).onFall(player, player.getOffhandItem(), event.getDistance());
    			}
    			event.setDamageMultiplier(multiplier);
    			player.hurtMarked = true;
    		}
    	}
    	
    	
		// TODO -> RenderLivingEvent<LivingEntity, EntityModel<T>>
    	
    	@SubscribeEvent
    	public static void onPlayerRender(RenderPlayerEvent.Pre event) {
    		// TODO hand logic
    		if (event.getPlayer().getMainHandItem().getItem() instanceof IBouncingBall) {
    			IBouncingBall ball = (IBouncingBall) event.getPlayer().getMainHandItem().getItem();
    			
    			if (ball.shouldSitOnBall(event.getPlayer())) {
    				event.setCanceled(true);

        			SitRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> m = new SitRenderer<>(event.getRenderer());
    				m.render((AbstractClientPlayerEntity) event.getEntityLiving(), 0, event.getPartialRenderTick(), event.getMatrixStack(), event.getBuffers(), event.getLight());
    			}
    		}
    	}
    }
}