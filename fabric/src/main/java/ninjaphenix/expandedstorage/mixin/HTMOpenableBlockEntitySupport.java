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
package ninjaphenix.expandedstorage.mixin;

import com.github.fabricservertools.htm.HTMContainerLock;
import com.github.fabricservertools.htm.api.LockableObject;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import ninjaphenix.expandedstorage.FabricChestProperties;
import ninjaphenix.expandedstorage.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.block.misc.AbstractAccessibleStorageBlockEntity;
import ninjaphenix.expandedstorage.block.misc.AbstractOpenableStorageBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// todo: find alternative for this, mixins on mod classes aren't allowed but work.
@Mixin(AbstractOpenableStorageBlockEntity.class)
public abstract class HTMOpenableBlockEntitySupport extends AbstractAccessibleStorageBlockEntity<AbstractChestBlock<?>> implements LockableObject {
    private HTMContainerLock htmLock = new HTMContainerLock();

    public HTMOpenableBlockEntitySupport(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, Identifier blockId) {
        super(blockEntityType, pos, state, blockId);
    }

    @Inject(method = "readNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventories;readNbt(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/util/collection/DefaultedList;)V"))
    private void readHTMLock(NbtCompound tag, CallbackInfo ci) {
        if (tag.contains(FabricChestProperties.LOCK_TAG_KEY, NbtElement.COMPOUND_TYPE)) {
            htmLock.fromTag(tag.getCompound(FabricChestProperties.LOCK_TAG_KEY));
        }
    }

    @Inject(method = "writeNbt(Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/nbt/NbtCompound;", at = @At("RETURN"), cancellable = true)
    private void writeHTMLock(NbtCompound tag, CallbackInfoReturnable<NbtCompound> cir) {
        NbtCompound subTag = new NbtCompound();
        htmLock.toTag(subTag);
        tag.put(FabricChestProperties.LOCK_TAG_KEY, subTag);
        cir.setReturnValue(tag);
    }

    @Override
    public boolean usableBy(ServerPlayerEntity player) {
        return htmLock.canOpen(player);
    }

    @Override
    public HTMContainerLock getLock() {
        return htmLock;
    }

    @Override
    public void setLock(HTMContainerLock lock) {
        htmLock = lock;
    }
}
