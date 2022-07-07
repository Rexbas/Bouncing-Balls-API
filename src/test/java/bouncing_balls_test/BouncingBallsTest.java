package bouncing_balls_test;

import bouncing_balls_test.init.BouncingBallsTestItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BouncingBallsTest.MODID)
public class BouncingBallsTest {
	public static final String MODID = "bouncing_balls_test";
	
	public static final ItemGroup ITEMGROUP = new ItemGroup(MODID) {
		@Override
		public ItemStack makeIcon() {
			return new ItemStack(BouncingBallsTestItems.NORMAL.get());
		}
	};
	
	public BouncingBallsTest() {
		BouncingBallsTestItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}