package com.rexbas.bouncingballs.test.init;

import com.rexbas.bouncingballs.api.item.BouncingBall;
import com.rexbas.bouncingballs.test.BouncingBallsTest;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = BouncingBallsTest.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BouncingBallsTestItems {
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BouncingBallsTest.MODID);
	
	public static final RegistryObject<Item> NORMAL = ITEMS.register("normal", () -> new BouncingBall(new Item.Properties().tab(BouncingBallsTest.ITEMGROUP), new BouncingBall.Properties().addFluid(FluidTags.WATER)));
	public static final RegistryObject<Item> MULTI = ITEMS.register("multi", () -> new BouncingBall(new Item.Properties().tab(BouncingBallsTest.ITEMGROUP), new BouncingBall.Properties(100, Items.DIAMOND, 0.5f, 0.65f, 12f, 0.3f, false, 5, Items.GUNPOWDER).addFluid(FluidTags.WATER).recipeItem(Items.BLUE_DYE)));
}