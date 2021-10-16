/**
 * Copyright 2021 NinjaPhenix
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ninjaphenix.expandedstorage;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.block.BarrelBlock;
import ninjaphenix.expandedstorage.block.ChestBlock;
import ninjaphenix.expandedstorage.block.OldChestBlock;
import ninjaphenix.expandedstorage.block.misc.AbstractChestBlockEntity;
import ninjaphenix.expandedstorage.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.block.misc.BarrelBlockEntity;
import ninjaphenix.expandedstorage.block.misc.ChestBlockEntity;
import ninjaphenix.expandedstorage.client.ChestBlockEntityRenderer;
import ninjaphenix.expandedstorage.compat.carrier.CarrierCompat;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;
import org.jetbrains.annotations.Nullable;

public final class Main implements ModInitializer {
    @Override
    public void onInitialize() {
        Common.registerBaseContent(Main::baseRegistration);
        Common.registerChestContent(Main::chestRegistration, TagFactory.BLOCK.create(new Identifier("c", "wooden_chests")), BlockItem::new);
        Common.registerOldChestContent(Main::oldChestRegistration);
        Common.registerBarrelContent(Main::barrelRegistration, TagFactory.BLOCK.create(new Identifier("c", "wooden_barrels")));

        if (FabricLoader.getInstance().isModLoaded("carrier")) {
            CarrierCompat.initialize();
        }

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
        // noinspection UnstableApiUsage,deprecation
        ItemStorage.SIDED.registerForBlocks(Main::getChestItemAccess, blocks);
        if (PlatformUtils.getInstance().isClient()) {
            Main.Client.registerChestTextures(blocks);
            Main.Client.registerItemRenderers(items);
        }
    }

    @SuppressWarnings({"deprecation", "UnstableApiUsage"})
    private static Storage<ItemVariant> getChestItemAccess(World world, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, Direction context) {
        //noinspection unchecked,deprecation,UnstableApiUsage
        return (Storage<ItemVariant>) AbstractOpenableStorageBlockEntity.getItemAccess(world, pos, state, blockEntity, context);
    }

    private static void oldChestRegistration(OldChestBlock[] blocks, BlockItem[] items, BlockEntityType<AbstractChestBlockEntity> blockEntityType) {
        for (OldChestBlock block : blocks) {
            Registry.register(Registry.BLOCK, block.getBlockId(), block);
        }
        for (BlockItem item : items) {
            Registry.register(Registry.ITEM, ((OldChestBlock) item.getBlock()).getBlockId(), item);
        }
        Registry.register(Registry.BLOCK_ENTITY_TYPE, Common.OLD_CHEST_BLOCK_TYPE, blockEntityType);
        //noinspection deprecation,UnstableApiUsage
        ItemStorage.SIDED.registerForBlocks(Main::getChestItemAccess, blocks);
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
        //noinspection deprecation,UnstableApiUsage
        ItemStorage.SIDED.registerForBlocks(Main::getChestItemAccess, blocks);
    }

    private static class Client {
        public static void registerChestTextures(ChestBlock[] blocks) {
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
