package pickaxe;

import pickaxe.item.ModItems;
import pickaxe.item.custom.SuperPickaxeItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Superpickaxe implements ModInitializer {
	public static final String MOD_ID = "superpickaxe";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.register();

		// LEFT CLICK on a block = 3x3x4 blast
		AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
			if (level.isClientSide()) return InteractionResult.PASS;
			var stack = player.getItemInHand(hand);
			if (!(stack.getItem() instanceof SuperPickaxeItem item)) return InteractionResult.PASS;
			item.triggerBlast(level, player, stack, hand);
			return InteractionResult.SUCCESS; // cancels vanilla hit
		});

		// Disable normal hold-to-mine while holding the super pickaxe
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, be) -> {
			if (SuperPickaxeItem.isBlasting) return true; // allow our own blast breaks
			return !(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SuperPickaxeItem);
		});

		LOGGER.info("[SuperPickaxe] Loaded!");
	}
}
