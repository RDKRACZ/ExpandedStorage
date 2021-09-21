package ninjaphenix.expandedstorage.internal_api.block.misc;

import com.google.common.base.Suppliers;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import ninjaphenix.container_library.api.OpenableBlockEntity;
import ninjaphenix.container_library.api.inventory.AbstractHandler;
import ninjaphenix.expandedstorage.internal_api.block.AbstractOpenableStorageBlock;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Internal
@Experimental
public abstract class AbstractOpenableStorageBlockEntity extends AbstractStorageBlockEntity implements OpenableBlockEntity {
    private final Identifier blockId;
    private final ViewerCountManager observerCounter;
    protected Text menuTitle;
    private int slots;
    private DefaultedList<ItemStack> inventory;
    private Supplier<Storage<ItemVariant>> itemStorage;
    private final Supplier<SidedInventory> container = Suppliers.memoize(() -> new SidedInventory() {
        private final int[] slotsForFace = AbstractOpenableStorageBlockEntity.createSlotsForFaceArray(this.size());

        @Override
        public int[] getAvailableSlots(Direction direction) {
            return slotsForFace;
        }

        @Override
        public boolean canInsert(int i, ItemStack itemStack, @Nullable Direction direction) {
            return true;
        }

        @Override
        public boolean canExtract(int i, ItemStack itemStack, Direction direction) {
            return true;
        }

        @Override
        public int size() {
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
        public ItemStack getStack(int slot) {
            return inventory.get(slot);
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            ItemStack stack = Inventories.splitStack(inventory, slot, amount);
            if (!stack.isEmpty()) {
                this.markDirty();
            }
            return stack;
        }

        @Override
        public ItemStack removeStack(int slot) {
            return Inventories.removeStack(inventory, slot);
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            inventory.set(slot, stack);
            if (stack.getCount() > this.getMaxCountPerStack()) {
                stack.setCount(this.getMaxCountPerStack());
            }
            this.markDirty();
        }

        @Override
        public void markDirty() {
            AbstractOpenableStorageBlockEntity.this.markDirty();
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return AbstractOpenableStorageBlockEntity.this.canContinueUse(player);
        }

        @Override
        public void clear() {
            inventory.clear();
        }

        @Override
        public void onOpen(PlayerEntity player) {
            AbstractOpenableStorageBlockEntity.this.playerStartUsing(player);
        }

        @Override
        public void onClose(PlayerEntity player) {
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

    public AbstractOpenableStorageBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, Identifier blockId) {
        super(blockEntityType, pos, state);
        this.observerCounter = new ViewerCountManager() {
            @Override
            protected void onContainerOpen(World level, BlockPos pos, BlockState state) {
                AbstractOpenableStorageBlockEntity.this.onOpen(level, pos, state);
            }

            @Override
            protected void onContainerClose(World level, BlockPos pos, BlockState state) {
                AbstractOpenableStorageBlockEntity.this.onClose(level, pos, state);
            }

            @Override
            protected void onViewerCountUpdate(World level, BlockPos pos, BlockState state, int oldCount, int newCount) {
                AbstractOpenableStorageBlockEntity.this.onObserverCountChanged(level, pos, state, oldCount, newCount);
            }

            @Override
            protected boolean isPlayerViewing(PlayerEntity player) {
                if (player.currentScreenHandler instanceof AbstractHandler menu) {
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

    public static Storage<ItemVariant> getItemStorage(World level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, Direction direction) {
        if (blockEntity != null) {
            AbstractOpenableStorageBlockEntity entity = (AbstractOpenableStorageBlockEntity) blockEntity;
            if (entity.itemStorage == null) {
                entity.itemStorage = Suppliers.memoize(() -> entity.createItemStorage(level, state, pos, direction));
            }
            return entity.itemStorage.get();
        }
        return null;

    }

    private void playerStartUsing(PlayerEntity player) {
        if (!player.isSpectator()) {
            observerCounter.openContainer(player, this.getWorld(), this.getPos(), this.getCachedState());
        }
    }

    private void playerStopUsing(PlayerEntity player) {
        if (!player.isSpectator()) {
            observerCounter.closeContainer(player, this.getWorld(), this.getPos(), this.getCachedState());
        }
    }

    protected boolean isThis(Inventory container) {
        return container == this.container.get();
    }

    protected void onObserverCountChanged(World level, BlockPos pos, BlockState state, int i, int j) {

    }

    protected void onOpen(World level, BlockPos pos, BlockState state) {

    }

    protected void onClose(World level, BlockPos pos, BlockState state) {

    }

    public final void recountObservers() {
        observerCounter.updateViewerCount(this.getWorld(), this.getPos(), this.getCachedState());
    }

    public SidedInventory getContainerWrapper() {
        return container.get();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setCachedState(BlockState state) {
        super.setCachedState(state);
        this.itemStorage = null;
    }

    protected Storage<ItemVariant> createItemStorage(World level, BlockState state, BlockPos pos, @Nullable Direction side) {
        return AbstractOpenableStorageBlockEntity.createGenericItemStorage(this);
    }

    private void initialise(Identifier blockId) {
        if (Registry.BLOCK.get(blockId) instanceof AbstractOpenableStorageBlock block) {
            slots = block.getSlotCount();
            inventory = DefaultedList.ofSize(slots, ItemStack.EMPTY);
            menuTitle = block.getMenuTitle();
        }
    }

    @Override
    public Text getDefaultTitle() {
        return menuTitle;
    }

    public final Identifier getBlockId() {
        return blockId;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        if (this.getCachedState().getBlock() instanceof AbstractOpenableStorageBlock block) {
            this.initialise(block.getBlockId());
            Inventories.readNbt(tag, inventory);
        } else {
            throw new IllegalStateException("Block Entity attached to wrong block.");
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        Inventories.writeNbt(tag, inventory);
        return tag;
    }

    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    public boolean canBeUsedBy(ServerPlayerEntity player) {
        return this.canPlayerInteractWith(player);
    }

    @Override
    public Inventory getInventory() {
        return this.getContainerWrapper();
    }

    @Override
    public Text getInventoryTitle() {
        return this.getDisplayName();
    }
}
