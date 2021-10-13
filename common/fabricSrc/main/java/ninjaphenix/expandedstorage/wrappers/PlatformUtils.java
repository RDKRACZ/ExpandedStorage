package ninjaphenix.expandedstorage.wrappers;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.block.misc.AbstractOpenableStorageBlockEntity;

import java.util.function.Supplier;

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

    public abstract ItemGroup createTab(Supplier<ItemStack> icon);

    public abstract Object createGenericItemAccess(AbstractOpenableStorageBlockEntity abstractOpenableStorageBlockEntity);

    public abstract Object createChestItemAccess(World world, BlockState state, BlockPos pos, Direction side);
}
