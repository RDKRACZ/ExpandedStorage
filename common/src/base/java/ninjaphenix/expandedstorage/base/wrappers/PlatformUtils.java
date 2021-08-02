package ninjaphenix.expandedstorage.base.wrappers;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import ninjaphenix.expandedstorage.base.internal_api.inventory.ClientMenuFactory;

import java.util.function.Supplier;

public interface PlatformUtils {
    static PlatformUtils getInstance() {
        return PlatformUtilsImpl.getInstance();
    }

    CreativeModeTab createTab(Supplier<ItemStack> icon);

    boolean isClient();

    <T extends AbstractContainerMenu> MenuType<T> createMenuType(ResourceLocation menuType, ClientMenuFactory<T> factory);

    boolean isModLoaded(String modId);

    KeyMapping getConfigKey();

    boolean isConfigKeyPressed(int keyCode, int scanCode, int modifiers);
}
