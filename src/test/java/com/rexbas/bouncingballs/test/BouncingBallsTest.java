package com.rexbas.bouncingballs.test;

import com.rexbas.bouncingballs.test.init.BouncingBallsTestItems;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BouncingBallsTest.MODID)
public class BouncingBallsTest {
	public static final String MODID = "bouncingballs_test";
	
	public BouncingBallsTest() {
		BouncingBallsTestItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}