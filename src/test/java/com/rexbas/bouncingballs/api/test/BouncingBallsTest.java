package com.rexbas.bouncingballs.api.test;

import com.rexbas.bouncingballs.api.test.init.BouncingBallsTestItems;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BouncingBallsTest.MODID)
public class BouncingBallsTest {
	public static final String MODID = "bouncingballs_api_test";
	
	public BouncingBallsTest() {
		BouncingBallsTestItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
		BouncingBallsTestItems.CREATIVE_TABS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}