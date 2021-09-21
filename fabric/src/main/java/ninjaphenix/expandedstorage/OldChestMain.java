package ninjaphenix.expandedstorage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.util.registry.Registry;
import ninjaphenix.expandedstorage.internal_api.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.block.OldChestBlock;
import ninjaphenix.expandedstorage.block.misc.OldChestBlockEntity;

import java.util.Set;

public final class OldChestMain {
    private static void registerBET(BlockEntityType<OldChestBlockEntity> blockEntityType) {
        Registry.register(Registry.BLOCK_ENTITY_TYPE, OldChestCommon.BLOCK_TYPE, blockEntityType);
        ItemStorage.SIDED.registerForBlocks(AbstractOpenableStorageBlockEntity::getItemStorage, blockEntityType.blocks.toArray(Block[]::new));
    }

    private static void registerBlocks(Set<OldChestBlock> blocks) {
        blocks.forEach(block -> Registry.register(Registry.BLOCK, block.getBlockId(), block));
    }

    private static void registerItems(Set<BlockItem> items) {
        items.forEach(item -> Registry.register(Registry.ITEM, ((OldChestBlock) item.getBlock()).getBlockId(), item));
    }

    public static void initialize() {
        OldChestCommon.registerContent(OldChestMain::registerBlocks, OldChestMain::registerItems, OldChestMain::registerBET);
    }
}
