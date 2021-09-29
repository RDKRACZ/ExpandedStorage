package ninjaphenix.expandedstorage.block.misc;

import com.google.common.base.Suppliers;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import ninjaphenix.container_library.api.inventory.AbstractHandler;
import ninjaphenix.container_library.api.v2.OpenableBlockEntityV2;
import ninjaphenix.expandedstorage.block.AbstractOpenableStorageBlock;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Internal
@Experimental
public abstract class AbstractOpenableStorageBlockEntity extends AbstractStorageBlockEntity implements OpenableBlockEntityV2 {
    private final Identifier blockId;
    private final ViewerCountManager observerCounter;
    protected Text defaultTitle;
    private int slots;
    private DefaultedList<ItemStack> items;
    protected Supplier<Object> itemAccess;
    private final Supplier<SidedInventory> inventory = Suppliers.memoize(() -> new SidedInventory() {
        private final int[] availableSlots = AbstractOpenableStorageBlockEntity.createAvailableSlots(this.size());

        @Override
        public int[] getAvailableSlots(Direction direction) {
            return availableSlots;
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction direction) {
            return true;
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction direction) {
            return true;
        }

        @Override
        public int size() {
            return slots;
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getStack(int slot) {
            return items.get(slot);
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            ItemStack stack = Inventories.splitStack(items, slot, amount);
            if (!stack.isEmpty()) {
                this.markDirty();
            }
            return stack;
        }

        @Override
        public ItemStack removeStack(int slot) {
            return Inventories.removeStack(items, slot);
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            items.set(slot, stack);
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
            return AbstractOpenableStorageBlockEntity.this.getWorld().getBlockEntity(AbstractOpenableStorageBlockEntity.this.getPos()) == AbstractOpenableStorageBlockEntity.this && player.squaredDistanceTo(Vec3d.ofCenter(AbstractOpenableStorageBlockEntity.this.getPos())) <= 64.0D;
        }

        @Override
        public void clear() {
            items.clear();
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

    private static int[] createAvailableSlots(int inventorySize) {
        int[] arr = new int[inventorySize];
        for (int i = 0; i < inventorySize; i++) {
            arr[i] = i;
        }
        return arr;
    }

    public AbstractOpenableStorageBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, Identifier blockId) {
        super(blockEntityType, pos, state);
        this.observerCounter = new ViewerCountManager() {
            @Override
            protected void onContainerOpen(World world, BlockPos pos, BlockState state) {
                AbstractOpenableStorageBlockEntity.this.onOpen(world, pos, state);
            }

            @Override
            protected void onContainerClose(World world, BlockPos pos, BlockState state) {
                AbstractOpenableStorageBlockEntity.this.onClose(world, pos, state);
            }

            @Override
            protected void onViewerCountUpdate(World world, BlockPos pos, BlockState state, int oldCount, int newCount) {
                AbstractOpenableStorageBlockEntity.this.onObserverCountChanged(world, pos, state, oldCount, newCount);
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

    public static Object getItemAccess(World world, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, Direction direction) {
        if (blockEntity != null) {
            AbstractOpenableStorageBlockEntity entity = (AbstractOpenableStorageBlockEntity) blockEntity;
            if (entity.itemAccess == null) {
                entity.itemAccess = Suppliers.memoize(() -> entity.createItemAccess(world, state, pos, direction));
            }
            return entity.itemAccess.get();
        }
        return null;

    }

    protected Object createItemAccess(World world, BlockState state, BlockPos pos, @Nullable Direction side) {
        return PlatformUtils.getInstance().createGenericItemAccess(this);
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

    protected boolean isThis(Inventory inventory) {
        return inventory == this.inventory.get();
    }

    protected void onObserverCountChanged(World world, BlockPos pos, BlockState state, int oldCount, int newCount) {

    }

    protected void onOpen(World world, BlockPos pos, BlockState state) {

    }

    protected void onClose(World world, BlockPos pos, BlockState state) {

    }

    public final void recountObservers() {
        observerCounter.updateViewerCount(this.getWorld(), this.getPos(), this.getCachedState());
    }

    private void initialise(Identifier blockId) {
        if (Registry.BLOCK.get(blockId) instanceof AbstractOpenableStorageBlock block) {
            slots = block.getSlotCount();
            items = DefaultedList.ofSize(slots, ItemStack.EMPTY);
            defaultTitle = block.getInventoryTitle();
        }
    }

    @Override
    public Text getDefaultTitle() {
        return defaultTitle;
    }

    public final Identifier getBlockId() {
        return blockId;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        if (this.getCachedState().getBlock() instanceof AbstractOpenableStorageBlock block) {
            this.initialise(block.getBlockId());
            Inventories.readNbt(tag, items);
        } else {
            throw new IllegalStateException("Block Entity attached to wrong block.");
        }
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        Inventories.writeNbt(tag, items);
    }

    public DefaultedList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public boolean canBeUsedBy(ServerPlayerEntity player) {
        return this.getInventory().canPlayerUse(player) && this.usableBy(player);
    }

    @Override
    public SidedInventory getInventory() {
        return inventory.get();
    }

    @Override
    public Text getInventoryTitle() {
        return this.getTitle();
    }
}
