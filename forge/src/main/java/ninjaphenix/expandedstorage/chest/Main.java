package ninjaphenix.expandedstorage.chest;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import ninjaphenix.expandedstorage.base.wrappers.PlatformUtils;
import ninjaphenix.expandedstorage.chest.block.ChestBlock;
import ninjaphenix.expandedstorage.chest.block.ChestBlockItem;
import ninjaphenix.expandedstorage.chest.block.misc.ChestBlockEntity;
import ninjaphenix.expandedstorage.chest.client.ChestBlockEntityRenderer;

import java.util.Set;

public final class Main {
    public Main() {
        ChestCommon.registerContent(this::registerBlocks, this::registerItems, this::registerBET,
                BlockTags.createOptional(new ResourceLocation("forge", "chests/wooden")),
                ChestBlockItem::new);
    }

    private void registerBlocks(Set<ChestBlock> blocks) {
        for (ChestBlock block : blocks) {
            block.setRegistryName(block.getBlockId());
        }
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addGenericListener(Block.class, (RegistryEvent.Register<Block> event) -> {
            IForgeRegistry<Block> registry = event.getRegistry();
            for (ChestBlock block : blocks) {
                registry.register(block);
            }
        });

        if (PlatformUtils.getInstance().isClient()) {
            ChestCommon.registerChestTextures(blocks);
            modEventBus.addListener((TextureStitchEvent.Pre event) -> {
                if (!event.getMap().location().equals(Sheets.CHEST_SHEET)) {
                    return;
                }
                ChestCommon.getChestTextures(blocks).forEach(event::addSprite);
            });
        }
    }

    private void registerItems(Set<BlockItem> items) {
        for (BlockItem item : items) {
            item.setRegistryName(((ChestBlock) item.getBlock()).getBlockId());
        }
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addGenericListener(Item.class, (RegistryEvent.Register<Item> event) -> {
            IForgeRegistry<Item> registry = event.getRegistry();
            for (BlockItem item : items) {
                registry.register(item);
            }
        });
    }

    private void registerBET(BlockEntityType<ChestBlockEntity> blockEntityType) {
        blockEntityType.setRegistryName(ChestCommon.BLOCK_TYPE);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addGenericListener(BlockEntityType.class, (RegistryEvent.Register<BlockEntityType<?>> event) -> {
            event.getRegistry().register(blockEntityType);
        });
        if (PlatformUtils.getInstance().isClient()) {
            Client.registerEvents(modEventBus, blockEntityType);
        }
    }

    private static class Client {
        private static void registerEvents(IEventBus bus, BlockEntityType<ChestBlockEntity> type) {
            bus.addListener((EntityRenderersEvent.RegisterRenderers.RegisterRenderers event) -> {
                event.registerBlockEntityRenderer(type, ChestBlockEntityRenderer::new);
            });

            bus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> {
                event.registerLayerDefinition(ChestBlockEntityRenderer.SINGLE_LAYER, ChestBlockEntityRenderer::createSingleBodyLayer);
                event.registerLayerDefinition(ChestBlockEntityRenderer.LEFT_LAYER, ChestBlockEntityRenderer::createLeftBodyLayer);
                event.registerLayerDefinition(ChestBlockEntityRenderer.RIGHT_LAYER, ChestBlockEntityRenderer::createRightBodyLayer);
                event.registerLayerDefinition(ChestBlockEntityRenderer.TOP_LAYER, ChestBlockEntityRenderer::createTopBodyLayer);
                event.registerLayerDefinition(ChestBlockEntityRenderer.BOTTOM_LAYER, ChestBlockEntityRenderer::createBottomBodyLayer);
                event.registerLayerDefinition(ChestBlockEntityRenderer.FRONT_LAYER, ChestBlockEntityRenderer::createFrontBodyLayer);
                event.registerLayerDefinition(ChestBlockEntityRenderer.BACK_LAYER, ChestBlockEntityRenderer::createBackBodyLayer);
            });
        }
    }
}
