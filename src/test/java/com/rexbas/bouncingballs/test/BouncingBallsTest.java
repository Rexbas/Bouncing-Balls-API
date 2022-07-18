package com.rexbas.bouncingballs.test;

import com.rexbas.bouncingballs.test.init.BouncingBallsTestItems;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BouncingBallsTest.MODID)
public class BouncingBallsTest {
	public static final String MODID = "bouncingballs_test";
	
	public static final ItemGroup ITEMGROUP = new ItemGroup(MODID) {
		@Override
		public ItemStack makeIcon() {
			return new ItemStack(BouncingBallsTestItems.NORMAL.get());
		}
	};
	
	public BouncingBallsTest() {
		BouncingBallsTestItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
	@Mod.EventBusSubscriber(modid = BouncingBallsTest.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Events {
    	
    	/*@SubscribeEvent
		public static void onModelBakeEvent(ModelBakeEvent event) {
    		BouncingBallsTestItems.ITEMS.getEntries().forEach((ball) -> {
    			ModelResourceLocation modelLocation = new ModelResourceLocation(ball.get().getRegistryName(), "inventory");
    			IBakedModel model = event.getModelRegistry().get(modelLocation);
    			if (model != null && !(model instanceof BouncingBallModel)) {
    				BouncingBallModel newModel = new BouncingBallModel(model, new BouncingBallItemOverrideList(new ModelResourceLocation(ball.get().getRegistryName() + "_active", "inventory")));
    				event.getModelRegistry().put(modelLocation, newModel);
    			}
    		});
		}
    	
    	@SubscribeEvent
        public static void onModelRegister(ModelRegistryEvent event) {
    		BouncingBallsTestItems.ITEMS.getEntries().forEach((ball) -> {
        		ModelLoader.addSpecialModel(new ModelResourceLocation(ball.get().getRegistryName() + "_active", "inventory"));
    		});
        }*/
	}	
}