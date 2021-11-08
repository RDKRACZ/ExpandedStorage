/*
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
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.block.BarrelBlock;
import ninjaphenix.expandedstorage.block.ChestBlock;
import ninjaphenix.expandedstorage.block.MiniChestBlock;
import ninjaphenix.expandedstorage.block.OpenableBlock;
import ninjaphenix.expandedstorage.block.entity.BarrelBlockEntity;
import ninjaphenix.expandedstorage.block.entity.ChestBlockEntity;
import ninjaphenix.expandedstorage.block.entity.MiniChestBlockEntity;
import ninjaphenix.expandedstorage.block.entity.OldChestBlockEntity;
import ninjaphenix.expandedstorage.block.entity.extendable.StrategyBlockEntity;
import ninjaphenix.expandedstorage.block.strategies.ItemAccess;
import ninjaphenix.expandedstorage.block.strategies.Lockable;
import ninjaphenix.expandedstorage.client.ChestBlockEntityRenderer;
import ninjaphenix.expandedstorage.compat.carrier.CarrierCompat;
import org.jetbrains.annotations.Nullable;

public final class Main implements ModInitializer {
    @Override
    public void onInitialize() {
        // Note: shared item access cannot be used for MiniChest
        // Lockable needs replacing with Basic or HTM lock impl.
        Common.setSharedStrategies((entity) -> new ItemAccess() {
            private InventoryStorage storage = null;
            @Override
            public Object get() {
                if (storage == null) {
                    DefaultedList<ItemStack> items = entity.getItems();
                    Inventory wrapped = entity.getInventory();
                    Inventory transferApiInventory = new Inventory() {
                        @Override
                        public int size() {
                            return wrapped.size();
                        }

                        @Override
                        public boolean isEmpty() {
                            return wrapped.isEmpty();
                        }

                        @Override
                        public ItemStack getStack(int slot) {
                            return wrapped.getStack(slot);
                        }

                        @Override
                        public ItemStack removeStack(int slot, int amount) {
                            return Inventories.splitStack(items, slot, amount);
                        }

                        @Override
                        public ItemStack removeStack(int slot) {
                            return wrapped.removeStack(slot);
                        }

                        @Override
                        public void setStack(int slot, ItemStack stack) {
                            items.set(slot, stack);
                            if (stack.getCount() > this.getMaxCountPerStack()) {
                                stack.setCount(this.getMaxCountPerStack());
                            }
                        }

                        @Override
                        public void markDirty() {
                            wrapped.markDirty();
                        }

                        @Override
                        public boolean canPlayerUse(PlayerEntity player) {
                            return wrapped.canPlayerUse(player);
                        }

                        @Override
                        public void clear() {
                            wrapped.clear();
                        }

                        @Override
                        public void onOpen(PlayerEntity player) {
                            wrapped.onOpen(player);
                        }

                        @Override
                        public void onClose(PlayerEntity player) {
                            wrapped.onClose(player);
                        }
                    };
                    storage = InventoryStorage.of(transferApiInventory, null);
                }
                return storage;
            }

            @Override
            public void invalidate() {
                storage = null;
            }
        }, (entity) -> Lockable.NOT_LOCKABLE);
        FabricItemGroupBuilder.build(new Identifier("dummy"), null); // Fabric API is dumb.
        Common.setGroup(new ItemGroup(ItemGroup.GROUPS.length - 1, Utils.MOD_ID) {
            @Override
            public ItemStack createIcon() {
                return new ItemStack(Registry.ITEM.get(Utils.id("netherite_chest")));
            }
        });
        Common.registerBaseContent(Main::baseRegistration, true);
        Common.registerChestContent(Main::chestRegistration, TagFactory.BLOCK.create(new Identifier("c", "wooden_chests")), BlockItem::new, FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT);
        Common.registerOldChestContent(Main::oldChestRegistration);
        Common.registerBarrelContent(Main::barrelRegistration, TagFactory.BLOCK.create(new Identifier("c", "wooden_barrels")));
        Common.registerMiniChestContent(Main::miniChestRegistration);
    }

    private static void miniChestRegistration(MiniChestBlock[] blocks, BlockItem[] items, BlockEntityType<MiniChestBlockEntity> blockEntityType) {
        for (MiniChestBlock block : blocks) {
            Registry.register(Registry.BLOCK, block.getBlockId(), block);
        }
        for (BlockItem item : items) {
            Registry.register(Registry.ITEM, ((OpenableBlock) item.getBlock()).getBlockId(), item);
        }
        Registry.register(Registry.BLOCK_ENTITY_TYPE, Common.MINI_CHEST_BLOCK_TYPE, blockEntityType);
        ItemStorage.SIDED.registerForBlocks(Main::getItemAccess, blocks);
    }

    private static void baseRegistration(Pair<Identifier, Item>[] items) {
        for (Pair<Identifier, Item> item : items) {
            Registry.register(Registry.ITEM, item.getLeft(), item.getRight());
        }
    }

    private static void chestRegistration(ChestBlock[] blocks, BlockItem[] items, BlockEntityType<ChestBlockEntity> blockEntityType) {
        final boolean addCarrierSupport = Main.shouldEnableCarrierCompat();
        for (ChestBlock block : blocks) {
            if (addCarrierSupport) CarrierCompat.registerChestBlock(block);
            Registry.register(Registry.BLOCK, block.getBlockId(), block);
        }
        for (BlockItem item : items) {
            Registry.register(Registry.ITEM, ((OpenableBlock) item.getBlock()).getBlockId(), item);
        }
        Registry.register(Registry.BLOCK_ENTITY_TYPE, Common.CHEST_BLOCK_TYPE, blockEntityType);
        // noinspection UnstableApiUsage,deprecation
        ItemStorage.SIDED.registerForBlocks(Main::getItemAccess, blocks);
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            Main.Client.registerChestTextures(blocks);
            Main.Client.registerItemRenderers(items);
        }
    }

    private static boolean shouldEnableCarrierCompat() {
        try {
            SemanticVersion version = SemanticVersion.parse("1.8.0");
            return FabricLoader.getInstance().getModContainer("carrier").map(it -> {
                if (it.getMetadata().getVersion() instanceof SemanticVersion carrierVersion) {
                    return carrierVersion.compareTo(version) > 0;
                }
                return false;
            }).orElse(false);
        } catch (VersionParsingException ignored) {
        }
        return false;
    }

    @SuppressWarnings({"deprecation", "UnstableApiUsage"})
    private static Storage<ItemVariant> getItemAccess(World world, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, Direction context) {
        if (blockEntity instanceof StrategyBlockEntity entity) {
            //noinspection unchecked
            return (Storage<ItemVariant>) entity.getItemAccess().get();
        }
        return null;
    }

    private static void oldChestRegistration(AbstractChestBlock[] blocks, BlockItem[] items, BlockEntityType<OldChestBlockEntity> blockEntityType) {
        final boolean addCarrierSupport = Main.shouldEnableCarrierCompat();
        for (AbstractChestBlock block : blocks) {
            if (addCarrierSupport) CarrierCompat.registerOldChestBlock(block);
            Registry.register(Registry.BLOCK, block.getBlockId(), block);
        }
        for (BlockItem item : items) {
            Registry.register(Registry.ITEM, ((OpenableBlock) item.getBlock()).getBlockId(), item);
        }
        Registry.register(Registry.BLOCK_ENTITY_TYPE, Common.OLD_CHEST_BLOCK_TYPE, blockEntityType);
        //noinspection deprecation,UnstableApiUsage
        ItemStorage.SIDED.registerForBlocks(Main::getItemAccess, blocks);
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
            Registry.register(Registry.ITEM, ((OpenableBlock) item.getBlock()).getBlockId(), item);
        }
        Registry.register(Registry.BLOCK_ENTITY_TYPE, Common.BARREL_BLOCK_TYPE, blockEntityType);
        //noinspection deprecation,UnstableApiUsage
        ItemStorage.SIDED.registerForBlocks(Main::getItemAccess, blocks);
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
                ChestBlockEntity renderEntity = Common.createChestBlockEntity(BlockPos.ORIGIN, item.getBlock().getDefaultState());
                BuiltinItemRendererRegistry.INSTANCE.register(item, (itemStack, transform, stack, source, light, overlay) ->
                        MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(renderEntity, stack, source, light, overlay));
            }
            EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.SINGLE_LAYER, ChestBlockEntityRenderer::createSingleBodyLayer);
            EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.LEFT_LAYER, ChestBlockEntityRenderer::createLeftBodyLayer);
            EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.RIGHT_LAYER, ChestBlockEntityRenderer::createRightBodyLayer);
            EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.TOP_LAYER, ChestBlockEntityRenderer::createTopBodyLayer);
            EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.BOTTOM_LAYER, ChestBlockEntityRenderer::createBottomBodyLayer);
            EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.FRONT_LAYER, ChestBlockEntityRenderer::createFrontBodyLayer);
            EntityModelLayerRegistry.registerModelLayer(ChestBlockEntityRenderer.BACK_LAYER, ChestBlockEntityRenderer::createBackBodyLayer);
        }
    }
}
