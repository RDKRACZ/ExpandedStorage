package ninjaphenix.expandedstorage.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import ninjaphenix.expandedstorage.block.OpenableBlock;
import ninjaphenix.expandedstorage.block.entity.extendable.InventoryBlockEntity;
import ninjaphenix.expandedstorage.block.entity.extendable.OpenableBlockEntity;
import ninjaphenix.expandedstorage.block.strategies.ItemAccess;
import ninjaphenix.expandedstorage.block.strategies.Lockable;
import ninjaphenix.expandedstorage.block.strategies.Nameable;
import ninjaphenix.expandedstorage.block.strategies.Observable;

import java.util.function.Function;

public class OldChestBlockEntity extends InventoryBlockEntity {
    public OldChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Identifier blockId,
                               Function<OpenableBlockEntity, ItemAccess> access, Function<OpenableBlockEntity, Lockable> lockable) {
        super(type, pos, state, blockId);
        this.setItemAccess(access.apply(this));
        this.setLock(lockable.apply(this));
        this.setName(new Nameable.Mutable(((OpenableBlock) state.getBlock()).getInventoryTitle()));
        this.setObservable(Observable.NOT);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    @Override
    protected boolean shouldStateUpdateInvalidateItemAccess(BlockState oldState, BlockState state) {
        return false;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return null;
    }
}
