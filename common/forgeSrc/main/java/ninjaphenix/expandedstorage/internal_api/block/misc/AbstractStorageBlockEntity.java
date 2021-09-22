package ninjaphenix.expandedstorage.internal_api.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.LockCode;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Experimental
public abstract class AbstractStorageBlockEntity extends BlockEntity {
    private LockCode lockKey;
    private Component title;

    public AbstractStorageBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
        lockKey = LockCode.NO_LOCK;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        lockKey = LockCode.fromTag(tag);
        if (tag.contains("CustomName", Tag.TAG_STRING)) {
            title = Component.Serializer.fromJson(tag.getString("CustomName"));
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        super.save(tag);
        lockKey.addToTag(tag);
        if (title != null) {
            tag.putString("CustomName", Component.Serializer.toJson(title));
        }
        return tag;
    }

    public boolean usableBy(ServerPlayer player) {
        return lockKey == LockCode.NO_LOCK || !player.isSpectator() && lockKey.unlocksWith(player.getMainHandItem());
    }

    public final Component getTitle() {
        return this.hasCustomTitle() ? title : this.getDefaultTitle();
    }

    protected abstract Component getDefaultTitle();

    public final boolean hasCustomTitle() {
        return title != null;
    }

    public final void setTitle(Component title) {
        this.title = title;
    }
}
