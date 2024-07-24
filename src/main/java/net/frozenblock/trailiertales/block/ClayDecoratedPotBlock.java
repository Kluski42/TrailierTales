package net.frozenblock.trailiertales.block;

import com.mojang.serialization.MapCodec;
import net.frozenblock.trailiertales.block.entity.ClayDecoratedPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import static net.minecraft.world.level.block.CampfireBlock.LIT;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ClayDecoratedPotBlock extends DecoratedPotBlock {
	public static final MapCodec<DecoratedPotBlock> CODEC = simpleCodec(DecoratedPotBlock::new);
	private static final BooleanProperty BAKING = BooleanProperty.create("baking");

	public MapCodec<DecoratedPotBlock> codec() {
		return CODEC;
	}

	public ClayDecoratedPotBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(getStateDefinition().any().setValue(BAKING, false));
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
		if (direction == Direction.DOWN) {
			state = state.setValue(BAKING, isActiveFire(neighborState));
		}
		return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
	}

	private boolean isActiveFire(BlockState state) {
		if (state.is(BlockTags.FIRE)) return true;
		if (state.is(BlockTags.CAMPFIRES) && state.hasProperty(LIT)) {
			return state.getValue(LIT);
		}
		if (state.is(Blocks.LAVA)) return true;
		return false;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		LevelAccessor levelAccessor = ctx.getLevel();
		BlockPos blockPos = ctx.getClickedPos();
		return super.getStateForPlacement(ctx).setValue(BAKING, isActiveFire(levelAccessor.getBlockState(blockPos.below())));
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player entity, InteractionHand hand, BlockHitResult hitResult) {
		if (world.getBlockEntity(pos) instanceof ClayDecoratedPotBlockEntity clayPotBlockEntity) {
			if (stack.is(ItemTags.DECORATED_POT_SHERDS)) {
				entity.awardStat(Stats.ITEM_USED.get(stack.getItem()));
				Direction direction = hitResult.getDirection();
				if (direction.get2DDataValue() != -1) {
					clayPotBlockEntity.addDecoration(stack.getItem(), state, direction);
					clayPotBlockEntity.setChanged();
					return ItemInteractionResult.SUCCESS;
				}
			}
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player entity, BlockHitResult hitResult) {
		return InteractionResult.PASS;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BAKING);
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return state.getValue(BAKING);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if (state.getValue(BAKING)) {
			bakePot(state, world, pos);
		}
		super.randomTick(state, world, pos, random);
	}

	private void bakePot(BlockState state, ServerLevel world, BlockPos pos) {
		if (!(world.getBlockEntity(pos) instanceof ClayDecoratedPotBlockEntity clayPotBlockEntity)) {
			return;
		}
		ItemStack potItemStack = clayPotBlockEntity.getBakedPotItem();
		world.setBlock(pos, getBakedEquivalent(state), 11);
		if (world.getBlockEntity(pos) instanceof DecoratedPotBlockEntity decoratedPotBlockEntity) {
			// Sculk block change signal
			decoratedPotBlockEntity.setFromItem(potItemStack);
			decoratedPotBlockEntity.setChanged();
		}
	}

	private BlockState getBakedEquivalent(BlockState state) {
		Direction horiztonalFacing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
		boolean cracked = state.getValue(BlockStateProperties.CRACKED);
		boolean waterlogged = state.getValue(BlockStateProperties.WATERLOGGED);
		return Blocks.DECORATED_POT.defaultBlockState()
			.setValue(BlockStateProperties.HORIZONTAL_FACING, horiztonalFacing)
			.setValue(BlockStateProperties.CRACKED, cracked)
			.setValue(BlockStateProperties.WATERLOGGED, waterlogged);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ClayDecoratedPotBlockEntity(pos, state);
	}
}
