package ninjaphenix.expandedstorage;

import com.google.common.collect.ImmutableSet;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import ninjaphenix.expandedstorage.block.BarrelBlock;
import ninjaphenix.expandedstorage.block.ChestBlock;
import ninjaphenix.expandedstorage.block.OldChestBlock;
import ninjaphenix.expandedstorage.block.misc.BarrelBlockEntity;
import ninjaphenix.expandedstorage.block.misc.ChestBlockEntity;
import ninjaphenix.expandedstorage.block.misc.OldChestBlockEntity;
import ninjaphenix.expandedstorage.client.ChestBlockEntityRenderer;
import ninjaphenix.expandedstorage.internal_api.BaseApi;
import ninjaphenix.expandedstorage.internal_api.Utils;
import ninjaphenix.expandedstorage.internal_api.tier.Tier;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;

import java.util.Collections;
import java.util.Set;

public final class Main implements ModInitializer {
    @Override
    public void onInitialize() {
        CommonMain.initialize();
        BaseApi.getInstance().getAndClearItems().forEach((id, item) -> Registry.register(Registry.ITEM, id, item));

        this.barrelInitialize();
        this.chestInitialize();
        this.oldChestInitialize();
    }

    public void barrelInitialize() {
        // Init and register opening stats
        ResourceLocation ironOpenStat = CommonMain.registerStat(Utils.resloc("open_iron_barrel"));
        ResourceLocation goldOpenStat = CommonMain.registerStat(Utils.resloc("open_gold_barrel"));
        ResourceLocation diamondOpenStat = CommonMain.registerStat(Utils.resloc("open_diamond_barrel"));
        ResourceLocation obsidianOpenStat = CommonMain.registerStat(Utils.resloc("open_obsidian_barrel"));
        ResourceLocation netheriteOpenStat = CommonMain.registerStat(Utils.resloc("open_netherite_barrel"));
        // Init block properties
        BlockBehaviour.Properties ironProperties = FabricBlockSettings.of(Material.WOOD, MaterialColor.WOOD)
                                                                      .breakByTool(FabricToolTags.AXES, Tiers.STONE.getLevel())
                                                                      .requiresCorrectToolForDrops() // End of FBS
                                                                      .strength(5.0F, 6.0F)
                                                                      .sound(SoundType.WOOD);
        BlockBehaviour.Properties goldProperties = FabricBlockSettings.of(Material.WOOD, MaterialColor.WOOD)
                                                                      .breakByTool(FabricToolTags.AXES, Tiers.STONE.getLevel())
                                                                      .requiresCorrectToolForDrops() // End of FBS
                                                                      .strength(3.0F, 6.0F)
                                                                      .sound(SoundType.WOOD);
        BlockBehaviour.Properties diamondProperties = FabricBlockSettings.of(Material.WOOD, MaterialColor.WOOD)
                                                                         .breakByTool(FabricToolTags.AXES, Tiers.IRON.getLevel())
                                                                         .requiresCorrectToolForDrops() // End of FBS
                                                                         .strength(5.0F, 6.0F)
                                                                         .sound(SoundType.WOOD);
        BlockBehaviour.Properties obsidianProperties = FabricBlockSettings.of(Material.WOOD, MaterialColor.WOOD)
                                                                          .breakByTool(FabricToolTags.AXES, Tiers.DIAMOND.getLevel())
                                                                          .requiresCorrectToolForDrops() // End of FBS
                                                                          .strength(50.0F, 1200.0F)
                                                                          .sound(SoundType.WOOD);
        BlockBehaviour.Properties netheriteProperties = FabricBlockSettings.of(Material.WOOD, MaterialColor.WOOD)
                                                                           .breakByTool(FabricToolTags.AXES, Tiers.DIAMOND.getLevel())
                                                                           .requiresCorrectToolForDrops() // End of FBS
                                                                           .strength(50.0F, 1200.0F)
                                                                           .sound(SoundType.WOOD);
        // Init blocks
        BarrelBlock ironBarrelBlock = this.barrelBlock(Utils.resloc("iron_barrel"), ironOpenStat, Utils.IRON_TIER, ironProperties);
        BarrelBlock goldBarrelBlock = this.barrelBlock(Utils.resloc("gold_barrel"), goldOpenStat, Utils.GOLD_TIER, goldProperties);
        BarrelBlock diamondBarrelBlock = this.barrelBlock(Utils.resloc("diamond_barrel"), diamondOpenStat, Utils.DIAMOND_TIER, diamondProperties);
        BarrelBlock obsidianBarrelBlock = this.barrelBlock(Utils.resloc("obsidian_barrel"), obsidianOpenStat, Utils.OBSIDIAN_TIER, obsidianProperties);
        BarrelBlock netheriteBarrelBlock = this.barrelBlock(Utils.resloc("netherite_barrel"), netheriteOpenStat, Utils.NETHERITE_TIER, netheriteProperties);
        Set<BarrelBlock> blocks = ImmutableSet.copyOf(new BarrelBlock[]{ironBarrelBlock, goldBarrelBlock, diamondBarrelBlock, obsidianBarrelBlock, netheriteBarrelBlock});
        // Init items
        this.barrelItem(Utils.IRON_TIER, ironBarrelBlock);
        this.barrelItem(Utils.GOLD_TIER, goldBarrelBlock);
        this.barrelItem(Utils.DIAMOND_TIER, diamondBarrelBlock);
        this.barrelItem(Utils.OBSIDIAN_TIER, obsidianBarrelBlock);
        this.barrelItem(Utils.NETHERITE_TIER, netheriteBarrelBlock);
        // Init block entity type
        BlockEntityType<BarrelBlockEntity> blockEntityType = new BlockEntityType<>(() -> new BarrelBlockEntity(CommonMain.getBarrelBlockEntityType(), null), Collections.unmodifiableSet(blocks), null);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, CommonMain.BARREL_BLOCK_TYPE, blockEntityType);
        CommonMain.setBarrelBlockEntityType(blockEntityType);
        CommonMain.registerBarrelUpgradeBehaviours(TagRegistry.block(new ResourceLocation("c", "wooden_barrels")));
        // Do client side stuff
        if (PlatformUtils.getInstance().isClient()) {
            blocks.forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutoutMipped()));
        }
    }

    private BarrelBlock barrelBlock(ResourceLocation blockId, ResourceLocation stat, Tier tier, BlockBehaviour.Properties properties) {
        BarrelBlock block = Registry.register(Registry.BLOCK, blockId, new BarrelBlock(tier.getBlockProperties().apply(properties), blockId, tier.getId(), stat, tier.getSlotCount()));
        BaseApi.getInstance().registerTieredBlock(block);
        return block;
    }

    private BlockItem barrelItem(Tier tier, BarrelBlock block) {
        Item.Properties itemProperties = tier.getItemProperties().apply(new Item.Properties().tab(Utils.TAB));
        return Registry.register(Registry.ITEM, block.getBlockId(), new BlockItem(block, itemProperties));
    }

    public void chestInitialize() {
        // Init and register opening stats
        ResourceLocation woodOpenStat = CommonMain.registerStat(Utils.resloc("open_wood_chest"));
        ResourceLocation pumpkinOpenStat = CommonMain.registerStat(Utils.resloc("open_pumpkin_chest"));
        ResourceLocation christmasOpenStat = CommonMain.registerStat(Utils.resloc("open_christmas_chest"));
        ResourceLocation ironOpenStat = CommonMain.registerStat(Utils.resloc("open_iron_chest"));
        ResourceLocation goldOpenStat = CommonMain.registerStat(Utils.resloc("open_gold_chest"));
        ResourceLocation diamondOpenStat = CommonMain.registerStat(Utils.resloc("open_diamond_chest"));
        ResourceLocation obsidianOpenStat = CommonMain.registerStat(Utils.resloc("open_obsidian_chest"));
        ResourceLocation netheriteOpenStat = CommonMain.registerStat(Utils.resloc("open_netherite_chest"));
        // Init block properties
        BlockBehaviour.Properties woodProperties = FabricBlockSettings.of(Material.WOOD, MaterialColor.WOOD)
                                                                      .breakByTool(FabricToolTags.AXES, Tiers.WOOD.getLevel())
                                                                      .strength(2.5F) // End of FBS
                                                                      .sound(SoundType.WOOD);
        BlockBehaviour.Properties pumpkinProperties = FabricBlockSettings.of(Material.VEGETABLE, MaterialColor.COLOR_ORANGE)
                                                                         .breakByTool(FabricToolTags.AXES, Tiers.WOOD.getLevel())
                                                                         .strength(1.0F) // End of FBS
                                                                         .sound(SoundType.WOOD);
        BlockBehaviour.Properties christmasProperties = FabricBlockSettings.of(Material.WOOD, MaterialColor.WOOD)
                                                                           .breakByTool(FabricToolTags.AXES, Tiers.WOOD.getLevel())
                                                                           .strength(2.5F) // End of FBS
                                                                           .sound(SoundType.WOOD);
        BlockBehaviour.Properties ironProperties = FabricBlockSettings.of(Material.METAL, MaterialColor.METAL)
                                                                      .breakByTool(FabricToolTags.PICKAXES, Tiers.STONE.getLevel())
                                                                      .requiresCorrectToolForDrops() // End of FBS
                                                                      .strength(5.0F, 6.0F)
                                                                      .sound(SoundType.METAL);
        BlockBehaviour.Properties goldProperties = FabricBlockSettings.of(Material.METAL, MaterialColor.GOLD)
                                                                      .breakByTool(FabricToolTags.PICKAXES, Tiers.STONE.getLevel())
                                                                      .requiresCorrectToolForDrops() // End of FBS
                                                                      .strength(3.0F, 6.0F)
                                                                      .sound(SoundType.METAL);
        BlockBehaviour.Properties diamondProperties = FabricBlockSettings.of(Material.METAL, MaterialColor.DIAMOND)
                                                                         .breakByTool(FabricToolTags.PICKAXES, Tiers.IRON.getLevel())
                                                                         .requiresCorrectToolForDrops() // End of FBS
                                                                         .strength(5.0F, 6.0F)
                                                                         .sound(SoundType.METAL);
        BlockBehaviour.Properties obsidianProperties = FabricBlockSettings.of(Material.STONE, MaterialColor.COLOR_BLACK)
                                                                          .breakByTool(FabricToolTags.PICKAXES, Tiers.DIAMOND.getLevel())
                                                                          .requiresCorrectToolForDrops() // End of FBS
                                                                          .strength(50.0F, 1200.0F);
        BlockBehaviour.Properties netheriteProperties = FabricBlockSettings.of(Material.METAL, MaterialColor.COLOR_BLACK)
                                                                           .breakByTool(FabricToolTags.PICKAXES, Tiers.DIAMOND.getLevel())
                                                                           .requiresCorrectToolForDrops() // End of FBS
                                                                           .strength(50.0F, 1200.0F)
                                                                           .sound(SoundType.NETHERITE_BLOCK);
        // Init and register blocks
        ChestBlock woodChestBlock = this.chestBlock(Utils.resloc("wood_chest"), woodOpenStat, Utils.WOOD_TIER, woodProperties);
        ChestBlock pumpkinChestBlock = this.chestBlock(Utils.resloc("pumpkin_chest"), pumpkinOpenStat, Utils.WOOD_TIER, pumpkinProperties);
        ChestBlock christmasChestBlock = this.chestBlock(Utils.resloc("christmas_chest"), christmasOpenStat, Utils.WOOD_TIER, christmasProperties);
        ChestBlock ironChestBlock = this.chestBlock(Utils.resloc("iron_chest"), ironOpenStat, Utils.IRON_TIER, ironProperties);
        ChestBlock goldChestBlock = this.chestBlock(Utils.resloc("gold_chest"), goldOpenStat, Utils.GOLD_TIER, goldProperties);
        ChestBlock diamondChestBlock = this.chestBlock(Utils.resloc("diamond_chest"), diamondOpenStat, Utils.DIAMOND_TIER, diamondProperties);
        ChestBlock obsidianChestBlock = this.chestBlock(Utils.resloc("obsidian_chest"), obsidianOpenStat, Utils.OBSIDIAN_TIER, obsidianProperties);
        ChestBlock netheriteChestBlock = this.chestBlock(Utils.resloc("netherite_chest"), netheriteOpenStat, Utils.NETHERITE_TIER, netheriteProperties);
        Set<ChestBlock> blocks = ImmutableSet.copyOf(new ChestBlock[]{woodChestBlock, pumpkinChestBlock, christmasChestBlock, ironChestBlock, goldChestBlock, diamondChestBlock, obsidianChestBlock, netheriteChestBlock});
        // Init and register items
        BlockItem woodChestItem = this.chestItem(Utils.WOOD_TIER, woodChestBlock);
        BlockItem pumpkinChestItem = this.chestItem(Utils.WOOD_TIER, pumpkinChestBlock);
        BlockItem christmasChestItem = this.chestItem(Utils.WOOD_TIER, christmasChestBlock);
        BlockItem ironChestItem = this.chestItem(Utils.IRON_TIER, ironChestBlock);
        BlockItem goldChestItem = this.chestItem(Utils.GOLD_TIER, goldChestBlock);
        BlockItem diamondChestItem = this.chestItem(Utils.DIAMOND_TIER, diamondChestBlock);
        BlockItem obsidianChestItem = this.chestItem(Utils.OBSIDIAN_TIER, obsidianChestBlock);
        BlockItem netheriteChestItem = this.chestItem(Utils.NETHERITE_TIER, netheriteChestBlock);
        Set<BlockItem> items = ImmutableSet.copyOf(new BlockItem[]{woodChestItem, pumpkinChestItem, christmasChestItem, ironChestItem, goldChestItem, diamondChestItem, obsidianChestItem, netheriteChestItem});
        // Init and register block entity type
        BlockEntityType<ChestBlockEntity> blockEntityType = new BlockEntityType<>(() -> new ChestBlockEntity(CommonMain.getChestBlockEntityType(), null), ImmutableSet.copyOf(blocks), null);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, CommonMain.CHEST_BLOCK_TYPE, blockEntityType);
        CommonMain.setChestBlockEntityType(blockEntityType);
        CommonMain.registerChestUpgradeBehaviours(TagRegistry.block(new ResourceLocation("c", "wooden_chests")));
        // Do client side stuff
        if (PlatformUtils.getInstance().isClient()) {
            Client.initialise(blocks, items);
        }
    }

    private ChestBlock chestBlock(ResourceLocation blockId, ResourceLocation stat, Tier tier, BlockBehaviour.Properties properties) {
        tier.getBlockProperties().apply(properties.dynamicShape());
        ChestBlock block = Registry.register(Registry.BLOCK, blockId, new ChestBlock(properties, blockId, tier.getId(), stat, tier.getSlotCount()));
        BaseApi.getInstance().registerTieredBlock(block);
        return block;
    }

    private BlockItem chestItem(Tier tier, ChestBlock block) {
        Item.Properties itemProperties = tier.getItemProperties().apply(new Item.Properties().tab(Utils.TAB));
        return Registry.register(Registry.ITEM, block.getBlockId(), new BlockItem(block, itemProperties));
    }

    private static class Client {
        private static void initialise(Set<ChestBlock> blocks, Set<BlockItem> items) {
            CommonMain.registerChestTextures(blocks);
            ClientSpriteRegistryCallback.event(Sheets.CHEST_SHEET).register((atlasTexture, registry) -> CommonMain.getChestTextures(blocks).forEach(registry::register));

            BlockEntityRendererRegistry.INSTANCE.register(CommonMain.getChestBlockEntityType(), ChestBlockEntityRenderer::new);

            items.forEach(item -> {
                ChestBlockEntity renderEntity = new ChestBlockEntity(CommonMain.getChestBlockEntityType(), ((ChestBlock) item.getBlock()).getBlockId());
                BuiltinItemRendererRegistry.INSTANCE.register(item, (itemStack, transform, stack, source, light, overlay) ->
                        BlockEntityRenderDispatcher.instance.renderItem(renderEntity, stack, source, light, overlay));
            });
        }
    }

    private void oldChestInitialize() {
        // Init and register opening stats
        ResourceLocation woodOpenStat = CommonMain.registerStat(Utils.resloc("open_old_wood_chest"));
        ResourceLocation ironOpenStat = CommonMain.registerStat(Utils.resloc("open_old_iron_chest"));
        ResourceLocation goldOpenStat = CommonMain.registerStat(Utils.resloc("open_old_gold_chest"));
        ResourceLocation diamondOpenStat = CommonMain.registerStat(Utils.resloc("open_old_diamond_chest"));
        ResourceLocation obsidianOpenStat = CommonMain.registerStat(Utils.resloc("open_old_obsidian_chest"));
        ResourceLocation netheriteOpenStat = CommonMain.registerStat(Utils.resloc("open_old_netherite_chest"));
        // Init block properties
        BlockBehaviour.Properties woodProperties = FabricBlockSettings.of(Material.WOOD, MaterialColor.WOOD)
                                                                      .breakByTool(FabricToolTags.AXES, Tiers.WOOD.getLevel())
                                                                      .strength(2.5F) // End of FBS
                                                                      .sound(SoundType.WOOD);
        BlockBehaviour.Properties ironProperties = FabricBlockSettings.of(Material.METAL, MaterialColor.METAL)
                                                                      .breakByTool(FabricToolTags.PICKAXES, Tiers.STONE.getLevel())
                                                                      .requiresCorrectToolForDrops() // End of FBS
                                                                      .strength(5.0F, 6.0F)
                                                                      .sound(SoundType.METAL);
        BlockBehaviour.Properties goldProperties = FabricBlockSettings.of(Material.METAL, MaterialColor.GOLD)
                                                                      .breakByTool(FabricToolTags.PICKAXES, Tiers.STONE.getLevel())
                                                                      .requiresCorrectToolForDrops() // End of FBS
                                                                      .strength(3.0F, 6.0F)
                                                                      .sound(SoundType.METAL);
        BlockBehaviour.Properties diamondProperties = FabricBlockSettings.of(Material.METAL, MaterialColor.DIAMOND)
                                                                         .breakByTool(FabricToolTags.PICKAXES, Tiers.IRON.getLevel())
                                                                         .requiresCorrectToolForDrops() // End of FBS
                                                                         .strength(5.0F, 6.0F)
                                                                         .sound(SoundType.METAL);
        BlockBehaviour.Properties obsidianProperties = FabricBlockSettings.of(Material.STONE, MaterialColor.COLOR_BLACK)
                                                                          .breakByTool(FabricToolTags.PICKAXES, Tiers.DIAMOND.getLevel())
                                                                          .requiresCorrectToolForDrops() // End of FBS
                                                                          .strength(50.0F, 1200.0F);
        BlockBehaviour.Properties netheriteProperties = FabricBlockSettings.of(Material.METAL, MaterialColor.COLOR_BLACK)
                                                                           .breakByTool(FabricToolTags.PICKAXES, Tiers.DIAMOND.getLevel())
                                                                           .requiresCorrectToolForDrops() // End of FBS
                                                                           .strength(50.0F, 1200.0F)
                                                                           .sound(SoundType.NETHERITE_BLOCK);
        // Init blocks
        OldChestBlock woodChestBlock = this.oldChestBlock(Utils.resloc("old_wood_chest"), woodOpenStat, Utils.WOOD_TIER, woodProperties);
        OldChestBlock ironChestBlock = this.oldChestBlock(Utils.resloc("old_iron_chest"), ironOpenStat, Utils.IRON_TIER, ironProperties);
        OldChestBlock goldChestBlock = this.oldChestBlock(Utils.resloc("old_gold_chest"), goldOpenStat, Utils.GOLD_TIER, goldProperties);
        OldChestBlock diamondChestBlock = this.oldChestBlock(Utils.resloc("old_diamond_chest"), diamondOpenStat, Utils.DIAMOND_TIER, diamondProperties);
        OldChestBlock obsidianChestBlock = this.oldChestBlock(Utils.resloc("old_obsidian_chest"), obsidianOpenStat, Utils.OBSIDIAN_TIER, obsidianProperties);
        OldChestBlock netheriteChestBlock = this.oldChestBlock(Utils.resloc("old_netherite_chest"), netheriteOpenStat, Utils.NETHERITE_TIER, netheriteProperties);
        Set<OldChestBlock> blocks = ImmutableSet.copyOf(new OldChestBlock[]{woodChestBlock, ironChestBlock, goldChestBlock, diamondChestBlock, obsidianChestBlock, netheriteChestBlock});
        // Init items
        this.oldChestItem(Utils.WOOD_TIER, woodChestBlock);
        this.oldChestItem(Utils.IRON_TIER, ironChestBlock);
        this.oldChestItem(Utils.GOLD_TIER, goldChestBlock);
        this.oldChestItem(Utils.DIAMOND_TIER, diamondChestBlock);
        this.oldChestItem(Utils.OBSIDIAN_TIER, obsidianChestBlock);
        this.oldChestItem(Utils.NETHERITE_TIER, netheriteChestBlock);
        // Init block entity type
        BlockEntityType<OldChestBlockEntity> blockEntityType = new BlockEntityType<>(() -> new OldChestBlockEntity(CommonMain.getOldChestBlockEntityType(), null), Collections.unmodifiableSet(blocks), null);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, CommonMain.OLD_CHEST_BLOCK_TYPE, blockEntityType);
        CommonMain.setOldChestBlockEntityType(blockEntityType);
        CommonMain.registerOldChestUpgradeBehaviours();
    }

    private BlockItem oldChestItem(Tier tier, OldChestBlock block) {
        Item.Properties itemProperties = tier.getItemProperties().apply(new Item.Properties().tab(Utils.TAB));
        return Registry.register(Registry.ITEM, block.getBlockId(), new BlockItem(block, itemProperties));
    }

    private OldChestBlock oldChestBlock(ResourceLocation blockId, ResourceLocation stat, Tier tier, BlockBehaviour.Properties properties) {
        tier.getBlockProperties().apply(properties);
        OldChestBlock block = Registry.register(Registry.BLOCK, blockId, new OldChestBlock(properties, blockId, tier.getId(), stat, tier.getSlotCount()));
        BaseApi.getInstance().registerTieredBlock(block);
        return block;
    }
}
