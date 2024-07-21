package net.frozenblock.trailiertales.block;

import com.mojang.serialization.MapCodec;
import net.frozenblock.trailiertales.block.entity.ClayDecoratedPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ClayDecoratedPotBlock extends DecoratedPotBlock {
	public static final MapCodec<DecoratedPotBlock> CODEC = simpleCodec(DecoratedPotBlock::new);

	public MapCodec<DecoratedPotBlock> codec() {
		return CODEC;
	}

	public ClayDecoratedPotBlock(Properties settings) {
		super(settings);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player entity, InteractionHand hand, BlockHitResult hitResult) {
		if (world.getBlockEntity(pos) instanceof ClayDecoratedPotBlockEntity clayPotBlockEntity) {
			if (stack.is(ItemTags.DECORATED_POT_SHERDS)) {
				entity.awardStat(Stats.ITEM_USED.get(stack.getItem()));
				ItemStack itemStack2 = stack.consumeAndReturn(1, entity);
				clayPotBlockEntity.addDecoration(itemStack2.getItem(), 0);
				clayPotBlockEntity.setChanged();
				return ItemInteractionResult.SUCCESS;
			} else {
				return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			}
		}
		return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player entity, BlockHitResult hitResult) {
		return InteractionResult.PASS;
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return true;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		bakePot(state, world, pos);
		super.randomTick(state, world, pos, random);
	}

	private void bakePot(BlockState state, ServerLevel world, BlockPos pos) {
		if (!(world.getBlockEntity(pos) instanceof ClayDecoratedPotBlockEntity clayPotBlockEntity)) {
			return;
		}
		ItemStack potItemStack = clayPotBlockEntity.getBakedPotItem();
		world.setBlock(pos, Blocks.DECORATED_POT.defaultBlockState(), 11);
		if (world.getBlockEntity(pos) instanceof DecoratedPotBlockEntity decoratedPotBlockEntity) {
			decoratedPotBlockEntity.setFromItem(potItemStack);
			decoratedPotBlockEntity.setChanged();
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ClayDecoratedPotBlockEntity(pos, state);
	}
}
