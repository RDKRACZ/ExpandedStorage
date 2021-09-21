package ninjaphenix.expandedstorage.barrel;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import ninjaphenix.expandedstorage.barrel.block.BarrelBlock;
import ninjaphenix.expandedstorage.barrel.block.misc.BarrelBlockEntity;
import ninjaphenix.expandedstorage.base.internal_api.ModuleInitializer;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.base.wrappers.PlatformUtils;

import java.util.Set;

public final class Main implements ModuleInitializer {
    private static void registerBET(BlockEntityType<BarrelBlockEntity> blockEntityType) {
        Registry.register(Registry.BLOCK_ENTITY_TYPE, BarrelCommon.BLOCK_TYPE, blockEntityType);
        ItemStorage.SIDED.registerForBlocks(AbstractOpenableStorageBlockEntity::getItemStorage, blockEntityType.blocks.toArray(Block[]::new));
    }

    private static void registerBlocks(Set<BarrelBlock> blocks) {
        blocks.forEach(block -> Registry.register(Registry.BLOCK, block.getBlockId(), block));
        if (PlatformUtils.getInstance().isClient()) {
            blocks.forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutoutMipped()));
        }
    }

    private static void registerItems(Set<BlockItem> items) {
        items.forEach(item -> Registry.register(Registry.ITEM, ((BarrelBlock) item.getBlock()).getBlockId(), item));
    }

    @Override
    public void initialize() {
        BarrelCommon.registerContent(Main::registerBlocks, Main::registerItems, Main::registerBET, TagFactory.BLOCK.create(new Identifier("c", "wooden_barrels")));
    }
}
