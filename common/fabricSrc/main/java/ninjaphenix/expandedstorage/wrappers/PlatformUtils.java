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

    public abstract ItemGroup createTab(Supplier<ItemStack> icon);

    public abstract Object createGenericItemAccess(AbstractOpenableStorageBlockEntity abstractOpenableStorageBlockEntity);

    public abstract Object createChestItemAccess(World world, BlockState state, BlockPos pos, Direction side);
}
