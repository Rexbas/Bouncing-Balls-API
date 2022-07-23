package com.rexbas.bouncingballs.api.item;

import com.rexbas.bouncingballs.api.BouncingBallsAPI.BouncingBallsSounds;
import com.rexbas.bouncingballs.api.capability.BounceCapabilityProvider;
import com.rexbas.bouncingballs.api.capability.IBounceCapability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
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
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
    	ItemStack stack = player.getItemInHand(hand);
    	
    	if (hand == Hand.MAIN_HAND && player.getOffhandItem().getItem() instanceof IBouncingBall) {
    		return new ActionResult<ItemStack>(ActionResultType.FAIL, stack);
    	}
    	
    	if (canBounce(player)) {
    		bounce(player, properties.upwardMotion);
    		damageBall(player, stack);
			playBounceSound(world, player);
    		return new ActionResult<ItemStack>(ActionResultType.PASS, stack);
    	}
		return new ActionResult<ItemStack>(ActionResultType.FAIL, stack);
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
		IBounceCapability cap = entity.getCapability(BounceCapabilityProvider.BOUNCE_CAPABILITY).orElse(null);
		if (cap != null) {
			if (properties.mustStartOnGroundOrFluid && cap.getConsecutiveBounces() == 0) {
				return cap.getConsecutiveBounces() < properties.maxConsecutiveBounces && (cap.getTicksOnGround() > 0 ||
						(cap.getTicksInFluid() > 0 && entity.isInWater())) && !entity.isEyeInFluid(FluidTags.WATER) &&
						!entity.isInLava() && hasConsumptionItem(entity);
			}
			return cap.getConsecutiveBounces() < properties.maxConsecutiveBounces && !entity.isEyeInFluid(FluidTags.WATER) &&
					!entity.isInLava() && hasConsumptionItem(entity);
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
		IBounceCapability cap = entity.getCapability(BounceCapabilityProvider.BOUNCE_CAPABILITY).orElse(null);
		if (cap != null) {
			return (cap.getConsecutiveBounces() > 0 && !entity.isOnGround() || (entity.isInWater()) && !entity.isSwimming()) ||
					cap.getTicksSinceLastReset() < 5 || entity.fallDistance > 3 || cap.getLastFluid() == FluidTags.WATER;
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
		float yaw = entity.yRot;
		float pitch = entity.xRot;
		double motionX = (double)(-MathHelper.sin(yaw / 180.0F * (float)Math.PI) * MathHelper.cos(pitch / 180.0F * (float)Math.PI) * properties.forwardMotion);
		double motionZ = (double)(MathHelper.cos(yaw / 180.0F * (float)Math.PI) * MathHelper.cos(pitch / 180.0F * (float)Math.PI) * properties.forwardMotion);
		
		entity.push(motionX, motionY, motionZ);
		
		entity.getCapability(BounceCapabilityProvider.BOUNCE_CAPABILITY).ifPresent(cap -> {
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
	public void inFluid(LivingEntity entity, ITag<Fluid> fluid) {
		if (fluid == FluidTags.WATER) {
			
			entity.getCapability(BounceCapabilityProvider.BOUNCE_CAPABILITY).ifPresent(cap -> {
				cap.setLastFluid(fluid);
			});
			
			double d = 0.05 * entity.getFluidHeight(fluid) + 0.0075;
			
			if (entity.getDeltaMovement().y() < 0) {
				d += -0.6 * entity.getDeltaMovement().y();	
			}
			entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, entity.getAttribute(ForgeMod.SWIM_SPEED.get()).getValue() * d, 0.0D));
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
	public void playBounceSound(World world, LivingEntity entity) {
		float pitch = world.random.nextFloat() * (1.1f - 0.9f) + 0.9f;
		world.playSound(null, entity.blockPosition(), getBounceSound(), SoundCategory.AMBIENT, 1, pitch);
	}
	
	public SoundEvent getBounceSound() {
		return BouncingBallsSounds.BOUNCE.get();
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
		
		protected int durability;
		protected Item repairItem;
		protected float forwardMotion;
		protected float upwardMotion;
		protected float rebounceHeight;
		protected float damageMultiplier;
		
		protected boolean mustStartOnGroundOrFluid;
		protected int maxConsecutiveBounces;
		protected ItemStack consumptionItem;
		
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
	}
}