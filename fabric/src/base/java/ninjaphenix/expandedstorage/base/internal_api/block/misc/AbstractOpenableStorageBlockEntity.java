package ninjaphenix.expandedstorage.base.internal_api.block.misc;

import com.google.common.base.Suppliers;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import ninjaphenix.container_library.api.OpenableBlockEntity;
import ninjaphenix.container_library.api.inventory.AbstractHandler;
import ninjaphenix.expandedstorage.base.internal_api.block.AbstractOpenableStorageBlock;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Internal
@Experimental
public abstract class AbstractOpenableStorageBlockEntity extends AbstractStorageBlockEntity implements OpenableBlockEntity {
    private final ResourceLocation blockId;
    private final ContainerOpenersCounter observerCounter;
    protected Component menuTitle;
    private int slots;
    private NonNullList<ItemStack> inventory;
    private Supplier<Storage<ItemVariant>> itemStorage;
    private final Supplier<WorldlyContainer> container = Suppliers.memoize(() -> new WorldlyContainer() {
        private final int[] slotsForFace = AbstractOpenableStorageBlockEntity.createSlotsForFaceArray(this.getContainerSize());

        @Override
        public int[] getSlotsForFace(Direction direction) {
            return slotsForFace;
        }

        @Override
        public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
            return true;
        }

        @Override
        public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
            return true;
        }

        @Override
        public int getContainerSize() {
            return slots;
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack stack : inventory) {
                if (!stack.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return inventory.get(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack stack = ContainerHelper.removeItem(inventory, slot, amount);
            if (!stack.isEmpty()) {
                this.setChanged();
            }
            return stack;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return ContainerHelper.takeItem(inventory, slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            inventory.set(slot, stack);
            if (stack.getCount() > this.getMaxStackSize()) {
                stack.setCount(this.getMaxStackSize());
            }
            this.setChanged();
        }

        @Override
        public void setChanged() {
            AbstractOpenableStorageBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return AbstractOpenableStorageBlockEntity.this.canContinueUse(player);
        }

        @Override
        public void clearContent() {
            inventory.clear();
        }

        @Override
        public void startOpen(Player player) {
            AbstractOpenableStorageBlockEntity.this.playerStartUsing(player);
        }

        @Override
        public void stopOpen(Player player) {
            AbstractOpenableStorageBlockEntity.this.playerStopUsing(player);
        }
    });

    private static int[] createSlotsForFaceArray(int containerSize) {
        int[] arr = new int[containerSize];
        for (int i = 0; i < containerSize; i++) {
            arr[i] = i;
        }
        return arr;
    }

    public AbstractOpenableStorageBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, ResourceLocation blockId) {
        super(blockEntityType, pos, state);
        this.observerCounter = new ContainerOpenersCounter() {
            @Override
            protected void onOpen(Level level, BlockPos pos, BlockState state) {
                AbstractOpenableStorageBlockEntity.this.onOpen(level, pos, state);
            }

            @Override
            protected void onClose(Level level, BlockPos pos, BlockState state) {
                AbstractOpenableStorageBlockEntity.this.onClose(level, pos, state);
            }

            @Override
            protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
                AbstractOpenableStorageBlockEntity.this.onObserverCountChanged(level, pos, state, oldCount, newCount);
            }

            @Override
            protected boolean isOwnContainer(Player player) {
                if (player.containerMenu instanceof AbstractHandler menu) {
                    return AbstractOpenableStorageBlockEntity.this.isThis(menu.getInventory());
                } else {
                    return false;
                }
            }
        };
        this.blockId = blockId;
        this.initialise(blockId);
    }

    public static Storage<ItemVariant> createGenericItemStorage(AbstractOpenableStorageBlockEntity entity) {
        return InventoryStorage.of(entity.getContainerWrapper(), null);
    }

    public static Storage<ItemVariant> getItemStorage(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, Direction direction) {
        if (blockEntity != null) {
            var entity = (AbstractOpenableStorageBlockEntity) blockEntity;
            if (entity.itemStorage == null) {
                entity.itemStorage = Suppliers.memoize(() -> entity.createItemStorage(level, state, pos, direction));
            }
            return entity.itemStorage.get();
        }
        return null;

    }

    private void playerStartUsing(Player player) {
        if (!player.isSpectator()) {
            observerCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    private void playerStopUsing(Player player) {
        if (!player.isSpectator()) {
            observerCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    protected boolean isThis(Container container) {
        return container == this.container.get();
    }

    protected void onObserverCountChanged(Level level, BlockPos pos, BlockState state, int i, int j) {

    }

    protected void onOpen(Level level, BlockPos pos, BlockState state) {

    }

    protected void onClose(Level level, BlockPos pos, BlockState state) {

    }

    public final void recountObservers() {
        observerCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
    }

    public WorldlyContainer getContainerWrapper() {
        return container.get();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setBlockState(BlockState state) {
        super.setBlockState(state);
        this.itemStorage = null;
    }

    protected Storage<ItemVariant> createItemStorage(Level level, BlockState state, BlockPos pos, @Nullable Direction side) {
        return AbstractOpenableStorageBlockEntity.createGenericItemStorage(this);
    }

    private void initialise(ResourceLocation blockId) {
        if (Registry.BLOCK.get(blockId) instanceof AbstractOpenableStorageBlock block) {
            slots = block.getSlotCount();
            inventory = NonNullList.withSize(slots, ItemStack.EMPTY);
            menuTitle = block.getMenuTitle();
        }
    }

    @Override
    public Component getDefaultTitle() {
        return menuTitle;
    }

    public final ResourceLocation getBlockId() {
        return blockId;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (this.getBlockState().getBlock() instanceof AbstractOpenableStorageBlock block) {
            this.initialise(block.getBlockId());
            ContainerHelper.loadAllItems(tag, inventory);
        } else {
            throw new IllegalStateException("Block Entity attached to wrong block.");
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        super.save(tag);
        ContainerHelper.saveAllItems(tag, inventory);
        return tag;
    }

    public NonNullList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    public boolean canBeUsedBy(ServerPlayer player) {
        return this.canPlayerInteractWith(player);
    }

    @Override
    public Container getInventory() {
        return this.getContainerWrapper();
    }

    @Override
    public Component getInventoryTitle() {
        return this.getDisplayName();
    }
}
