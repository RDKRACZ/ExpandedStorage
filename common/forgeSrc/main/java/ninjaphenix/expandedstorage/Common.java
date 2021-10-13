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

import ninjaphenix.expandedstorage.block.BarrelBlock;
import ninjaphenix.expandedstorage.block.ChestBlock;
import ninjaphenix.expandedstorage.block.OldChestBlock;
import ninjaphenix.expandedstorage.block.misc.BarrelBlockEntity;
import ninjaphenix.expandedstorage.block.misc.ChestBlockEntity;
import ninjaphenix.expandedstorage.block.misc.AbstractChestBlockEntity;
import ninjaphenix.expandedstorage.block.AbstractOpenableStorageBlock;
import ninjaphenix.expandedstorage.block.AbstractStorageBlock;
import ninjaphenix.expandedstorage.block.misc.CursedChestType;
import ninjaphenix.expandedstorage.item.BlockUpgradeBehaviour;
import ninjaphenix.expandedstorage.tier.Tier;
import ninjaphenix.expandedstorage.item.StorageConversionKit;
import ninjaphenix.expandedstorage.item.StorageMutator;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.tags.Tag;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public final class Common {
    public static final CreativeModeTab GROUP = PlatformUtils.getInstance().createTab(() -> new ItemStack(Registry.ITEM.get(Utils.id("netherite_chest"))));
    public static final ResourceLocation BARREL_BLOCK_TYPE = Utils.id("barrel");
    public static final ResourceLocation CHEST_BLOCK_TYPE = Utils.id("cursed_chest");
    public static final ResourceLocation OLD_CHEST_BLOCK_TYPE = Utils.id("old_cursed_chest");

    private static final Map<Predicate<Block>, BlockUpgradeBehaviour> BLOCK_UPGRADE_BEHAVIOURS = new HashMap<>();
    private static final Map<BlockTierId, AbstractStorageBlock> BLOCKS = new HashMap<>();
    private static final Map<ResourceLocation, TextureCollection> CHEST_TEXTURES = PlatformUtils.getInstance().isClient() ? new HashMap<>() : null;

    private static final int IRON_STACK_COUNT = 54;
    private static final int GOLD_STACK_COUNT = 81;
    private static final int DIAMOND_STACK_COUNT = 108;
    private static final int OBSIDIAN_STACK_COUNT = 108;
    private static final int NETHERITE_STACK_COUNT = 135;
    private static final Tier IRON_TIER = new Tier(Utils.id("iron"), Common.IRON_STACK_COUNT, Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
    private static final Tier GOLD_TIER = new Tier(Utils.id("gold"), Common.GOLD_STACK_COUNT, Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
    private static final Tier DIAMOND_TIER = new Tier(Utils.id("diamond"), Common.DIAMOND_STACK_COUNT, Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
    private static final Tier OBSIDIAN_TIER = new Tier(Utils.id("obsidian"), Common.OBSIDIAN_STACK_COUNT, Properties::requiresCorrectToolForDrops, UnaryOperator.identity());
    private static final Tier NETHERITE_TIER = new Tier(Utils.id("netherite"), Common.NETHERITE_STACK_COUNT, Properties::requiresCorrectToolForDrops, Item.Properties::fireResistant);
    private static BlockEntityType<ChestBlockEntity> chestBlockEntityType;
    private static BlockEntityType<AbstractChestBlockEntity> oldChestBlockEntityType;
    private static BlockEntityType<BarrelBlockEntity> barrelBlockEntityType;

    public static BlockEntityType<ChestBlockEntity> getChestBlockEntityType() {
        return chestBlockEntityType;
    }

    public static BlockEntityType<AbstractChestBlockEntity> getOldChestBlockEntityType() {
        return oldChestBlockEntityType;
    }

    public static BlockEntityType<BarrelBlockEntity> getBarrelBlockEntityType() {
        return barrelBlockEntityType;
    }

    private static ChestBlock chestBlock(ResourceLocation blockId, ResourceLocation stat, Tier tier, Properties settings) {
        ChestBlock block = new ChestBlock(tier.getBlockSettings().apply(settings.dynamicShape()), blockId, tier.getId(), stat, tier.getSlotCount());
        Common.registerTieredBlock(block);
        return block;
    }

    private static BlockItem chestItem(Tier tier, ChestBlock block, BiFunction<Block, Item.Properties, BlockItem> blockItemMaker) {
        return blockItemMaker.apply(block, tier.getItemSettings().apply(new Item.Properties().tab(GROUP)));
    }

    private static BlockItem oldChestItem(Tier tier, OldChestBlock block) {
        return new BlockItem(block, tier.getItemSettings().apply(new Item.Properties().tab(GROUP)));
    }

    private static OldChestBlock oldChestBlock(ResourceLocation blockId, ResourceLocation stat, Tier tier, Properties settings) {
        OldChestBlock block = new OldChestBlock(tier.getBlockSettings().apply(settings), blockId, tier.getId(), stat, tier.getSlotCount());
        Common.registerTieredBlock(block);
        return block;
    }

    private static BarrelBlock barrelBlock(ResourceLocation blockId, ResourceLocation stat, Tier tier, Properties settings) {
        BarrelBlock block = new BarrelBlock(tier.getBlockSettings().apply(settings), blockId, tier.getId(), stat, tier.getSlotCount());
        Common.registerTieredBlock(block);
        return block;
    }

    private static BlockItem barrelItem(Tier tier, BarrelBlock block) {
        return new BlockItem(block, tier.getItemSettings().apply(new Item.Properties().tab(GROUP)));
    }

    static void registerChestContent(RegistrationConsumer<ChestBlock, BlockItem, ChestBlockEntity> registrationConsumer, Tag<Block> woodenChestTag, BiFunction<Block, Item.Properties, BlockItem> blockItemMaker) {
        // Init and register opening stats
        ResourceLocation woodOpenStat = Common.registerStat(Utils.id("open_wood_chest"));
        ResourceLocation pumpkinOpenStat = Common.registerStat(Utils.id("open_pumpkin_chest"));
        ResourceLocation christmasOpenStat = Common.registerStat(Utils.id("open_christmas_chest"));
        ResourceLocation ironOpenStat = Common.registerStat(Utils.id("open_iron_chest"));
        ResourceLocation goldOpenStat = Common.registerStat(Utils.id("open_gold_chest"));
        ResourceLocation diamondOpenStat = Common.registerStat(Utils.id("open_diamond_chest"));
        ResourceLocation obsidianOpenStat = Common.registerStat(Utils.id("open_obsidian_chest"));
        ResourceLocation netheriteOpenStat = Common.registerStat(Utils.id("open_netherite_chest"));
        // Init block properties
        Properties woodSettings = Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.5f).sound(SoundType.WOOD);
        Properties pumpkinSettings = Properties.of(Material.VEGETABLE, MaterialColor.COLOR_ORANGE).strength(1).sound(SoundType.WOOD);
        // todo: use mapColorProvider, would require mixins...
        Properties christmasSettings = Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.5f).sound(SoundType.WOOD);
        Properties ironSettings = Properties.of(Material.METAL, MaterialColor.METAL).strength(5, 6).sound(SoundType.METAL);
        Properties goldSettings = Properties.of(Material.METAL, MaterialColor.GOLD).strength(3, 6).sound(SoundType.METAL);
        Properties diamondSettings = Properties.of(Material.METAL, MaterialColor.DIAMOND).strength(5, 6).sound(SoundType.METAL);
        Properties obsidianSettings = Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).strength(50, 1200);
        Properties netheriteSettings = Properties.of(Material.METAL, MaterialColor.COLOR_BLACK).strength(50, 1200).sound(SoundType.NETHERITE_BLOCK);
        // Init and register blocks
        ChestBlock woodChestBlock = Common.chestBlock(Utils.id("wood_chest"), woodOpenStat, Utils.WOOD_TIER, woodSettings);
        ChestBlock pumpkinChestBlock = Common.chestBlock(Utils.id("pumpkin_chest"), pumpkinOpenStat, Utils.WOOD_TIER, pumpkinSettings);
        ChestBlock christmasChestBlock = Common.chestBlock(Utils.id("christmas_chest"), christmasOpenStat, Utils.WOOD_TIER, christmasSettings);
        ChestBlock ironChestBlock = Common.chestBlock(Utils.id("iron_chest"), ironOpenStat, Common.IRON_TIER, ironSettings);
        ChestBlock goldChestBlock = Common.chestBlock(Utils.id("gold_chest"), goldOpenStat, Common.GOLD_TIER, goldSettings);
        ChestBlock diamondChestBlock = Common.chestBlock(Utils.id("diamond_chest"), diamondOpenStat, Common.DIAMOND_TIER, diamondSettings);
        ChestBlock obsidianChestBlock = Common.chestBlock(Utils.id("obsidian_chest"), obsidianOpenStat, Common.OBSIDIAN_TIER, obsidianSettings);
        ChestBlock netheriteChestBlock = Common.chestBlock(Utils.id("netherite_chest"), netheriteOpenStat, Common.NETHERITE_TIER, netheriteSettings);
        ChestBlock[] blocks = new ChestBlock[]{woodChestBlock, pumpkinChestBlock, christmasChestBlock, ironChestBlock, goldChestBlock, diamondChestBlock, obsidianChestBlock, netheriteChestBlock};
        if (PlatformUtils.getInstance().isClient()) {
            Common.registerChestTextures(blocks);
        }
        // Init and register items
        BlockItem woodChestItem = Common.chestItem(Utils.WOOD_TIER, woodChestBlock, blockItemMaker);
        BlockItem pumpkinChestItem = Common.chestItem(Utils.WOOD_TIER, pumpkinChestBlock, blockItemMaker);
        BlockItem christmasChestItem = Common.chestItem(Utils.WOOD_TIER, christmasChestBlock, blockItemMaker);
        BlockItem ironChestItem = Common.chestItem(Common.IRON_TIER, ironChestBlock, blockItemMaker);
        BlockItem goldChestItem = Common.chestItem(Common.GOLD_TIER, goldChestBlock, blockItemMaker);
        BlockItem diamondChestItem = Common.chestItem(Common.DIAMOND_TIER, diamondChestBlock, blockItemMaker);
        BlockItem obsidianChestItem = Common.chestItem(Common.OBSIDIAN_TIER, obsidianChestBlock, blockItemMaker);
        BlockItem netheriteChestItem = Common.chestItem(Common.NETHERITE_TIER, netheriteChestBlock, blockItemMaker);
        BlockItem[] items = new BlockItem[]{woodChestItem, pumpkinChestItem, christmasChestItem, ironChestItem, goldChestItem, diamondChestItem, obsidianChestItem, netheriteChestItem};
        // Init and register block entity type
        chestBlockEntityType = BlockEntityType.Builder.of((pos, state) -> new ChestBlockEntity(Common.getChestBlockEntityType(), pos, state), blocks).build(null);
        registrationConsumer.accept(blocks, items, chestBlockEntityType);
        // Register chest module icon & upgrade behaviours
        Predicate<Block> isUpgradableChestBlock = (block) -> block instanceof ChestBlock || block instanceof net.minecraft.world.level.block.ChestBlock || woodenChestTag.contains(block);
        Common.defineBlockUpgradeBehaviour(isUpgradableChestBlock, Common::tryUpgradeBlockToChest);
    }

    static void registerOldChestContent(RegistrationConsumer<OldChestBlock, BlockItem, AbstractChestBlockEntity> registrationConsumer) {
        // Init and register opening stats
        ResourceLocation woodOpenStat = Common.registerStat(Utils.id("open_old_wood_chest"));
        ResourceLocation ironOpenStat = Common.registerStat(Utils.id("open_old_iron_chest"));
        ResourceLocation goldOpenStat = Common.registerStat(Utils.id("open_old_gold_chest"));
        ResourceLocation diamondOpenStat = Common.registerStat(Utils.id("open_old_diamond_chest"));
        ResourceLocation obsidianOpenStat = Common.registerStat(Utils.id("open_old_obsidian_chest"));
        ResourceLocation netheriteOpenStat = Common.registerStat(Utils.id("open_old_netherite_chest"));
        // Init block properties
        Properties woodSettings = Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.5f).sound(SoundType.WOOD);
        Properties ironSettings = Properties.of(Material.METAL, MaterialColor.METAL).strength(5, 6).sound(SoundType.METAL);
        Properties goldSettings = Properties.of(Material.METAL, MaterialColor.GOLD).strength(3, 6).sound(SoundType.METAL);
        Properties diamondSettings = Properties.of(Material.METAL, MaterialColor.DIAMOND).strength(5, 6).sound(SoundType.METAL);
        Properties obsidianSettings = Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).strength(50, 1200);
        Properties netheriteSettings = Properties.of(Material.METAL, MaterialColor.COLOR_BLACK).strength(50, 1200).sound(SoundType.NETHERITE_BLOCK);
        // Init blocks
        OldChestBlock woodChestBlock = Common.oldChestBlock(Utils.id("old_wood_chest"), woodOpenStat, Utils.WOOD_TIER, woodSettings);
        OldChestBlock ironChestBlock = Common.oldChestBlock(Utils.id("old_iron_chest"), ironOpenStat, Common.IRON_TIER, ironSettings);
        OldChestBlock goldChestBlock = Common.oldChestBlock(Utils.id("old_gold_chest"), goldOpenStat, Common.GOLD_TIER, goldSettings);
        OldChestBlock diamondChestBlock = Common.oldChestBlock(Utils.id("old_diamond_chest"), diamondOpenStat, Common.DIAMOND_TIER, diamondSettings);
        OldChestBlock obsidianChestBlock = Common.oldChestBlock(Utils.id("old_obsidian_chest"), obsidianOpenStat, Common.OBSIDIAN_TIER, obsidianSettings);
        OldChestBlock netheriteChestBlock = Common.oldChestBlock(Utils.id("old_netherite_chest"), netheriteOpenStat, Common.NETHERITE_TIER, netheriteSettings);
        OldChestBlock[] blocks = new OldChestBlock[]{woodChestBlock, ironChestBlock, goldChestBlock, diamondChestBlock, obsidianChestBlock, netheriteChestBlock};
        // Init items
        BlockItem woodChestItem = Common.oldChestItem(Utils.WOOD_TIER, woodChestBlock);
        BlockItem ironChestItem = Common.oldChestItem(Common.IRON_TIER, ironChestBlock);
        BlockItem goldChestItem = Common.oldChestItem(Common.GOLD_TIER, goldChestBlock);
        BlockItem diamondChestItem = Common.oldChestItem(Common.DIAMOND_TIER, diamondChestBlock);
        BlockItem obsidianChestItem = Common.oldChestItem(Common.OBSIDIAN_TIER, obsidianChestBlock);
        BlockItem netheriteChestItem = Common.oldChestItem(Common.NETHERITE_TIER, netheriteChestBlock);
        BlockItem[] items = new BlockItem[]{woodChestItem, ironChestItem, goldChestItem, diamondChestItem, obsidianChestItem, netheriteChestItem};
        // Init block entity type
        oldChestBlockEntityType = BlockEntityType.Builder.of((pos, state) -> new AbstractChestBlockEntity(Common.getOldChestBlockEntityType(), pos, state, ((AbstractStorageBlock) state.getBlock()).getBlockId()), blocks).build(null);
        registrationConsumer.accept(blocks, items, oldChestBlockEntityType);
        // Register chest module icon & upgrade behaviours
        Predicate<Block> isUpgradableChestBlock = (block) -> block instanceof OldChestBlock;
        Common.defineBlockUpgradeBehaviour(isUpgradableChestBlock, Common::tryUpgradeBlockToOldChest);
    }

    static void registerBarrelContent(RegistrationConsumer<BarrelBlock, BlockItem, BarrelBlockEntity> registration, Tag<Block> woodenBarrelTag) {
        // Init and register opening stats
        ResourceLocation ironOpenStat = Common.registerStat(Utils.id("open_iron_barrel"));
        ResourceLocation goldOpenStat = Common.registerStat(Utils.id("open_gold_barrel"));
        ResourceLocation diamondOpenStat = Common.registerStat(Utils.id("open_diamond_barrel"));
        ResourceLocation obsidianOpenStat = Common.registerStat(Utils.id("open_obsidian_barrel"));
        ResourceLocation netheriteOpenStat = Common.registerStat(Utils.id("open_netherite_barrel"));
        // Init block properties
        Properties ironSettings = Properties.of(Material.WOOD).strength(5, 6).sound(SoundType.WOOD);
        Properties goldSettings = Properties.of(Material.WOOD).strength(3, 6).sound(SoundType.WOOD);
        Properties diamondSettings = Properties.of(Material.WOOD).strength(5, 6).sound(SoundType.WOOD);
        Properties obsidianSettings = Properties.of(Material.WOOD).strength(50, 1200).sound(SoundType.WOOD);
        Properties netheriteSettings = Properties.of(Material.WOOD).strength(50, 1200).sound(SoundType.WOOD);
        // Init blocks
        BarrelBlock ironBarrelBlock = Common.barrelBlock(Utils.id("iron_barrel"), ironOpenStat, Common.IRON_TIER, ironSettings);
        BarrelBlock goldBarrelBlock = Common.barrelBlock(Utils.id("gold_barrel"), goldOpenStat, Common.GOLD_TIER, goldSettings);
        BarrelBlock diamondBarrelBlock = Common.barrelBlock(Utils.id("diamond_barrel"), diamondOpenStat, Common.DIAMOND_TIER, diamondSettings);
        BarrelBlock obsidianBarrelBlock = Common.barrelBlock(Utils.id("obsidian_barrel"), obsidianOpenStat, Common.OBSIDIAN_TIER, obsidianSettings);
        BarrelBlock netheriteBarrelBlock = Common.barrelBlock(Utils.id("netherite_barrel"), netheriteOpenStat, Common.NETHERITE_TIER, netheriteSettings);
        BarrelBlock[] blocks = new BarrelBlock[]{ironBarrelBlock, goldBarrelBlock, diamondBarrelBlock, obsidianBarrelBlock, netheriteBarrelBlock};
        // Init items
        BlockItem ironBarrelItem = Common.barrelItem(Common.IRON_TIER, ironBarrelBlock);
        BlockItem goldBarrelItem = Common.barrelItem(Common.GOLD_TIER, goldBarrelBlock);
        BlockItem diamondBarrelItem = Common.barrelItem(Common.DIAMOND_TIER, diamondBarrelBlock);
        BlockItem obsidianBarrelItem = Common.barrelItem(Common.OBSIDIAN_TIER, obsidianBarrelBlock);
        BlockItem netheriteBarrelItem = Common.barrelItem(Common.NETHERITE_TIER, netheriteBarrelBlock);
        BlockItem[] items = new BlockItem[]{ironBarrelItem, goldBarrelItem, diamondBarrelItem, obsidianBarrelItem, netheriteBarrelItem};
        // Init block entity type
        barrelBlockEntityType = BlockEntityType.Builder.of((pos, state) -> new BarrelBlockEntity(Common.getBarrelBlockEntityType(), pos, state), blocks).build(null);
        registration.accept(blocks, items, barrelBlockEntityType);
        // Register chest module icon & upgrade behaviours
        Predicate<Block> isUpgradableBarrelBlock = (block) -> block instanceof BarrelBlock || block instanceof net.minecraft.world.level.block.BarrelBlock || woodenBarrelTag.contains(block);
        Common.defineBlockUpgradeBehaviour(isUpgradableBarrelBlock, Common::tryUpgradeBlockToBarrel);
    }

    private static boolean tryUpgradeBlockToBarrel(UseOnContext context, ResourceLocation from, ResourceLocation to) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        boolean isExpandedStorageBarrel = block instanceof BarrelBlock;
        int containerSize = !isExpandedStorageBarrel ? Utils.WOOD_STACK_COUNT : ((BarrelBlock) Common.getTieredBlock(BARREL_BLOCK_TYPE, ((BarrelBlock) block).getBlockTier())).getSlotCount();
        if (isExpandedStorageBarrel && ((BarrelBlock) block).getBlockTier() == from || !isExpandedStorageBarrel && from == Utils.WOOD_TIER.getId()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            //noinspection ConstantConditions
            CompoundTag tag = blockEntity.save(new CompoundTag());
            boolean verifiedSize = blockEntity instanceof Container container && container.getContainerSize() == containerSize;
            if (!verifiedSize) { // Cannot verify container size, we'll let it upgrade if it has or has less than 27 items
                if (tag.contains("Items", net.minecraft.nbt.Tag.TAG_LIST)) {
                    ListTag items = tag.getList("Items", net.minecraft.nbt.Tag.TAG_COMPOUND);
                    if (items.size() <= containerSize) {
                        verifiedSize = true;
                    }
                }
            }
            if (verifiedSize) {
                AbstractOpenableStorageBlock toBlock = (AbstractOpenableStorageBlock) Common.getTieredBlock(BARREL_BLOCK_TYPE, to);
                NonNullList<ItemStack> inventory = NonNullList.withSize(toBlock.getSlotCount(), ItemStack.EMPTY);
                LockCode code = LockCode.fromTag(tag);
                ContainerHelper.loadAllItems(tag, inventory);
                world.removeBlockEntity(pos);
                BlockState newState = toBlock.defaultBlockState().setValue(BlockStateProperties.FACING, state.getValue(BlockStateProperties.FACING));
                if (world.setBlockAndUpdate(pos, newState)) {
                    BlockEntity newEntity = world.getBlockEntity(pos);
                    //noinspection ConstantConditions
                    CompoundTag newTag = newEntity.save(new CompoundTag());
                    ContainerHelper.saveAllItems(newTag, inventory);
                    code.addToTag(newTag);
                    newEntity.load(newTag);
                    context.getItemInHand().shrink(1);
                    return true;
                }
            }
        }
        return false;
    }

    static ResourceLocation[] getChestTextures(ChestBlock[] blocks) {
        ResourceLocation[] textures = new ResourceLocation[blocks.length * CursedChestType.values().length];
        int index = 0;
        for (ChestBlock block : blocks) {
            ResourceLocation blockId = block.getBlockId();
            for (CursedChestType type : CursedChestType.values()) {
                textures[index++] = Common.getChestTexture(blockId, type);
            }
        }
        return textures;
    }

    static void registerChestTextures(ChestBlock[] blocks) {
        for (ChestBlock block : blocks) {
            ResourceLocation blockId = block.getBlockId();
            Common.declareChestTextures(
                    blockId, Utils.id("entity/" + blockId.getPath() + "/single"),
                    Utils.id("entity/" + blockId.getPath() + "/left"),
                    Utils.id("entity/" + blockId.getPath() + "/right"),
                    Utils.id("entity/" + blockId.getPath() + "/top"),
                    Utils.id("entity/" + blockId.getPath() + "/bottom"),
                    Utils.id("entity/" + blockId.getPath() + "/front"),
                    Utils.id("entity/" + blockId.getPath() + "/back"));
        }
    }

    private static boolean tryUpgradeBlockToChest(UseOnContext context, ResourceLocation from, ResourceLocation to) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack handStack = context.getItemInHand();
        if (state.getBlock() instanceof ChestBlock) {
            if (ChestBlock.getBlockType(state) == DoubleBlockCombiner.BlockType.SINGLE) {
                Common.upgradeSingleBlockToChest(world, state, pos, from, to);
                handStack.shrink(1);
                return true;
            } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
                BlockPos otherPos = pos.relative(ChestBlock.getDirectionToAttached(state));
                BlockState otherState = world.getBlockState(otherPos);
                Common.upgradeSingleBlockToChest(world, state, pos, from, to);
                Common.upgradeSingleBlockToChest(world, otherState, otherPos, from, to);
                handStack.shrink(2);
                return true;
            }
        } else {
            if (net.minecraft.world.level.block.ChestBlock.getBlockType(state) == DoubleBlockCombiner.BlockType.SINGLE) {
                Common.upgradeSingleBlockToChest(world, state, pos, from, to);
                handStack.shrink(1);
                return true;
            } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
                BlockPos otherPos = pos.relative(net.minecraft.world.level.block.ChestBlock.getConnectedDirection(state));
                BlockState otherState = world.getBlockState(otherPos);
                Common.upgradeSingleBlockToChest(world, state, pos, from, to);
                Common.upgradeSingleBlockToChest(world, otherState, otherPos, from, to);
                handStack.shrink(2);
                return true;
            }
        }

        return false;
    }

    private static void upgradeSingleBlockToChest(Level world, BlockState state, BlockPos pos, ResourceLocation from, ResourceLocation to) {
        Block block = state.getBlock();
        boolean isExpandedStorageChest = block instanceof ChestBlock;
        int containerSize = !isExpandedStorageChest ? Utils.WOOD_STACK_COUNT : ((ChestBlock) Common.getTieredBlock(CHEST_BLOCK_TYPE, ((ChestBlock) block).getBlockTier())).getSlotCount();
        if (isExpandedStorageChest && ((ChestBlock) block).getBlockTier() == from || !isExpandedStorageChest && from == Utils.WOOD_TIER.getId()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            //noinspection ConstantConditions
            CompoundTag tag = blockEntity.save(new CompoundTag());
            boolean verifiedSize = blockEntity instanceof Container container && container.getContainerSize() == containerSize;
            if (!verifiedSize) { // Cannot verify container size, we'll let it upgrade if it has or has less than 27 items
                if (tag.contains("Items", net.minecraft.nbt.Tag.TAG_LIST)) {
                    ListTag items = tag.getList("Items", net.minecraft.nbt.Tag.TAG_COMPOUND);
                    if (items.size() <= containerSize) {
                        verifiedSize = true;
                    }
                }
            }
            if (verifiedSize) {
                AbstractOpenableStorageBlock toBlock = (AbstractOpenableStorageBlock) Common.getTieredBlock(CHEST_BLOCK_TYPE, to);
                NonNullList<ItemStack> inventory = NonNullList.withSize(toBlock.getSlotCount(), ItemStack.EMPTY);
                LockCode code = LockCode.fromTag(tag);
                ContainerHelper.loadAllItems(tag, inventory);
                world.removeBlockEntity(pos);
                // Needs fixing up to check for vanilla states.
                BlockState newState = toBlock.defaultBlockState()
                                             .setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING))
                                             .setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
                if (state.hasProperty(ChestBlock.CURSED_CHEST_TYPE)) {
                    newState = newState.setValue(ChestBlock.CURSED_CHEST_TYPE, state.getValue(ChestBlock.CURSED_CHEST_TYPE));
                } else if (state.hasProperty(BlockStateProperties.CHEST_TYPE)) {
                    ChestType type = state.getValue(BlockStateProperties.CHEST_TYPE);
                    newState = newState.setValue(ChestBlock.CURSED_CHEST_TYPE, type == ChestType.LEFT ? CursedChestType.RIGHT : type == ChestType.RIGHT ? CursedChestType.LEFT : CursedChestType.SINGLE);
                }
                if (world.setBlockAndUpdate(pos, newState)) {
                    BlockEntity newEntity = world.getBlockEntity(pos);
                    //noinspection ConstantConditions
                    CompoundTag newTag = newEntity.save(new CompoundTag());
                    ContainerHelper.saveAllItems(newTag, inventory);
                    code.addToTag(newTag);
                    newEntity.load(newTag);
                }
            }
        }
    }

    private static boolean tryUpgradeBlockToOldChest(UseOnContext context, ResourceLocation from, ResourceLocation to) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack handStack = context.getItemInHand();
        if (OldChestBlock.getBlockType(state) == DoubleBlockCombiner.BlockType.SINGLE) {
            Common.upgradeSingleBlockToOldChest(world, state, pos, from, to);
            handStack.shrink(1);
            return true;
        } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
            BlockPos otherPos = pos.relative(OldChestBlock.getDirectionToAttached(state));
            BlockState otherState = world.getBlockState(otherPos);
            Common.upgradeSingleBlockToOldChest(world, state, pos, from, to);
            Common.upgradeSingleBlockToOldChest(world, otherState, otherPos, from, to);
            handStack.shrink(2);
            return true;
        }
        return false;
    }

    private static void upgradeSingleBlockToOldChest(Level world, BlockState state, BlockPos pos, ResourceLocation from, ResourceLocation to) {
        if (((OldChestBlock) state.getBlock()).getBlockTier() == from) {
            AbstractOpenableStorageBlock toBlock = (AbstractOpenableStorageBlock) Common.getTieredBlock(OLD_CHEST_BLOCK_TYPE, to);
            NonNullList<ItemStack> inventory = NonNullList.withSize(toBlock.getSlotCount(), ItemStack.EMPTY);
            //noinspection ConstantConditions
            CompoundTag tag = world.getBlockEntity(pos).save(new CompoundTag());
            LockCode code = LockCode.fromTag(tag);
            ContainerHelper.loadAllItems(tag, inventory);
            world.removeBlockEntity(pos);
            BlockState newState = toBlock.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING)).setValue(OldChestBlock.CURSED_CHEST_TYPE, state.getValue(OldChestBlock.CURSED_CHEST_TYPE));
            if (world.setBlockAndUpdate(pos, newState)) {
                BlockEntity newEntity = world.getBlockEntity(pos);
                //noinspection ConstantConditions
                CompoundTag newTag = newEntity.save(new CompoundTag());
                ContainerHelper.saveAllItems(newTag, inventory);
                code.addToTag(newTag);
                newEntity.load(newTag);
            }
        }
    }

    static void registerBaseContent(Consumer<Tuple<ResourceLocation, Item>[]> itemRegistration) {
        //noinspection unchecked
        Tuple<ResourceLocation, Item>[] items = new Tuple[16];
        items[0] = new Tuple<>(Utils.id("chest_mutator"), new StorageMutator(new Item.Properties().stacksTo(1).tab(Common.GROUP)));
        Common.defineTierUpgradePath(items, Utils.WOOD_TIER, Common.IRON_TIER, Common.GOLD_TIER, Common.DIAMOND_TIER, Common.OBSIDIAN_TIER, Common.NETHERITE_TIER);
        itemRegistration.accept(items);
    }

    public static ResourceLocation registerStat(ResourceLocation stat) {
        ResourceLocation rv = Registry.register(Registry.CUSTOM_STAT, stat, stat); // Forge doesn't provide registries for stats
        Stats.CUSTOM.get(rv);
        return rv;
    }

    private static void defineTierUpgradePath(Tuple<ResourceLocation, Item>[] items, Tier... tiers) {
        int numTiers = tiers.length;
        int index = 1;
        for (int fromIndex = 0; fromIndex < numTiers - 1; fromIndex++) {
            Tier fromTier = tiers[fromIndex];
            for (int toIndex = fromIndex + 1; toIndex < numTiers; toIndex++) {
                Tier toTier = tiers[toIndex];
                ResourceLocation itemId = Utils.id(fromTier.getId().getPath() + "_to_" + toTier.getId().getPath() + "_conversion_kit");
                Item.Properties properties = fromTier.getItemSettings()
                                                   .andThen(toTier.getItemSettings())
                                                   .apply(new Item.Properties().tab(Common.GROUP).stacksTo(16));
                Item kit = new StorageConversionKit(properties, fromTier.getId(), toTier.getId());
                items[index++] = new Tuple<>(itemId, kit);
            }
        }
    }

    public static BlockUpgradeBehaviour getBlockUpgradeBehaviour(Block block) {
        for (Map.Entry<Predicate<Block>, BlockUpgradeBehaviour> entry : Common.BLOCK_UPGRADE_BEHAVIOURS.entrySet()) {
            if (entry.getKey().test(block)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static void defineBlockUpgradeBehaviour(Predicate<Block> target, BlockUpgradeBehaviour behaviour) {
        Common.BLOCK_UPGRADE_BEHAVIOURS.put(target, behaviour);
    }

    record BlockTierId(ResourceLocation blockType, ResourceLocation blockTier) {

    }

    private static void registerTieredBlock(AbstractStorageBlock block) {
        Common.BLOCKS.putIfAbsent(new BlockTierId(block.getBlockType(), block.getBlockTier()), block);
    }

    public static AbstractStorageBlock getTieredBlock(ResourceLocation blockType, ResourceLocation tier) {
        return Common.BLOCKS.get(new BlockTierId(blockType, tier));
    }

    public static void declareChestTextures(ResourceLocation block, ResourceLocation singleTexture, ResourceLocation leftTexture, ResourceLocation rightTexture, ResourceLocation topTexture, ResourceLocation bottomTexture, ResourceLocation frontTexture, ResourceLocation backTexture) {
        if (!Common.CHEST_TEXTURES.containsKey(block)) {
            TextureCollection collection = new TextureCollection(singleTexture, leftTexture, rightTexture, topTexture, bottomTexture, frontTexture, backTexture);
            Common.CHEST_TEXTURES.put(block, collection);
        } else {
            throw new IllegalArgumentException("Tried registering chest textures for \"" + block + "\" which already has textures.");
        }
    }

    public static ResourceLocation getChestTexture(ResourceLocation block, CursedChestType chestType) {
        if (Common.CHEST_TEXTURES.containsKey(block)) {
            return Common.CHEST_TEXTURES.get(block).getTexture(chestType);
        } else {
            return MissingTextureAtlasSprite.getLocation();
        }
    }
}
