package net.frozenblock.trailiertales.registry;

import net.frozenblock.trailiertales.block.entity.ClayDecoratedPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RegisterDispenserBehavior {
	// Every sherd in the game
	public static Item[] sherds = {Items.ANGLER_POTTERY_SHERD, Items.ARCHER_POTTERY_SHERD, Items.ARMS_UP_POTTERY_SHERD, Items.BLADE_POTTERY_SHERD, Items.BREWER_POTTERY_SHERD, Items.BURN_POTTERY_SHERD, Items.DANGER_POTTERY_SHERD, Items.EXPLORER_POTTERY_SHERD, Items.FLOW_POTTERY_SHERD, Items.FRIEND_POTTERY_SHERD, Items.GUSTER_POTTERY_SHERD, Items.HEART_POTTERY_SHERD, Items.HEARTBREAK_POTTERY_SHERD, Items.HOWL_POTTERY_SHERD, Items.MINER_POTTERY_SHERD, Items.MOURNER_POTTERY_SHERD, Items.PLENTY_POTTERY_SHERD, Items.PRIZE_POTTERY_SHERD, Items.SCRAPE_POTTERY_SHERD, Items.SHEAF_POTTERY_SHERD, Items.SHELTER_POTTERY_SHERD, Items.SKULL_POTTERY_SHERD, Items.SNORT_POTTERY_SHERD, RegisterItems.BULLSEYE_POTTERY_SHERD, RegisterItems.WITHER_POTTERY_SHERD, RegisterItems.BLOOM_POTTERY_SHERD, RegisterItems.INCIDENCE_POTTERY_SHERD, RegisterItems.CULTIVATOR_POTTERY_SHERD, RegisterItems.SPADE_POTTERY_SHERD};

	public static void bootstrap() {
		OptionalDispenseItemBehavior sherdDispenserBehavior = new OptionalDispenseItemBehavior() {
			@Override
			protected ItemStack execute(BlockSource pointer, ItemStack stack) {
				Level level = pointer.level();
				BlockPos potPos = pointer.pos().relative(pointer.state().getValue(DispenserBlock.FACING));
				BlockState potState = level.getBlockState(potPos);
				BlockState dispenserState = level.getBlockState(pointer.pos());
				Direction imprintDirection = dispenserState.getValue(DispenserBlock.FACING).getOpposite();
				if (potState.is(RegisterBlocks.CLAY_DECORATED_POT)) {
					if (level.getBlockEntity(potPos) instanceof ClayDecoratedPotBlockEntity blockEntity) {
						blockEntity.addDecoration(stack.getItem(), potState, imprintDirection);
						blockEntity.setChanged();
					}
					return stack;
				}
				return super.execute(pointer, stack);
			}
		};

		for (Item sherd : sherds) {
			DispenserBlock.registerBehavior(sherd, sherdDispenserBehavior);
		}
	}
}
