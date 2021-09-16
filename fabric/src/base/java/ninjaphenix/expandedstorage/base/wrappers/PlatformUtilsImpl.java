package ninjaphenix.expandedstorage.base.wrappers;

import com.mojang.datafixers.types.Type;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import ninjaphenix.expandedstorage.base.internal_api.Utils;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class PlatformUtilsImpl extends PlatformUtils {
    PlatformUtilsImpl() {
        super(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT);
    }

    @Override
    public CreativeModeTab createTab(Supplier<ItemStack> icon) {
        FabricItemGroupBuilder.build(new ResourceLocation("dummy"), null); // Fabric API is dumb.
        return new CreativeModeTab(CreativeModeTab.TABS.length - 1, Utils.MOD_ID) {
            @Override
            public ItemStack makeIcon() {
                return icon.get();
            }
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BiFunction<BlockPos, BlockState, T> blockEntitySupplier, Set<Block> blocks, Type<?> type) {
        return new BlockEntityType<>(blockEntitySupplier::apply, blocks, type);
    }
}
