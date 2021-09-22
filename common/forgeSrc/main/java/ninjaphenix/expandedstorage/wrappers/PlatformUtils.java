package ninjaphenix.expandedstorage.wrappers;

import ninjaphenix.expandedstorage.block.misc.AbstractOpenableStorageBlockEntity;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class PlatformUtils {
    private static PlatformUtils INSTANCE;
    private final boolean isClient;

    protected PlatformUtils(boolean isClient) {
        this.isClient = isClient;
    }

    public boolean isClient() {
        return isClient;
    }

    public static PlatformUtils getInstance() {
        if (PlatformUtils.INSTANCE == null) {
            PlatformUtils.INSTANCE = new PlatformUtilsImpl();
        }
        return PlatformUtils.INSTANCE;
    }

    public abstract CreativeModeTab createTab(Supplier<ItemStack> icon);

    public abstract Object createGenericItemAccess(AbstractOpenableStorageBlockEntity abstractOpenableStorageBlockEntity);

    public abstract Object createChestItemAccess(Level world, BlockState state, BlockPos pos, Direction side);
}
