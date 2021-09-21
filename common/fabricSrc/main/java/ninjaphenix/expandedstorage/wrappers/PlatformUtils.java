package ninjaphenix.expandedstorage.wrappers;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

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
}
