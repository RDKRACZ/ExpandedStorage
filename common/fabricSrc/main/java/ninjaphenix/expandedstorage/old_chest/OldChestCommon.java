package ninjaphenix.expandedstorage.old_chest;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
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
import ninjaphenix.expandedstorage.base.internal_api.tier.Tier;
import ninjaphenix.expandedstorage.base.wrappers.PlatformUtils;
import ninjaphenix.expandedstorage.old_chest.block.OldChestBlock;
import ninjaphenix.expandedstorage.old_chest.block.misc.OldChestBlockEntity;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class OldChestCommon {
    public static final Identifier BLOCK_TYPE = Utils.resloc("old_cursed_chest");
    private static final int ICON_SUITABILITY = 999;
    private static BlockEntityType<OldChestBlockEntity> blockEntityType;

    private OldChestCommon() {

    }

    public static BlockEntityType<OldChestBlockEntity> getBlockEntityType() {
        return blockEntityType;
    }

    static void registerContent(Consumer<Set<OldChestBlock>> blockReg,
                                Consumer<Set<BlockItem>> itemReg,
                                Consumer<BlockEntityType<OldChestBlockEntity>> blockEntityTypeConsumer) {
        // Init and register opening stats
        Identifier woodOpenStat = BaseCommon.registerStat(Utils.resloc("open_old_wood_chest"));
        Identifier ironOpenStat = BaseCommon.registerStat(Utils.resloc("open_old_iron_chest"));
        Identifier goldOpenStat = BaseCommon.registerStat(Utils.resloc("open_old_gold_chest"));
        Identifier diamondOpenStat = BaseCommon.registerStat(Utils.resloc("open_old_diamond_chest"));
        Identifier obsidianOpenStat = BaseCommon.registerStat(Utils.resloc("open_old_obsidian_chest"));
        Identifier netheriteOpenStat = BaseCommon.registerStat(Utils.resloc("open_old_netherite_chest"));
        // Init block properties
        AbstractBlock.Settings woodProperties = AbstractBlock.Settings.of(Material.WOOD, MapColor.OAK_TAN)
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
        // Init blocks
        OldChestBlock woodChestBlock = OldChestCommon.oldChestBlock(Utils.resloc("old_wood_chest"), woodOpenStat, Utils.WOOD_TIER, woodProperties);
        OldChestBlock ironChestBlock = OldChestCommon.oldChestBlock(Utils.resloc("old_iron_chest"), ironOpenStat, Utils.IRON_TIER, ironProperties);
        OldChestBlock goldChestBlock = OldChestCommon.oldChestBlock(Utils.resloc("old_gold_chest"), goldOpenStat, Utils.GOLD_TIER, goldProperties);
        OldChestBlock diamondChestBlock = OldChestCommon.oldChestBlock(Utils.resloc("old_diamond_chest"), diamondOpenStat, Utils.DIAMOND_TIER, diamondProperties);
        OldChestBlock obsidianChestBlock = OldChestCommon.oldChestBlock(Utils.resloc("old_obsidian_chest"), obsidianOpenStat, Utils.OBSIDIAN_TIER, obsidianProperties);
        OldChestBlock netheriteChestBlock = OldChestCommon.oldChestBlock(Utils.resloc("old_netherite_chest"), netheriteOpenStat, Utils.NETHERITE_TIER, netheriteProperties);
        Set<OldChestBlock> blocks = ImmutableSet.copyOf(new OldChestBlock[]{woodChestBlock, ironChestBlock, goldChestBlock, diamondChestBlock, obsidianChestBlock, netheriteChestBlock});
        blockReg.accept(blocks);
        // Init items
        BlockItem woodChestItem = OldChestCommon.oldChestItem(Utils.WOOD_TIER, woodChestBlock);
        BlockItem ironChestItem = OldChestCommon.oldChestItem(Utils.IRON_TIER, ironChestBlock);
        BlockItem goldChestItem = OldChestCommon.oldChestItem(Utils.GOLD_TIER, goldChestBlock);
        BlockItem diamondChestItem = OldChestCommon.oldChestItem(Utils.DIAMOND_TIER, diamondChestBlock);
        BlockItem obsidianChestItem = OldChestCommon.oldChestItem(Utils.OBSIDIAN_TIER, obsidianChestBlock);
        BlockItem netheriteChestItem = OldChestCommon.oldChestItem(Utils.NETHERITE_TIER, netheriteChestBlock);
        Set<BlockItem> items = ImmutableSet.copyOf(new BlockItem[]{woodChestItem, ironChestItem, goldChestItem, diamondChestItem, obsidianChestItem, netheriteChestItem});
        itemReg.accept(items);
        // Init block entity type
        BlockEntityType<OldChestBlockEntity> blockEntityType = PlatformUtils.getInstance().createBlockEntityType((pos, state) -> new OldChestBlockEntity(OldChestCommon.getBlockEntityType(), pos, state), Collections.unmodifiableSet(blocks), null);
        OldChestCommon.blockEntityType = blockEntityType;
        blockEntityTypeConsumer.accept(blockEntityType);
        // Register chest module icon & upgrade behaviours
        BaseApi.getInstance().offerTabIcon(netheriteChestItem, OldChestCommon.ICON_SUITABILITY);
        Predicate<Block> isUpgradableChestBlock = (block) -> block instanceof OldChestBlock;
        BaseApi.getInstance().defineBlockUpgradeBehaviour(isUpgradableChestBlock, OldChestCommon::tryUpgradeBlock);
    }

    private static BlockItem oldChestItem(Tier tier, OldChestBlock block) {
        return new BlockItem(block, tier.getItemProperties().apply(new Item.Settings().group(Utils.TAB)));
    }

    private static OldChestBlock oldChestBlock(Identifier blockId, Identifier stat, Tier tier, AbstractBlock.Settings properties) {
        OldChestBlock block = new OldChestBlock(tier.getBlockProperties().apply(properties), blockId, tier.getId(), stat, tier.getSlotCount());
        BaseApi.getInstance().registerTieredBlock(block);
        return block;
    }

    private static boolean tryUpgradeBlock(ItemUsageContext context, Identifier from, Identifier to) {
        World level = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = level.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        ItemStack handStack = context.getStack();
        if (OldChestBlock.getBlockType(state) == DoubleBlockProperties.Type.SINGLE) {
            OldChestCommon.upgradeSingleBlock(level, state, pos, from, to);
            handStack.decrement(1);
            return true;
        } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
            BlockPos otherPos = pos.offset(OldChestBlock.getDirectionToAttached(state));
            BlockState otherState = level.getBlockState(otherPos);
            OldChestCommon.upgradeSingleBlock(level, state, pos, from, to);
            OldChestCommon.upgradeSingleBlock(level, otherState, otherPos, from, to);
            handStack.decrement(2);
            return true;
        }
        return false;
    }

    private static void upgradeSingleBlock(World level, BlockState state, BlockPos pos, Identifier from, Identifier to) {
        if (((OldChestBlock) state.getBlock()).getBlockTier() == from) {
            AbstractOpenableStorageBlock toBlock = (AbstractOpenableStorageBlock) BaseApi.getInstance().getTieredBlock(OldChestCommon.BLOCK_TYPE, to);
            DefaultedList<ItemStack> inventory = DefaultedList.ofSize(toBlock.getSlotCount(), ItemStack.EMPTY);
            //noinspection ConstantConditions
            NbtCompound tag = level.getBlockEntity(pos).writeNbt(new NbtCompound());
            ContainerLock code = ContainerLock.fromNbt(tag);
            Inventories.readNbt(tag, inventory);
            level.removeBlockEntity(pos);
            BlockState newState = toBlock.getDefaultState().with(Properties.HORIZONTAL_FACING, state.get(Properties.HORIZONTAL_FACING)).with(OldChestBlock.CURSED_CHEST_TYPE, state.get(OldChestBlock.CURSED_CHEST_TYPE));
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
