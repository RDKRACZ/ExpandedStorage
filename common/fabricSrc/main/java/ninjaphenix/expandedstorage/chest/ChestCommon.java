package ninjaphenix.expandedstorage.chest;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.base.BaseCommon;
import ninjaphenix.expandedstorage.base.internal_api.BaseApi;
import ninjaphenix.expandedstorage.base.internal_api.Utils;
import ninjaphenix.expandedstorage.base.internal_api.block.AbstractOpenableStorageBlock;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.AbstractOpenableStorageBlockEntity;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.CursedChestType;
import ninjaphenix.expandedstorage.base.internal_api.tier.Tier;
import ninjaphenix.expandedstorage.base.wrappers.PlatformUtils;
import ninjaphenix.expandedstorage.chest.block.ChestBlock;
import ninjaphenix.expandedstorage.chest.block.misc.ChestBlockEntity;
import ninjaphenix.expandedstorage.chest.internal_api.ChestApi;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class ChestCommon {
    public static final Identifier BLOCK_TYPE = Utils.resloc("cursed_chest");
    private static final int ICON_SUITABILITY = 1000;
    private static BlockEntityType<ChestBlockEntity> blockEntityType;

    private ChestCommon() {

    }

    static void registerContent(Consumer<Set<ChestBlock>> blockReg,
                                Consumer<Set<BlockItem>> itemReg,
                                Consumer<BlockEntityType<ChestBlockEntity>> blockEntityTypeConsumer,
                                net.minecraft.tag.Tag<Block> woodenChestTag,
                                BiFunction<Block, Item.Settings, BlockItem> blockItemMaker) {
        // Init and register opening stats
        Identifier woodOpenStat = BaseCommon.registerStat(Utils.resloc("open_wood_chest"));
        Identifier pumpkinOpenStat = BaseCommon.registerStat(Utils.resloc("open_pumpkin_chest"));
        Identifier christmasOpenStat = BaseCommon.registerStat(Utils.resloc("open_christmas_chest"));
        Identifier ironOpenStat = BaseCommon.registerStat(Utils.resloc("open_iron_chest"));
        Identifier goldOpenStat = BaseCommon.registerStat(Utils.resloc("open_gold_chest"));
        Identifier diamondOpenStat = BaseCommon.registerStat(Utils.resloc("open_diamond_chest"));
        Identifier obsidianOpenStat = BaseCommon.registerStat(Utils.resloc("open_obsidian_chest"));
        Identifier netheriteOpenStat = BaseCommon.registerStat(Utils.resloc("open_netherite_chest"));
        // Init block properties
        AbstractBlock.Settings woodProperties = AbstractBlock.Settings.of(Material.WOOD, MapColor.OAK_TAN)
                                                                      .strength(2.5F)
                                                                      .sounds(BlockSoundGroup.WOOD);
        AbstractBlock.Settings pumpkinProperties = AbstractBlock.Settings.of(Material.GOURD, MapColor.ORANGE)
                                                                         .strength(1.0F)
                                                                         .sounds(BlockSoundGroup.WOOD);
        AbstractBlock.Settings christmasProperties = AbstractBlock.Settings.of(Material.WOOD, MapColor.OAK_TAN)
                                                                           .strength(2.5F)
                                                                           .sounds(BlockSoundGroup.WOOD);
        AbstractBlock.Settings ironProperties = AbstractBlock.Settings.of(Material.METAL, MapColor.IRON_GRAY)
                                                                      .strength(5.0F, 6.0F)
                                                                      .sounds(BlockSoundGroup.METAL);
        AbstractBlock.Settings goldProperties = AbstractBlock.Settings.of(Material.METAL, MapColor.GOLD)
                                                                      .strength(3.0F, 6.0F)
                                                                      .sounds(BlockSoundGroup.METAL);
        AbstractBlock.Settings diamondProperties = AbstractBlock.Settings.of(Material.METAL, MapColor.DIAMOND_BLUE)
                                                                         .strength(5.0F, 6.0F)
                                                                         .sounds(BlockSoundGroup.METAL);
        AbstractBlock.Settings obsidianProperties = AbstractBlock.Settings.of(Material.STONE, MapColor.BLACK)
                                                                          .strength(50.0F, 1200.0F);
        AbstractBlock.Settings netheriteProperties = AbstractBlock.Settings.of(Material.METAL, MapColor.BLACK)
                                                                           .strength(50.0F, 1200.0F)
                                                                           .sounds(BlockSoundGroup.NETHERITE);
        // Init and register blocks
        ChestBlock woodChestBlock = ChestCommon.chestBlock(Utils.resloc("wood_chest"), woodOpenStat, Utils.WOOD_TIER, woodProperties);
        ChestBlock pumpkinChestBlock = ChestCommon.chestBlock(Utils.resloc("pumpkin_chest"), pumpkinOpenStat, Utils.WOOD_TIER, pumpkinProperties);
        ChestBlock christmasChestBlock = ChestCommon.chestBlock(Utils.resloc("christmas_chest"), christmasOpenStat, Utils.WOOD_TIER, christmasProperties);
        ChestBlock ironChestBlock = ChestCommon.chestBlock(Utils.resloc("iron_chest"), ironOpenStat, Utils.IRON_TIER, ironProperties);
        ChestBlock goldChestBlock = ChestCommon.chestBlock(Utils.resloc("gold_chest"), goldOpenStat, Utils.GOLD_TIER, goldProperties);
        ChestBlock diamondChestBlock = ChestCommon.chestBlock(Utils.resloc("diamond_chest"), diamondOpenStat, Utils.DIAMOND_TIER, diamondProperties);
        ChestBlock obsidianChestBlock = ChestCommon.chestBlock(Utils.resloc("obsidian_chest"), obsidianOpenStat, Utils.OBSIDIAN_TIER, obsidianProperties);
        ChestBlock netheriteChestBlock = ChestCommon.chestBlock(Utils.resloc("netherite_chest"), netheriteOpenStat, Utils.NETHERITE_TIER, netheriteProperties);
        Set<ChestBlock> blocks = ImmutableSet.copyOf(new ChestBlock[]{woodChestBlock, pumpkinChestBlock, christmasChestBlock, ironChestBlock, goldChestBlock, diamondChestBlock, obsidianChestBlock, netheriteChestBlock});
        blockReg.accept(blocks);
        // Init and register items
        BlockItem woodChestItem = ChestCommon.chestItem(Utils.WOOD_TIER, woodChestBlock, blockItemMaker);
        BlockItem pumpkinChestItem = ChestCommon.chestItem(Utils.WOOD_TIER, pumpkinChestBlock, blockItemMaker);
        BlockItem christmasChestItem = ChestCommon.chestItem(Utils.WOOD_TIER, christmasChestBlock, blockItemMaker);
        BlockItem ironChestItem = ChestCommon.chestItem(Utils.IRON_TIER, ironChestBlock, blockItemMaker);
        BlockItem goldChestItem = ChestCommon.chestItem(Utils.GOLD_TIER, goldChestBlock, blockItemMaker);
        BlockItem diamondChestItem = ChestCommon.chestItem(Utils.DIAMOND_TIER, diamondChestBlock, blockItemMaker);
        BlockItem obsidianChestItem = ChestCommon.chestItem(Utils.OBSIDIAN_TIER, obsidianChestBlock, blockItemMaker);
        BlockItem netheriteChestItem = ChestCommon.chestItem(Utils.NETHERITE_TIER, netheriteChestBlock, blockItemMaker);
        Set<BlockItem> items = ImmutableSet.copyOf(new BlockItem[]{woodChestItem, pumpkinChestItem, christmasChestItem, ironChestItem, goldChestItem, diamondChestItem, obsidianChestItem, netheriteChestItem});
        itemReg.accept(items);
        // Init and register block entity type
        BlockEntityType<ChestBlockEntity> blockEntityType = PlatformUtils.getInstance().createBlockEntityType((pos, state) -> new ChestBlockEntity(ChestCommon.getBlockEntityType(), pos, state), Collections.unmodifiableSet(blocks), null);
        ChestCommon.blockEntityType = blockEntityType;
        blockEntityTypeConsumer.accept(blockEntityType);
        // Register chest module icon & upgrade behaviours
        BaseApi.getInstance().offerTabIcon(netheriteChestItem, ChestCommon.ICON_SUITABILITY);
        Predicate<Block> isUpgradableChestBlock = (block) -> block instanceof ChestBlock || block instanceof net.minecraft.block.ChestBlock || woodenChestTag.contains(block);
        BaseApi.getInstance().defineBlockUpgradeBehaviour(isUpgradableChestBlock, ChestCommon::tryUpgradeBlock);
    }

    public static BlockEntityType<ChestBlockEntity> getBlockEntityType() {
        return blockEntityType;
    }

    private static ChestBlock chestBlock(Identifier blockId, Identifier stat, Tier tier, AbstractBlock.Settings properties) {
        ChestBlock block = new ChestBlock(tier.getBlockProperties().apply(properties.dynamicBounds()), blockId, tier.getId(), stat, tier.getSlotCount());
        BaseApi.getInstance().registerTieredBlock(block);
        return block;
    }

    private static BlockItem chestItem(Tier tier, ChestBlock block, BiFunction<Block, Item.Settings, BlockItem> blockItemMaker) {
        return blockItemMaker.apply(block, tier.getItemProperties().apply(new Item.Settings().group(Utils.TAB)));
    }

    static Set<Identifier> getChestTextures(Set<ChestBlock> blocks) {
        Set<Identifier> textures = new HashSet<>();
        for (ChestBlock block : blocks) {
            Identifier blockId = block.getBlockId();
            for (CursedChestType type : CursedChestType.values()) {
                textures.add(ChestApi.INSTANCE.getChestTexture(blockId, type));
            }
        }
        return textures;
    }

    static void registerChestTextures(Set<ChestBlock> blocks) {
        for (ChestBlock block : blocks) {
            Identifier blockId = block.getBlockId();
            ChestApi.INSTANCE.declareChestTextures(
                    blockId, Utils.resloc("entity/" + blockId.getPath() + "/single"),
                    Utils.resloc("entity/" + blockId.getPath() + "/left"),
                    Utils.resloc("entity/" + blockId.getPath() + "/right"),
                    Utils.resloc("entity/" + blockId.getPath() + "/top"),
                    Utils.resloc("entity/" + blockId.getPath() + "/bottom"),
                    Utils.resloc("entity/" + blockId.getPath() + "/front"),
                    Utils.resloc("entity/" + blockId.getPath() + "/back"));
        }
    }

    private static boolean tryUpgradeBlock(ItemUsageContext context, Identifier from, Identifier to) {
        World level = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = level.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        ItemStack handStack = context.getStack();
        if (state.getBlock() instanceof ChestBlock) {
            if (ChestBlock.getBlockType(state) == DoubleBlockProperties.Type.SINGLE) {
                ChestCommon.upgradeSingleBlock(level, state, pos, from, to);
                handStack.decrement(1);
                return true;
            } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
                BlockPos otherPos = pos.offset(ChestBlock.getDirectionToAttached(state));
                BlockState otherState = level.getBlockState(otherPos);
                ChestCommon.upgradeSingleBlock(level, state, pos, from, to);
                ChestCommon.upgradeSingleBlock(level, otherState, otherPos, from, to);
                handStack.decrement(2);
                return true;
            }
        } else {
            if (net.minecraft.block.ChestBlock.getDoubleBlockType(state) == DoubleBlockProperties.Type.SINGLE) {
                ChestCommon.upgradeSingleBlock(level, state, pos, from, to);
                handStack.decrement(1);
                return true;
            } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
                BlockPos otherPos = pos.offset(net.minecraft.block.ChestBlock.getFacing(state));
                BlockState otherState = level.getBlockState(otherPos);
                ChestCommon.upgradeSingleBlock(level, state, pos, from, to);
                ChestCommon.upgradeSingleBlock(level, otherState, otherPos, from, to);
                handStack.decrement(2);
                return true;
            }
        }

        return false;
    }

    private static void upgradeSingleBlock(World level, BlockState state, BlockPos pos, Identifier from, Identifier to) {
        Block block = state.getBlock();
        boolean isExpandedStorageChest = block instanceof ChestBlock;
        int containerSize = !isExpandedStorageChest ? Utils.WOOD_STACK_COUNT : ((ChestBlock) BaseApi.getInstance().getTieredBlock(ChestCommon.BLOCK_TYPE, ((ChestBlock) block).getBlockTier())).getSlotCount();
        if (isExpandedStorageChest && ((ChestBlock) block).getBlockTier() == from || !isExpandedStorageChest && from == Utils.WOOD_TIER.getId()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            //noinspection ConstantConditions
            NbtCompound tag = blockEntity.writeNbt(new NbtCompound());
            boolean verifiedSize = blockEntity instanceof Inventory container && container.size() == containerSize;
            if (!verifiedSize) { // Cannot verify container size, we'll let it upgrade if it has or has less than 27 items
                if (tag.contains("Items", NbtElement.LIST_TYPE)) {
                    NbtList items = tag.getList("Items", NbtElement.COMPOUND_TYPE);
                    if (items.size() <= containerSize) {
                        verifiedSize = true;
                    }
                }
            }
            if (verifiedSize) {
                AbstractOpenableStorageBlock toBlock = (AbstractOpenableStorageBlock) BaseApi.getInstance().getTieredBlock(ChestCommon.BLOCK_TYPE, to);
                DefaultedList<ItemStack> inventory = DefaultedList.ofSize(toBlock.getSlotCount(), ItemStack.EMPTY);
                ContainerLock code = ContainerLock.fromNbt(tag);
                Inventories.readNbt(tag, inventory);
                level.removeBlockEntity(pos);
                // Needs fixing up to check for vanilla states.
                BlockState newState = toBlock.getDefaultState()
                                             .with(Properties.HORIZONTAL_FACING, state.get(Properties.HORIZONTAL_FACING))
                                             .with(Properties.WATERLOGGED, state.get(Properties.WATERLOGGED));
                if (state.contains(ChestBlock.CURSED_CHEST_TYPE)) {
                    newState = newState.with(ChestBlock.CURSED_CHEST_TYPE, state.get(ChestBlock.CURSED_CHEST_TYPE));
                } else if (state.contains(Properties.CHEST_TYPE)) {
                    ChestType type = state.get(Properties.CHEST_TYPE);
                    newState = newState.with(ChestBlock.CURSED_CHEST_TYPE, type == ChestType.LEFT ? CursedChestType.RIGHT : type == ChestType.RIGHT ? CursedChestType.LEFT : CursedChestType.SINGLE);
                }
                if (level.setBlockState(pos, newState)) {
                    BlockEntity newEntity = level.getBlockEntity(pos);
                    //noinspection ConstantConditions
                    NbtCompound newTag = newEntity.writeNbt(new NbtCompound());
                    Inventories.writeNbt(newTag, inventory);
                    code.writeNbt(newTag);
                    newEntity.readNbt(newTag);
                }
            }
        }
    }
}
