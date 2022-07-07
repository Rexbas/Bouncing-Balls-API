package bouncing_balls_test.init;

import bouncing_balls_api.item.BouncingBall;
import bouncing_balls_api.item.MultiBouncingBall;
import bouncing_balls_test.BouncingBallsTest;
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