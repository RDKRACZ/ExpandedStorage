/**
 * Copyright 2021 NinjaPhenix
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ninjaphenix.expandedstorage.block.misc;

import com.google.common.base.Suppliers;
import ninjaphenix.container_library.api.inventory.AbstractHandler;
import ninjaphenix.container_library.api.v2.OpenableBlockEntityV2;
import ninjaphenix.expandedstorage.block.AbstractOpenableStorageBlock;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
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
import net.minecraft.world.phys.Vec3;

@Internal
@Experimental
public abstract class AbstractOpenableStorageBlockEntity extends AbstractStorageBlockEntity implements OpenableBlockEntityV2 {
    private final ResourceLocation blockId;
    private final ContainerOpenersCounter observerCounter;
    protected Component defaultTitle;
    private int slots;
    private NonNullList<ItemStack> items;
    protected Supplier<Object> itemAccess;
    private final Supplier<WorldlyContainer> inventory = Suppliers.memoize(() -> new WorldlyContainer() {
        private final int[] availableSlots = AbstractOpenableStorageBlockEntity.createAvailableSlots(this.getContainerSize());

        @Override
        public int[] getSlotsForFace(Direction direction) {
            return availableSlots;
        }

        @Override
        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
            return true;
        }

        @Override
        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
            return true;
        }

        @Override
        public int getContainerSize() {
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
        public ItemStack getItem(int slot) {
            return items.get(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
            if (!stack.isEmpty()) {
                this.setChanged();
            }
            return stack;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return ContainerHelper.takeItem(items, slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            items.set(slot, stack);
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
            return AbstractOpenableStorageBlockEntity.this.getLevel().getBlockEntity(AbstractOpenableStorageBlockEntity.this.getBlockPos()) == AbstractOpenableStorageBlockEntity.this && player.distanceToSqr(Vec3.atCenterOf(AbstractOpenableStorageBlockEntity.this.getBlockPos())) <= 64.0D;
        }

        @Override
        public void clearContent() {
            items.clear();
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

    private static int[] createAvailableSlots(int inventorySize) {
        int[] arr = new int[inventorySize];
        for (int i = 0; i < inventorySize; i++) {
            arr[i] = i;
        }
        return arr;
    }

    public AbstractOpenableStorageBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, ResourceLocation blockId, boolean observable) {
        super(blockEntityType, pos, state);
        this.observerCounter = !observable ? null : new ContainerOpenersCounter() {
            @Override
            protected void onOpen(Level world, BlockPos pos, BlockState state) {
                AbstractOpenableStorageBlockEntity.this.onOpen(world, pos, state);
            }

            @Override
            protected void onClose(Level world, BlockPos pos, BlockState state) {
                AbstractOpenableStorageBlockEntity.this.onClose(world, pos, state);
            }

            @Override
            protected void openerCountChanged(Level world, BlockPos pos, BlockState state, int oldCount, int newCount) {
                AbstractOpenableStorageBlockEntity.this.onObserverCountChanged(world, pos, state, oldCount, newCount);
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

    public static Object getItemAccess(Level world, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, Direction direction) {
        if (blockEntity != null) {
            AbstractOpenableStorageBlockEntity entity = (AbstractOpenableStorageBlockEntity) blockEntity;
            if (entity.itemAccess == null) {
                entity.itemAccess = Suppliers.memoize(() -> entity.createItemAccess(world, state, pos, direction));
            }
            return entity.itemAccess.get();
        }
        return null;

    }

    protected Object createItemAccess(Level world, BlockState state, BlockPos pos, @Nullable Direction side) {
        return PlatformUtils.getInstance().createGenericItemAccess(this);
    }

    private void playerStartUsing(Player player) {
        if (!player.isSpectator() && observerCounter != null) {
            observerCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    private void playerStopUsing(Player player) {
        if (!player.isSpectator() && observerCounter != null) {
            observerCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    protected boolean isThis(Container inventory) {
        return inventory == this.inventory.get();
    }

    protected void onObserverCountChanged(Level world, BlockPos pos, BlockState state, int oldCount, int newCount) {

    }

    protected void onOpen(Level world, BlockPos pos, BlockState state) {

    }

    protected void onClose(Level world, BlockPos pos, BlockState state) {

    }

    public final void recountObservers() {
        observerCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
    }

    private void initialise(ResourceLocation blockId) {
        if (Registry.BLOCK.get(blockId) instanceof AbstractOpenableStorageBlock block) {
            slots = block.getSlotCount();
            items = NonNullList.withSize(slots, ItemStack.EMPTY);
            defaultTitle = block.getInventoryTitle();
        }
    }

    @Override
    public Component getDefaultTitle() {
        return defaultTitle;
    }

    public final ResourceLocation getBlockId() {
        return blockId;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (this.getBlockState().getBlock() instanceof AbstractOpenableStorageBlock block) {
            this.initialise(block.getBlockId());
            ContainerHelper.loadAllItems(tag, items);
        } else {
            throw new IllegalStateException("Block Entity attached to wrong block.");
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        super.save(tag);
        ContainerHelper.saveAllItems(tag, items);
        return tag;
    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public boolean canBeUsedBy(ServerPlayer player) {
        return this.getInventory().stillValid(player) && this.usableBy(player);
    }

    @Override
    public WorldlyContainer getInventory() {
        return inventory.get();
    }

    @Override
    public Component getInventoryTitle() {
        return this.getTitle();
    }
}
