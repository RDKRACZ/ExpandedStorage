package ninjaphenix.expandedstorage.old_chest;

import com.google.common.collect.ImmutableSet;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public final class OldChestCommon {
    public static final ResourceLocation BLOCK_TYPE = Utils.resloc("old_cursed_chest");
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
        ResourceLocation woodOpenStat = BaseCommon.registerStat(Utils.resloc("open_old_wood_chest"));
        ResourceLocation ironOpenStat = BaseCommon.registerStat(Utils.resloc("open_old_iron_chest"));
        ResourceLocation goldOpenStat = BaseCommon.registerStat(Utils.resloc("open_old_gold_chest"));
        ResourceLocation diamondOpenStat = BaseCommon.registerStat(Utils.resloc("open_old_diamond_chest"));
        ResourceLocation obsidianOpenStat = BaseCommon.registerStat(Utils.resloc("open_old_obsidian_chest"));
        ResourceLocation netheriteOpenStat = BaseCommon.registerStat(Utils.resloc("open_old_netherite_chest"));
        // Init block properties
        BlockBehaviour.Properties woodProperties = BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD)
                                                                      .strength(2.5F)
                                                                      .sound(SoundType.WOOD);
        BlockBehaviour.Properties ironProperties = BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                                                                      .strength(5.0F, 6.0F)
                                                                      .sound(SoundType.METAL);
        BlockBehaviour.Properties goldProperties = BlockBehaviour.Properties.of(Material.METAL, MaterialColor.GOLD)
                                                                      .strength(3.0F, 6.0F)
                                                                      .sound(SoundType.METAL);
        BlockBehaviour.Properties diamondProperties = BlockBehaviour.Properties.of(Material.METAL, MaterialColor.DIAMOND)
                                                                         .strength(5.0F, 6.0F)
                                                                         .sound(SoundType.METAL);
        BlockBehaviour.Properties obsidianProperties = BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK)
                                                                          .strength(50.0F, 1200.0F);
        BlockBehaviour.Properties netheriteProperties = BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_BLACK)
                                                                           .strength(50.0F, 1200.0F)
                                                                           .sound(SoundType.NETHERITE_BLOCK);
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
        return new BlockItem(block, tier.getItemProperties().apply(new Item.Properties().tab(Utils.TAB)));
    }

    private static OldChestBlock oldChestBlock(ResourceLocation blockId, ResourceLocation stat, Tier tier, BlockBehaviour.Properties properties) {
        OldChestBlock block = new OldChestBlock(tier.getBlockProperties().apply(properties), blockId, tier.getId(), stat, tier.getSlotCount());
        BaseApi.getInstance().registerTieredBlock(block);
        return block;
    }

    private static boolean tryUpgradeBlock(UseOnContext context, ResourceLocation from, ResourceLocation to) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack handStack = context.getItemInHand();
        if (OldChestBlock.getBlockType(state) == DoubleBlockCombiner.BlockType.SINGLE) {
            OldChestCommon.upgradeSingleBlock(level, state, pos, from, to);
            handStack.shrink(1);
            return true;
        } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
            BlockPos otherPos = pos.relative(OldChestBlock.getDirectionToAttached(state));
            BlockState otherState = level.getBlockState(otherPos);
            OldChestCommon.upgradeSingleBlock(level, state, pos, from, to);
            OldChestCommon.upgradeSingleBlock(level, otherState, otherPos, from, to);
            handStack.shrink(2);
            return true;
        }
        return false;
    }

    private static void upgradeSingleBlock(Level level, BlockState state, BlockPos pos, ResourceLocation from, ResourceLocation to) {
        if (((OldChestBlock) state.getBlock()).getBlockTier() == from) {
            AbstractOpenableStorageBlock toBlock = (AbstractOpenableStorageBlock) BaseApi.getInstance().getTieredBlock(OldChestCommon.BLOCK_TYPE, to);
            NonNullList<ItemStack> inventory = NonNullList.withSize(toBlock.getSlotCount(), ItemStack.EMPTY);
            //noinspection ConstantConditions
            CompoundTag tag = level.getBlockEntity(pos).save(new CompoundTag());
            LockCode code = LockCode.fromTag(tag);
            ContainerHelper.loadAllItems(tag, inventory);
            level.removeBlockEntity(pos);
            BlockState newState = toBlock.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING)).setValue(OldChestBlock.CURSED_CHEST_TYPE, state.getValue(OldChestBlock.CURSED_CHEST_TYPE));
            if (level.setBlockAndUpdate(pos, newState)) {
                BlockEntity newEntity = level.getBlockEntity(pos);
                //noinspection ConstantConditions
                CompoundTag newTag = newEntity.save(new CompoundTag());
                ContainerHelper.saveAllItems(newTag, inventory);
                code.addToTag(newTag);
                newEntity.load(newTag);
            }
        }
    }
}
