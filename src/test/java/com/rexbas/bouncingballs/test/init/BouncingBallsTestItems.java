package com.rexbas.bouncingballs.test.init;

import com.rexbas.bouncingballs.api.item.BouncingBall;
import com.rexbas.bouncingballs.api.item.MultiBouncingBall;
import com.rexbas.bouncingballs.test.BouncingBallsTest;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = BouncingBallsTest.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BouncingBallsTestItems {
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BouncingBallsTest.MODID);
	
	public static final RegistryObject<Item> NORMAL = ITEMS.register("normal", () -> new BouncingBall(new Item.Properties().tab(BouncingBallsTest.ITEMGROUP), new BouncingBall.Properties()));
	public static final RegistryObject<Item> MULTI = ITEMS.register("multi", () -> new MultiBouncingBall(new Item.Properties().tab(BouncingBallsTest.ITEMGROUP), new BouncingBall.Properties(100, Items.DIAMOND, 0.5f, 0.65f, 12f, 0.3f), 5, Items.GUNPOWDER));
}