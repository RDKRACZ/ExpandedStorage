package ninjaphenix.expandedstorage;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import ninjaphenix.expandedstorage.block.BarrelBlock;
import ninjaphenix.expandedstorage.block.ChestBlock;
import ninjaphenix.expandedstorage.block.OldChestBlock;
import ninjaphenix.expandedstorage.block.misc.BarrelBlockEntity;
import ninjaphenix.expandedstorage.block.misc.ChestBlockEntity;
import ninjaphenix.expandedstorage.block.misc.OldChestBlockEntity;
import ninjaphenix.expandedstorage.client.ChestBlockEntityRenderer;
import ninjaphenix.expandedstorage.internal_api.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;

public final class BaseMain implements ModInitializer {
    @Override
    public void onInitialize() {
        Common.registerBaseContent(BaseMain::baseRegistration);
        Common.registerChestContent(BaseMain::chestRegistration, TagFactory.BLOCK.create(new Identifier("c", "wooden_chests")), BlockItem::new);
        Common.registerOldChestContent(BaseMain::oldChestRegistration);
        Common.registerBarrelContent(BaseMain::barrelRegistration, TagFactory.BLOCK.create(new Identifier("c", "wooden_barrels")));

        /* GOALS
         *
         * Provide a centralised api for kubejs and java to register new tiers and therefore blocks.
         *  will probably make my own json loaded content at some point...
         * Probably a bunch of other stuff I can't think of.
         */
    }

    private static void baseRegistration(Pair<Identifier, Item>[] items) {
        for (Pair<Identifier, Item> item : items) {
            Registry.register(Registry.ITEM, item.getLeft(), item.getRight());
        }
    }

    private static void chestRegistration(ChestBlock[] blocks, BlockItem[] items, BlockEntityType<ChestBlockEntity> blockEntityType) {
        for (ChestBlock block : blocks) {
            Registry.register(Registry.BLOCK, block.getBlockId(), block);
        }
        for (BlockItem item : items) {
            Registry.register(Registry.ITEM, ((ChestBlock) item.getBlock()).getBlockId(), item);
        }
        Registry.register(Registry.BLOCK_ENTITY_TYPE, Common.CHEST_BLOCK_TYPE, blockEntityType);
        ItemStorage.SIDED.registerForBlocks(AbstractOpenableStorageBlockEntity::getItemStorage, blocks);
        if (PlatformUtils.getInstance().isClient()) {
            BaseMain.Client.registerChestTextures(blocks);
            BaseMain.Client.registerItemRenderers(items);
        }
    }

    private static void oldChestRegistration(OldChestBlock[] blocks, BlockItem[] items, BlockEntityType<OldChestBlockEntity> blockEntityType) {
        for (OldChestBlock block : blocks) {
            Registry.register(Registry.BLOCK, block.getBlockId(), block);
        }
        for (BlockItem item : items) {
            Registry.register(Registry.ITEM, ((OldChestBlock) item.getBlock()).getBlockId(), item);
        }
        Registry.register(Registry.BLOCK_ENTITY_TYPE, Common.OLD_CHEST_BLOCK_TYPE, blockEntityType);
        ItemStorage.SIDED.registerForBlocks(AbstractOpenableStorageBlockEntity::getItemStorage, blocks);
    }

    private static void barrelRegistration(BarrelBlock[] blocks, BlockItem[] items, BlockEntityType<BarrelBlockEntity> blockEntityType) {
        boolean isClient = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
        for (BarrelBlock block : blocks) {
            Registry.register(Registry.BLOCK, block.getBlockId(), block);
            if (isClient) {
                BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutoutMipped());
            }
        }
        for (BlockItem item : items) {
            Registry.register(Registry.ITEM, ((BarrelBlock) item.getBlock()).getBlockId(), item);
        }
        Registry.register(Registry.BLOCK_ENTITY_TYPE, Common.BARREL_BLOCK_TYPE, blockEntityType);
        ItemStorage.SIDED.registerForBlocks(AbstractOpenableStorageBlockEntity::getItemStorage, blocks);
    }

    private static class Client {
        public static void registerChestTextures(ChestBlock[] blocks) {
            Common.registerChestTextures(blocks);
            ClientSpriteRegistryCallback.event(TexturedRenderLayers.CHEST_ATLAS_TEXTURE).register((atlasTexture, registry) -> {
                for (Identifier texture : Common.getChestTextures(blocks)) {
                    registry.register(texture);
                }
            });
            BlockEntityRendererRegistry.register(Common.getChestBlockEntityType(), ChestBlockEntityRenderer::new);
        }

        public static void registerItemRenderers(BlockItem[] items) {
            for (BlockItem item : items) {
                ChestBlockEntity renderEntity = new ChestBlockEntity(Common.getChestBlockEntityType(), BlockPos.ORIGIN, item.getBlock().getDefaultState());
                BuiltinItemRendererRegistry.INSTANCE.register(item, (itemStack, transform, stack, source, light, overlay) ->
                        MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(renderEntity, stack, source, light, overlay));
            }
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
