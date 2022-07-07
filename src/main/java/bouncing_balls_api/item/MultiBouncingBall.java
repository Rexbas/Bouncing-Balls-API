package bouncing_balls_api.item;

import bouncing_balls_api.capability.BounceCapabilityProvider;
import bouncing_balls_api.capability.IBounceCapability;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class MultiBouncingBall extends BouncingBall {

	protected int maxConsecutiveBounces;
	protected ItemStack consumptionItem;

	public MultiBouncingBall(Item.Properties itemProperties, BouncingBall.Properties ballProperties, int maxConsecutiveBounces, Item consumptionItem) {
		super(itemProperties, ballProperties);
		this.maxConsecutiveBounces = maxConsecutiveBounces;
		this.consumptionItem = new ItemStack(consumptionItem);
	}

	public MultiBouncingBall(Item.Properties itemProperties, BouncingBall.Properties ballProperties, int maxConsecutiveBounces) {
		this(itemProperties, ballProperties, maxConsecutiveBounces, Items.AIR);
	}

	@Override
	public boolean canBounce(LivingEntity entity) {
		IBounceCapability cap = entity.getCapability(BounceCapabilityProvider.BOUNCE_CAPABILITY).orElse(null);
		if (cap != null) {
			return cap.getConsecutiveBounces() < this.maxConsecutiveBounces && !entity.isInWater() && !entity.isInLava() && hasConsumptionItem(entity);
		}
		return false;
	}

	/**
	 * Add y-motion to the entity and reduce the consumption item if applicable.
	 * 
	 * @param entity  The entity to bounce.
	 * @param motionY The y-motion to add.
	 */
	@Override
	public void bounce(LivingEntity entity, float motionY) {
		super.bounce(entity, motionY);
		if (consumptionItem.getItem() != Items.AIR) {
			entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(itemHandler -> {
				int slot = findConsumptionItemSlot(itemHandler);
				if (slot != -1) {
					itemHandler.extractItem(slot, 1, false);
				}
			});
		}
	}

	@Override
	public float onFall(LivingEntity entity, ItemStack stack, float fallDistance) {
		if (fallDistance > properties.rebounceHeight) {
			float multiplier;
			if (hasConsumptionItem(entity)) {
				bounce(entity, properties.upwardMotion);
				multiplier = properties.damageMultiplier;
			} else {
				super.bounce(entity, properties.upwardMotion / 2);
				multiplier = properties.damageMultiplier * 2 > 1 ? 1 : properties.damageMultiplier * 2;
			}

			damageBall(entity, stack);
			playBounceSound(entity.level, entity);
			return multiplier;
		}
		return 0f;
	}

	protected boolean hasConsumptionItem(LivingEntity entity) {
		if (consumptionItem.getItem() == Items.AIR) {
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
			if (!stack.isEmpty() && stack.getItem() == consumptionItem.getItem() && ItemStack.tagMatches(stack, consumptionItem)) {
				return i;
			}
		}
		return -1;
	}
}