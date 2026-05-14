package pickaxe.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SuperPickaxeItem extends Item {

	// Flag so our own destroyBlock calls aren't blocked by the BEFORE event
	public static volatile boolean isBlasting = false;

	private static final Map<String, Long> cooldowns = new HashMap<>();
	private static final long COOLDOWN_MS = 1000L;

	private static final Set<String> COLLECTIBLE = Set.of(
		"minecraft:diamond",      "minecraft:raw_iron",
		"minecraft:raw_gold",     "minecraft:raw_copper",
		"minecraft:emerald",      "minecraft:coal",
		"minecraft:lapis_lazuli", "minecraft:redstone",
		"minecraft:quartz",       "minecraft:amethyst_shard",
		"minecraft:ancient_debris"
	);

	private static final Set<String> SAFE = Set.of(
		"minecraft:bedrock",       "minecraft:water",
		"minecraft:lava",          "minecraft:chest",
		"minecraft:trapped_chest", "minecraft:ender_chest",
		"minecraft:barrel",        "minecraft:barrier"
	);

	public SuperPickaxeItem(Properties properties) {
		super(properties);
	}

	// Called by AttackBlockCallback (left-click) in Superpickaxe.java
	public void triggerBlast(Level level, Player player, ItemStack stack, InteractionHand hand) {
		String uid = player.getStringUUID();
		long now = System.currentTimeMillis();
		Long last = cooldowns.get(uid);

		if (last != null && (now - last) < COOLDOWN_MS) {
			double secs = (COOLDOWN_MS - (now - last)) / 1000.0;
			player.displayClientMessage(
				Component.literal(String.format("§cCooldown: %.1fs", secs)), true
			);
			return;
		}
		cooldowns.put(uid, now);

		int broken = blast3x3x4(level, player, player.getDirection());
		player.displayClientMessage(
			Component.literal("§d\uD83D\uDCA5 Super Blast! §eMined " + broken + " blocks"), true
		);
		stack.hurtAndBreak(1, player, hand);
	}

	// 3 wide x 3 tall x 4 deep
	private int blast3x3x4(Level level, Player player, Direction facing) {
		BlockPos origin = player.blockPosition();
		int[] f = fwd(facing);
		int[] s = side(facing);
		int count = 0;

		isBlasting = true;
		try {
			for (int d = 1; d <= 4; d++)
				for (int a = -1; a <= 1; a++)
					for (int b = -1; b <= 1; b++)
						if (mine(level, player, new BlockPos(
							origin.getX() + f[0]*d + s[0]*a,
							origin.getY() + b,
							origin.getZ() + f[2]*d + s[2]*a
						))) count++;
		} finally {
			isBlasting = false;
		}

		autoCollect(level, player, origin, 8);
		return count;
	}

	private boolean mine(Level level, Player player, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.isAir()) return false;
		if (!state.getFluidState().isEmpty()) return false;
		Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
		if (id == null) return false;
		String idStr = id.toString();
		if (SAFE.contains(idStr) || idStr.contains("shulker_box")) return false;
		level.destroyBlock(pos, true, player);
		return true;
	}

	private void autoCollect(Level level, Player player, BlockPos origin, int r) {
		double cx = origin.getX() + 0.5, cy = origin.getY() + 0.5, cz = origin.getZ() + 0.5;
		AABB box = new AABB(cx-r, cy-r, cz-r, cx+r, cy+r, cz+r);
		for (ItemEntity ie : level.getEntitiesOfClass(ItemEntity.class, box, e -> true)) {
			ItemStack drop = ie.getItem();
			if (drop.isEmpty()) continue;
			Identifier id = BuiltInRegistries.ITEM.getKey(drop.getItem());
			if (id != null && COLLECTIBLE.contains(id.toString()) && player.addItem(drop.copy()))
				ie.discard();
		}
	}

	private int[] fwd(Direction d) {
		return new int[]{ d.getStepX(), 0, d.getStepZ() };
	}

	private int[] side(Direction d) {
		return (d == Direction.NORTH || d == Direction.SOUTH)
			? new int[]{1, 0, 0} : new int[]{0, 0, 1};
	}
}
