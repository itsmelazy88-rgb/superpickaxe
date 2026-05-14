package pickaxe.item;

import pickaxe.Superpickaxe;
import pickaxe.item.custom.SuperPickaxeItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class ModItems {

	public static final ResourceKey<Item> SUPER_PICKAXE_KEY = ResourceKey.create(
		Registries.ITEM,
		Identifier.fromNamespaceAndPath(Superpickaxe.MOD_ID, "super_pickaxe")
	);

	public static final Item SUPER_PICKAXE = new SuperPickaxeItem(
		new Item.Properties()
			.durability(4096)
			.fireResistant()
			.stacksTo(1)
			.setId(SUPER_PICKAXE_KEY)
	);

	public static void register() {
		Registry.register(BuiltInRegistries.ITEM, SUPER_PICKAXE_KEY, SUPER_PICKAXE);
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
			.register(entries -> entries.accept(SUPER_PICKAXE));
	}
}
