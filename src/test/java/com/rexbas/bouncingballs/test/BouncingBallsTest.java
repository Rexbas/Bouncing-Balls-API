package com.rexbas.bouncingballs.test;

import com.rexbas.bouncingballs.test.init.BouncingBallsTestItems;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BouncingBallsTest.MODID)
public class BouncingBallsTest {
	public static final String MODID = "bouncingballs_test";
	
	public static final CreativeModeTab ITEMGROUP = new CreativeModeTab(MODID) {
		@Override
		public ItemStack makeIcon() {
			return new ItemStack(BouncingBallsTestItems.NORMAL.get());
		}
	};
	
	public BouncingBallsTest() {
		BouncingBallsTestItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}