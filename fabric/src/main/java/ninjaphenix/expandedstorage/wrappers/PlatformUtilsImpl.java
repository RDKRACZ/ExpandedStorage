package ninjaphenix.expandedstorage.wrappers;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import ninjaphenix.expandedstorage.internal_api.Utils;

import java.util.function.Supplier;

public final class PlatformUtilsImpl extends PlatformUtils {
    PlatformUtilsImpl() {
        super(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT);
    }

    @Override
    public ItemGroup createTab(Supplier<ItemStack> icon) { // Hopefully fabric api gets rid of this builder in favour of transitive AW.
        FabricItemGroupBuilder.build(new Identifier("dummy"), null); // Fabric API is dumb.
        return new ItemGroup(ItemGroup.GROUPS.length - 1, Utils.MOD_ID) {
            @Override
            public ItemStack createIcon() {
                return icon.get();
            }
        };
    }
}
