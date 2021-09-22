package ninjaphenix.expandedstorage;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.IForgeRegistry;
import ninjaphenix.expandedstorage.block.BarrelBlock;
import ninjaphenix.expandedstorage.block.ChestBlock;
import ninjaphenix.expandedstorage.block.OldChestBlock;
import ninjaphenix.expandedstorage.block.misc.AbstractChestBlockEntity;
import ninjaphenix.expandedstorage.block.misc.BarrelBlockEntity;
import ninjaphenix.expandedstorage.block.misc.ChestBlockEntity;
import ninjaphenix.expandedstorage.client.ChestBlockEntityRenderer;
import ninjaphenix.expandedstorage.internal_api.Utils;
import ninjaphenix.expandedstorage.internal_api.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod("expandedstorage")
public final class Main {
    private final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
    public Main() {
        Common.registerBaseContent(this::baseRegistration);
        Common.registerChestContent(this::chestRegistration, BlockTags.createOptional(new ResourceLocation("forge", "chests/wooden")), ChestBlockItem::new);
        Common.registerOldChestContent(this::oldChestRegistration);
        Common.registerBarrelContent(this::barrelRegistration, BlockTags.createOptional(new ResourceLocation("forge", "barrels/wooden")));

        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, (AttachCapabilitiesEvent<BlockEntity> event) -> {
            if (event.getObject() instanceof AbstractOpenableStorageBlockEntity entity) {
                event.addCapability(Utils.id("item_access"), new ICapabilityProvider() {
                    @NotNull
                    @Override
                    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
                        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                            //noinspection unchecked
                            return LazyOptional.of(() -> (T) AbstractOpenableStorageBlockEntity.getItemAccess(entity.getLevel(), entity.getBlockPos(), entity.getBlockState(), entity, side));
                        }
                        return LazyOptional.empty();
                    }
                });
            }
        });
    }

    private void chestRegistration(ChestBlock[] blocks, BlockItem[] items, BlockEntityType<ChestBlockEntity> blockEntityType) {
        modBus.addGenericListener(Block.class, (RegistryEvent.Register<Block> event) -> {
            IForgeRegistry<Block> registry = event.getRegistry();
            for (ChestBlock block : blocks) {
                registry.register(block.setRegistryName(block.getBlockId()));
            }
        });
        modBus.addGenericListener(Item.class, (RegistryEvent.Register<Item> event) -> {
            IForgeRegistry<Item> registry = event.getRegistry();
            for (BlockItem item : items) {
                registry.register(item.setRegistryName(((ChestBlock) item.getBlock()).getBlockId()));
            }
        });
        blockEntityType.setRegistryName(Common.CHEST_BLOCK_TYPE);
        modBus.addGenericListener(BlockEntityType.class, (RegistryEvent.Register<BlockEntityType<?>> event) -> {
            event.getRegistry().register(blockEntityType);
        });

        if (PlatformUtils.getInstance().isClient()) {
            modBus.addListener((TextureStitchEvent.Pre event) -> {
                if (!event.getMap().location().equals(Sheets.CHEST_SHEET)) {
                    return;
                }
                for (ResourceLocation texture : Common.getChestTextures(blocks)) {
                    event.addSprite(texture);
                }
            });
            Client.registerEvents(modBus, blockEntityType);
        }
    }

    private void oldChestRegistration(OldChestBlock[] blocks, BlockItem[] items, BlockEntityType<AbstractChestBlockEntity> blockEntityType) {
        modBus.addGenericListener(Block.class, (RegistryEvent.Register<Block> event) -> {
            IForgeRegistry<Block> registry = event.getRegistry();
            for (OldChestBlock block : blocks) {
                registry.register(block.setRegistryName(block.getBlockId()));
            }
        });
        modBus.addGenericListener(Item.class, (RegistryEvent.Register<Item> event) -> {
            IForgeRegistry<Item> registry = event.getRegistry();
            for (BlockItem item : items) {
                registry.register(item.setRegistryName(((OldChestBlock) item.getBlock()).getBlockId()));
            }
        });
        blockEntityType.setRegistryName(Common.OLD_CHEST_BLOCK_TYPE);
        modBus.addGenericListener(BlockEntityType.class, (RegistryEvent.Register<BlockEntityType<?>> event) -> {
            event.getRegistry().register(blockEntityType);
        });
    }

    private void barrelRegistration(BarrelBlock[] blocks, BlockItem[] items, BlockEntityType<BarrelBlockEntity> blockEntityType) {
        modBus.addGenericListener(Block.class, (RegistryEvent.Register<Block> event) -> {
            IForgeRegistry<Block> registry = event.getRegistry();
            for (BarrelBlock block : blocks) {
                registry.register(block.setRegistryName(block.getBlockId()));
            }
        });
        modBus.addGenericListener(Item.class, (RegistryEvent.Register<Item> event) -> {
            IForgeRegistry<Item> registry = event.getRegistry();
            for (BlockItem item : items) {
                registry.register(item.setRegistryName(((BarrelBlock) item.getBlock()).getBlockId()));
            }
        });
        blockEntityType.setRegistryName(Common.BARREL_BLOCK_TYPE);
        modBus.addGenericListener(BlockEntityType.class, (RegistryEvent.Register<BlockEntityType<?>> event) -> {
            event.getRegistry().register(blockEntityType);
        });

        if (PlatformUtils.getInstance().isClient()) {
            modBus.addListener((FMLClientSetupEvent event) -> {
                for (BarrelBlock block : blocks) {
                    ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped());
                }
            });
        }
    }

    private  void baseRegistration(Tuple<ResourceLocation, Item>[] items) {
        modBus.addGenericListener(Item.class, (RegistryEvent.Register<Item> event) -> {
            IForgeRegistry<Item> registry = event.getRegistry();
            for (Tuple<ResourceLocation, Item> item : items) {
                registry.register(item.getB().setRegistryName(item.getA()));
            }
        });
    }

    private static class Client {
        private static void registerEvents(IEventBus modBus, BlockEntityType<ChestBlockEntity> type) {
            modBus.addListener((EntityRenderersEvent.RegisterRenderers.RegisterRenderers event) -> {
                event.registerBlockEntityRenderer(type, ChestBlockEntityRenderer::new);
            });

            modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> {
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
