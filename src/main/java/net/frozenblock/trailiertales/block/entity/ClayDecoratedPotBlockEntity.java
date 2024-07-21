package net.frozenblock.trailiertales.block.entity;

import java.util.List;
import java.util.Optional;
import net.frozenblock.trailiertales.registry.RegisterBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

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

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public CompoundTag getUpdateTag(HolderLookup.Provider lookupProvider) {
		return this.saveCustomOnly(lookupProvider);
	}

	public Direction getDirection() {
		return this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
	}

	public PotDecorations getDecorations() {
		return this.decorations;
	}

	public boolean addDecoration(Item newSherd, int index) {
		List<Item> sherds = getDecorations().ordered();
		if (index >= 4) return false;
//		Optional<Item> back, left, right, front;
		Optional<Item>[] items = new Optional[4];
		for (int i = 0; i < sherds.size(); i++) {
			if (i == index) {
				items[i] = Optional.ofNullable(newSherd);
				continue;
			}
			if (sherds.get(i).equals(Items.BRICK)) {
				items[i] = Optional.empty();
				continue;
			}
			items[i] = Optional.ofNullable(sherds.get(i));
		}
		decorations = new PotDecorations(items[0], items[1], items[2], items[3]);
		return true;
	}

	public ItemStack getBakedPotItem() {
		ItemStack itemStack = Items.DECORATED_POT.getDefaultInstance();
		itemStack.applyComponents(this.collectComponents());
		return itemStack;
	}

	protected void collectImplicitComponents(DataComponentMap.Builder builder) {
		super.collectImplicitComponents(builder);
		builder.set(DataComponents.POT_DECORATIONS, this.decorations);
	}

	protected void applyImplicitComponents(BlockEntity.DataComponentInput dataComponentInput) {
		super.applyImplicitComponents(dataComponentInput);
		this.decorations = (PotDecorations)dataComponentInput.getOrDefault(DataComponents.POT_DECORATIONS, PotDecorations.EMPTY);
	}

	public void removeComponentsFromTag(CompoundTag nbt) {
		super.removeComponentsFromTag(nbt);
		nbt.remove("sherds");
	}
}
