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
    private final boolean isForge;

    protected PlatformUtils(boolean isClient, boolean isForge) {
        this.isClient = isClient;
        this.isForge = isForge;
    }

    public final boolean isClient() {
        return isClient;
    }

    public final boolean isForge() {
        return isForge;
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
