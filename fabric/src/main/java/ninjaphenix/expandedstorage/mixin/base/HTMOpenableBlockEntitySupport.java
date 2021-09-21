package ninjaphenix.expandedstorage.mixin.base;

import com.github.fabricservertools.htm.HTMContainerLock;
import com.github.fabricservertools.htm.api.LockableObject;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventories;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.AbstractStorageBlockEntity;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.FabricChestProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractOpenableStorageBlockEntity.class)
public abstract class HTMOpenableBlockEntitySupport extends AbstractStorageBlockEntity implements LockableObject {
    private HTMContainerLock htmLock = new HTMContainerLock();

    public HTMOpenableBlockEntitySupport(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    @Inject(method = "readNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventories;loadAllItems(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/util/collection/DefaultedList;)V"))
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
    public boolean canPlayerInteractWith(ServerPlayerEntity player) {
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
