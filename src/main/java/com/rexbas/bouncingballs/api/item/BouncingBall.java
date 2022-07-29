package com.rexbas.bouncingballs.api.item;

import java.util.HashSet;
import java.util.List;

import com.rexbas.bouncingballs.api.BouncingBallsAPI.BouncingBallsSounds;
import com.rexbas.bouncingballs.api.capability.BounceCapability;
import com.rexbas.bouncingballs.api.capability.IBounceCapability;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BouncingBall extends Item implements IBouncingBall {
	
	protected BouncingBall.Properties properties;

	public BouncingBall(Item.Properties itemProperties, BouncingBall.Properties ballProperties) {
		super(itemProperties.stacksTo(1).defaultDurability(ballProperties.durability));
		this.properties = ballProperties;
	}

	@Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    	ItemStack stack = player.getItemInHand(hand);
    	
    	if (hand == InteractionHand.MAIN_HAND && player.getOffhandItem().getItem() instanceof IBouncingBall) {
    		return new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, stack);
    	}

    	if (!player.level.isClientSide() && canBounce(player)) {
    		bounce(player, properties.upwardMotion);
    		damageBall(player, stack);
			playBounceSound(level, player);
    		return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, stack);
    	}
		return new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, stack);
	}
	
	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		return properties.repairItem != Items.AIR && repair.getItem() == properties.repairItem;
	}
	
	/**
	 * Determines if the entity can bounce.
	 * 
	 * @param entity The entity using the ball.
	 * @return Whether or not the entity can bounce.
	 */
	@Override
	public boolean canBounce(LivingEntity entity) {
		IBounceCapability cap = entity.getCapability(BounceCapability.BOUNCE_CAPABILITY).orElse(null);
		if (cap != null) {
			if (properties.mustStartOnGroundOrFluid && cap.getConsecutiveBounces() == 0) {
				return cap.getConsecutiveBounces() < properties.maxConsecutiveBounces && 
						(cap.getTicksOnGround() > 0 && !entity.level.containsAnyLiquid(entity.getBoundingBox()) ||
								(cap.getTicksInFluid() > 0 && cap.getLastFluid() != null &&
								properties.fluidList.contains(cap.getLastFluid()) &&
								!entity.isEyeInFluid(cap.getLastFluid()))) &&
						hasConsumptionItem(entity);
			}
			return cap.getConsecutiveBounces() < properties.maxConsecutiveBounces &&
					(!entity.level.containsAnyLiquid(entity.getBoundingBox()) ||
							(cap.getLastFluid() != null &&
							properties.fluidList.contains(cap.getLastFluid()) &&
							!entity.isEyeInFluid(cap.getLastFluid())))
					&& hasConsumptionItem(entity);
		}
		return false;
	}
	
	/**
	 * Determine if the entity should be in a sitting pose.
	 * 
	 * @param entity The entity using the ball.
	 * @return Whether or not the entity should be in the sitting pose.
	 */
	@Override
	public boolean shouldSitOnBall(LivingEntity entity) {
		IBounceCapability cap = entity.getCapability(BounceCapability.BOUNCE_CAPABILITY).orElse(null);
		if (cap != null) {
			return cap.getConsecutiveBounces() > 0 && !entity.isOnGround() ||
					cap.getTicksSinceLastReset() < 7 || entity.fallDistance > 3 || 
					(properties.fluidList.contains(cap.getLastFluid()) && !entity.isSwimming());
		}
		return false;
	}

	/**
	 * Add y-motion to the entity and reduce the consumption item if applicable.
	 * 
	 * @param entity  The entity using the ball.
	 * @param motionY The y-motion to add.
	 */
	@Override
	public void bounce(LivingEntity entity, float motionY) {
		float yaw = entity.getYRot();
		float pitch = entity.getXRot();
		double motionX = (double)(-Mth.sin(yaw / 180.0F * (float)Math.PI) * Mth.cos(pitch / 180.0F * (float)Math.PI) * properties.forwardMotion);
		double motionZ = (double)(Mth.cos(yaw / 180.0F * (float)Math.PI) * Mth.cos(pitch / 180.0F * (float)Math.PI) * properties.forwardMotion);
		
		if (entity.level.containsAnyLiquid(entity.getBoundingBox())) {
			entity.setDeltaMovement(entity.getDeltaMovement().x(), 0, entity.getDeltaMovement().z());
		}
		
		entity.push(motionX, motionY, motionZ);
		entity.hurtMarked = true;
		
		entity.getCapability(BounceCapability.BOUNCE_CAPABILITY).ifPresent(cap -> {
			cap.addBounce();
		});
		
		if (properties.consumptionItem.getItem() != Items.AIR) {
			entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(itemHandler -> {
				int slot = findConsumptionItemSlot(itemHandler);
				if (slot != -1) {
					itemHandler.extractItem(slot, 1, false);
				}
			});
		}
	}
	
	/**
	 * Handles a fall with the ball and returns a damage multiplier.
	 * 
	 * @param entity 		The entity using the ball.
	 * @param stack 		The ItemStack containing the ball.
	 * @param fallDistance	The fall distance.
	 * @return A multiplier for the fall damage.
	 */
	@Override
	public float onFall(LivingEntity entity, ItemStack stack, float fallDistance) {
		if (!entity.level.isClientSide()) {
			if (fallDistance > properties.rebounceHeight) {
				float multiplier;
				if (hasConsumptionItem(entity)) {
					bounce(entity, properties.upwardMotion);
					multiplier = properties.damageMultiplier;
				} else {
					bounce(entity, properties.upwardMotion / 2);
					multiplier = properties.damageMultiplier * 2 > 1 ? 1 : properties.damageMultiplier * 2;
				}
		
				damageBall(entity, stack);
				playBounceSound(entity.level, entity);
				return multiplier;
			}
		}
		return 0f;
	}
	
	/**
	 * Handles damage (except fall damage)
	 * 
	 * @param entity		The entity using the ball.
	 * @param damageSource	The damage source.
	 * @param amount		The damage amount.
	 * @return Whether or not the damage should be canceled.
	 */
	@Override
	public boolean onDamage(LivingEntity entity, DamageSource damageSource, float amount) {
		return false;
	}
	
	/**
	 * Handles what happens when the entity is in a fluid.
	 * 
	 * @param entity	The entity using the ball.
	 * @param fluid		The fluid.
	 */
	@Override
	public void inFluid(LivingEntity entity, Tag<Fluid> fluid) {
		if (properties.fluidList.contains(fluid)) {
			
			entity.getCapability(BounceCapability.BOUNCE_CAPABILITY).ifPresent(cap -> {
				cap.setLastFluid(fluid);
			});
			
			if (!entity.level.isClientSide()) {
				double d = 0.1 * entity.getFluidHeight(fluid) + 0.0175;
				
				if (entity.getDeltaMovement().y() < 0 && entity.getFluidHeight(fluid) > 0) {
					d += -0.98 * entity.getDeltaMovement().y();	
				}
				
				if (fluid == FluidTags.LAVA) {
					d *= 1.5;
				}
				
				entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, entity.getAttribute(ForgeMod.SWIM_SPEED.get()).getValue() * d, 0.0D));
				entity.hurtMarked = true;
			}
		}
	}
	
	/**
	 * Damage the ball if applicable.
	 * 
	 * @param entity	The entity using the ball.
	 * @param stack		The ItemStack containing the ball.
	 */
	public void damageBall(LivingEntity entity, ItemStack stack) {
		stack.hurtAndBreak(1, entity, (p) -> {});
	}
	
	/**
	 * Playing the bounce sound.
	 * 
	 * @param world		The world.
	 * @param entity	The entity using the ball.
	 */
	public void playBounceSound(Level level, LivingEntity entity) {
		float pitch = level.random.nextFloat() * (1.1f - 0.9f) + 0.9f;
		level.playSound(null, entity, getBounceSound(),	SoundSource.PLAYERS, 1, pitch);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> list, TooltipFlag flag) {
		for (Tag<Fluid> fluid : properties.fluidList) {
			if (fluid == FluidTags.WATER) {
				list.add(new TranslatableComponent("bouncingballs_api.hovertext.water_floating").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x0099FF))));
			}
			else if (fluid == FluidTags.LAVA) {
				list.add(new TranslatableComponent("bouncingballs_api.hovertext.lava_floating").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF9900))));
			}
		}
		if (properties.consumptionItem.getItem() != Items.AIR) {
			list.add(new TranslatableComponent("bouncingballs_api.hovertext.consumes").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xAAAAAA)))
					.append(" ")
					.append(properties.consumptionItem.getHoverName()));
		}
    }
	
	public SoundEvent getBounceSound() {
		return BouncingBallsSounds.BOUNCE.get();
	}
	
	public Item getRecipeItem() {
		return this.properties.recipeItem;
	}
	
	protected boolean hasConsumptionItem(LivingEntity entity) {
		if (properties.consumptionItem.getItem() == Items.AIR) {
			return true;
		}
		
		IItemHandler itemHandler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
		if (itemHandler != null) {
			return findConsumptionItemSlot(itemHandler) != -1;
		}
		return false;
	}
	
	protected int findConsumptionItemSlot(IItemHandler itemHandler) {
		for (int i = 0; i < itemHandler.getSlots(); ++i) {
			ItemStack stack = itemHandler.getStackInSlot(i);
			if (!stack.isEmpty() && stack.getItem() == properties.consumptionItem.getItem() && ItemStack.tagMatches(stack, properties.consumptionItem)) {
				return i;
			}
		}
		return -1;
	}
	
	public static class Properties {
		
		public int durability;
		public Item repairItem;
		public float forwardMotion;
		public float upwardMotion;
		public float rebounceHeight;
		public float damageMultiplier;
		public boolean mustStartOnGroundOrFluid;
		public int maxConsecutiveBounces;
		public ItemStack consumptionItem;
		public HashSet<Tag<Fluid>> fluidList;
		public Item recipeItem;
		
		public Properties(int durability, Item repairItem, float forwardMotion, float upwardMotion, float rebounceHeight, float damageMultiplier, boolean mustStartOnGroundOrFluid, int maxConsecutiveBounces, Item consumptionItem) {
			this.durability = durability;
			this.repairItem = repairItem;
			this.forwardMotion = forwardMotion;
			this.upwardMotion = upwardMotion;
			this.rebounceHeight = rebounceHeight;
			this.damageMultiplier = damageMultiplier;
			this.mustStartOnGroundOrFluid = mustStartOnGroundOrFluid;
			this.maxConsecutiveBounces = maxConsecutiveBounces;
			this.consumptionItem = new ItemStack(consumptionItem);
			this.fluidList = new HashSet<Tag<Fluid>>();
			this.recipeItem = Items.AIR;
		}
		
		public Properties(int durability, Item repairItem, float forwardMotion, float upwardMotion, float rebounceHeight, float damageMultiplier) {
			this(durability, repairItem, forwardMotion, upwardMotion, rebounceHeight, damageMultiplier, true, 1, Items.AIR);
		}
		
		public Properties(float forwardMotion, float upwardMotion, float rebounceHeight, float damageMultiplier) {
			this(0, Items.AIR, forwardMotion, upwardMotion, rebounceHeight, damageMultiplier, true, 1, Items.AIR);
		}
		
		public Properties(float forwardMotion, float upwardMotion, float rebounceHeight, float damageMultiplier, boolean mustStartOnGround, int maxConsecutiveBounces, Item consumptionItem) {
			this(0, Items.AIR, forwardMotion, upwardMotion, rebounceHeight, damageMultiplier, mustStartOnGround, maxConsecutiveBounces, consumptionItem);
		}
		
		public Properties() {
			this(100, Items.SLIME_BALL, 0.5f, 0.65f, 10f, 0.5f, true, 1, Items.AIR);
		}
		
		public Properties addFluid(Tag<Fluid> fluid) {
			this.fluidList.add(fluid);
			return this;
		}
		
		public Properties recipeItem(Item recipeItem) {
			this.recipeItem = recipeItem;
			return this;
		}
	}
}