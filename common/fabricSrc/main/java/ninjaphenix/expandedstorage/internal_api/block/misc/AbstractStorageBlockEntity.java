package ninjaphenix.expandedstorage.internal_api.block.misc;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

@Internal
@Experimental
public abstract class AbstractStorageBlockEntity extends BlockEntity implements Nameable {
    private ContainerLock lockKey;
    private Text menuTitle;

    public AbstractStorageBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
        lockKey = ContainerLock.EMPTY;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        lockKey = ContainerLock.fromNbt(tag);
        if (tag.contains("CustomName", NbtElement.STRING_TYPE)) {
            menuTitle = Text.Serializer.fromJson(tag.getString("CustomName"));
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        lockKey.writeNbt(tag);
        if (menuTitle != null) {
            tag.putString("CustomName", Text.Serializer.toJson(menuTitle));
        }
        return tag;
    }

    public boolean canPlayerInteractWith(ServerPlayerEntity player) {
        return lockKey == ContainerLock.EMPTY || !player.isSpectator() && lockKey.canOpen(player.getMainHandStack());
    }

    @Override
    public final Text getName() {
        return this.hasCustomName() ? menuTitle : this.getDefaultTitle();
    }

    public abstract Text getDefaultTitle();

    @Override
    public final boolean hasCustomName() {
        return menuTitle != null;
    }

    @Nullable
    @Override
    public final Text getCustomName() {
        return menuTitle;
    }

    public final void setMenuTitle(Text title) {
        menuTitle = title;
    }
}
