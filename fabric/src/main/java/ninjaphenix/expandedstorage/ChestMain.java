package ninjaphenix.expandedstorage;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import ninjaphenix.expandedstorage.block.ChestBlock;
import ninjaphenix.expandedstorage.block.misc.ChestBlockEntity;
import ninjaphenix.expandedstorage.client.ChestBlockEntityRenderer;
import ninjaphenix.expandedstorage.internal_api.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class ChestMain {
    public static void initialize() {
        // This is nasty, can I make this code better?
        AtomicReference<Set<ChestBlock>> b = new AtomicReference<>();
        AtomicReference<Set<BlockItem>> i = new AtomicReference<>();
        Consumer<Set<ChestBlock>> registerBlocks = (blocks) -> {
            b.set(blocks);
            blocks.forEach(block -> Registry.register(Registry.BLOCK, block.getBlockId(), block));
        };
        Consumer<Set<BlockItem>> registerItems = (items) -> {
            i.set(items);
            items.forEach(item -> Registry.register(Registry.ITEM, ((ChestBlock) item.getBlock()).getBlockId(), item));
        };
        Consumer<BlockEntityType<ChestBlockEntity>> registerBET = (blockEntityType) -> {
            Registry.register(Registry.BLOCK_ENTITY_TYPE, ChestCommon.BLOCK_TYPE, blockEntityType);
            ItemStorage.SIDED.registerForBlocks(AbstractOpenableStorageBlockEntity::getItemStorage, blockEntityType.blocks.toArray(Block[]::new));
            if (PlatformUtils.getInstance().isClient()) {
                Client.registerChestTextures(b.get());
                Client.registerItemRenderers(i.get());
            }
        };
        ChestCommon.registerContent(registerBlocks, registerItems, registerBET, TagFactory.BLOCK.create(new Identifier("c", "wooden_chests")), BlockItem::new);
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
