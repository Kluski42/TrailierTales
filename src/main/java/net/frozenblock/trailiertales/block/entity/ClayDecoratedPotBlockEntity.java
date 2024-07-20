package net.frozenblock.trailiertales.block.entity;

import net.frozenblock.trailiertales.registry.RegisterBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.state.BlockState;

public class ClayDecoratedPotBlockEntity extends BlockEntity {
	public static final String TAG_SHERDS = "sherds";
	private PotDecorations decorations;

	public ClayDecoratedPotBlockEntity(BlockPos pos, BlockState state) {
		super(RegisterBlockEntities.CLAY_DECORATED_POT, pos, state);
		this.decorations = PotDecorations.EMPTY;
	}

	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider lookupProvider) {
		super.saveAdditional(nbt, lookupProvider);
		this.decorations.save(nbt);
	}

	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider lookupProvider) {
		super.loadAdditional(nbt, lookupProvider);
		this.decorations = PotDecorations.load(nbt);
	}

	public PotDecorations getDecorations() {
		return this.decorations;
	}
}
