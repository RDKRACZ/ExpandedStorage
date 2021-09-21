package ninjaphenix.expandedstorage;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import ninjaphenix.expandedstorage.block.BarrelBlock;
import ninjaphenix.expandedstorage.block.ChestBlock;
import ninjaphenix.expandedstorage.block.OldChestBlock;
import ninjaphenix.expandedstorage.block.misc.BarrelBlockEntity;
import ninjaphenix.expandedstorage.block.misc.ChestBlockEntity;
import ninjaphenix.expandedstorage.block.misc.OldChestBlockEntity;
import ninjaphenix.expandedstorage.client.ChestBlockEntityRenderer;
import ninjaphenix.expandedstorage.internal_api.BaseApi;
import ninjaphenix.expandedstorage.internal_api.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;

import java.util.Set;

public final class BaseMain implements ModInitializer {
    @Override
    public void onInitialize() {
        BaseCommon.initialize();
        BaseApi.getInstance().getAndClearItems().forEach((id, item) -> Registry.register(Registry.ITEM, id, item));

        ChestCommon.registerContent(BaseMain::chestRegistration, TagFactory.BLOCK.create(new Identifier("c", "wooden_chests")), BlockItem::new);
        OldChestCommon.registerContent(BaseMain::oldChestRegistration);
        BarrelCommon.registerContent(BaseMain::barrelRegistration, TagFactory.BLOCK.create(new Identifier("c", "wooden_barrels")));

        /* GOALS
         *
         * Provide a centralised api for kubejs and java to register new tiers and therefore blocks.
         *  will probably make my own json loaded content at some point...
         * Probably a bunch of other stuff I can't think of.
         */
    }

    private static void barrelRegistration(Set<BarrelBlock> blocks, Set<BlockItem> items, BlockEntityType<BarrelBlockEntity> blockEntityType) {
        blocks.forEach(block -> Registry.register(Registry.BLOCK, block.getBlockId(), block));
        items.forEach(item -> Registry.register(Registry.ITEM, ((BarrelBlock) item.getBlock()).getBlockId(), item));
        Registry.register(Registry.BLOCK_ENTITY_TYPE, BarrelCommon.BLOCK_TYPE, blockEntityType);
        ItemStorage.SIDED.registerForBlocks(AbstractOpenableStorageBlockEntity::getItemStorage, blockEntityType.blocks.toArray(Block[]::new));
        if (PlatformUtils.getInstance().isClient()) {
            blocks.forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutoutMipped()));
        }
    }

    private static void chestRegistration(Set<ChestBlock> blocks, Set<BlockItem> items, BlockEntityType<ChestBlockEntity> blockEntityType) {
        blocks.forEach(block -> Registry.register(Registry.BLOCK, block.getBlockId(), block));
        items.forEach(item -> Registry.register(Registry.ITEM, ((ChestBlock) item.getBlock()).getBlockId(), item));
        Registry.register(Registry.BLOCK_ENTITY_TYPE, ChestCommon.BLOCK_TYPE, blockEntityType);
        ItemStorage.SIDED.registerForBlocks(AbstractOpenableStorageBlockEntity::getItemStorage, blockEntityType.blocks.toArray(Block[]::new));
        if (PlatformUtils.getInstance().isClient()) {
            BaseMain.Client.registerChestTextures(blocks);
            BaseMain.Client.registerItemRenderers(items);
        }
    }

    private static void oldChestRegistration(Set<OldChestBlock> blocks, Set<BlockItem> items, BlockEntityType<OldChestBlockEntity> blockEntityType) {
        blocks.forEach(block -> Registry.register(Registry.BLOCK, block.getBlockId(), block));
        items.forEach(item -> Registry.register(Registry.ITEM, ((OldChestBlock) item.getBlock()).getBlockId(), item));
        Registry.register(Registry.BLOCK_ENTITY_TYPE, OldChestCommon.BLOCK_TYPE, blockEntityType);
        ItemStorage.SIDED.registerForBlocks(AbstractOpenableStorageBlockEntity::getItemStorage, blockEntityType.blocks.toArray(Block[]::new));
    }

    private static class Client {
        public static void registerChestTextures(Set<ChestBlock> blocks) {
            ChestCommon.registerChestTextures(blocks);
            ClientSpriteRegistryCallback.event(TexturedRenderLayers.CHEST_ATLAS_TEXTURE).register((atlasTexture, registry) -> ChestCommon.getChestTextures(blocks).forEach(registry::register));
            BlockEntityRendererRegistry.register(ChestCommon.getBlockEntityType(), ChestBlockEntityRenderer::new);
        }

        public static void registerItemRenderers(Set<BlockItem> items) {
            items.forEach(item -> {
                ChestBlockEntity renderEntity = new ChestBlockEntity(ChestCommon.getBlockEntityType(), BlockPos.ORIGIN, item.getBlock().getDefaultState());
                BuiltinItemRendererRegistry.INSTANCE.register(item, (itemStack, transform, stack, source, light, overlay) ->
                        MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(renderEntity, stack, source, light, overlay));
            });
            EntityModelLayers.LAYERS.add(ChestBlockEntityRenderer.SINGLE_LAYER);
            EntityModelLayers.LAYERS.add(ChestBlockEntityRenderer.LEFT_LAYER);
            EntityModelLayers.LAYERS.add(ChestBlockEntityRenderer.RIGHT_LAYER);
            EntityModelLayers.LAYERS.add(ChestBlockEntityRenderer.TOP_LAYER);
            EntityModelLayers.LAYERS.add(ChestBlockEntityRenderer.BOTTOM_LAYER);
            EntityModelLayers.LAYERS.add(ChestBlockEntityRenderer.FRONT_LAYER);
            EntityModelLayers.LAYERS.add(ChestBlockEntityRenderer.BACK_LAYER);
        }
    }
}
