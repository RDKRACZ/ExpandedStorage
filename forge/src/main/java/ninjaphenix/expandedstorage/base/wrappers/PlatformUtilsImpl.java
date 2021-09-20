package ninjaphenix.expandedstorage.base.wrappers;

import com.mojang.datafixers.types.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;
import ninjaphenix.expandedstorage.base.internal_api.Utils;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

final class PlatformUtilsImpl extends PlatformUtils {
    PlatformUtilsImpl() {
        super(FMLLoader.getDist() == Dist.CLIENT);
    }

    @Override
    public CreativeModeTab createTab(Supplier<ItemStack> icon) {
        return new CreativeModeTab(Utils.MOD_ID) {
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
