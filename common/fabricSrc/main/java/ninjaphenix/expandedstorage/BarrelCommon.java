package ninjaphenix.expandedstorage;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
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
import ninjaphenix.expandedstorage.block.BarrelBlock;
import ninjaphenix.expandedstorage.block.misc.BarrelBlockEntity;
import ninjaphenix.expandedstorage.internal_api.BaseApi;
import ninjaphenix.expandedstorage.internal_api.Utils;
import ninjaphenix.expandedstorage.internal_api.block.AbstractOpenableStorageBlock;
import ninjaphenix.expandedstorage.internal_api.tier.Tier;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class BarrelCommon {
    public static final Identifier BLOCK_TYPE = Utils.resloc("barrel");
    private static final int ICON_SUITABILITY = 998;
    private static BlockEntityType<BarrelBlockEntity> blockEntityType;

    private BarrelCommon() {

    }

    public static BlockEntityType<BarrelBlockEntity> getBlockEntityType() {
        return blockEntityType;
    }

    static void registerContent(Consumer<Set<BarrelBlock>> blockReg,
                                Consumer<Set<BlockItem>> itemReg,
                                Consumer<BlockEntityType<BarrelBlockEntity>> blockEntityTypeConsumer,
                                net.minecraft.tag.Tag<Block> woodenBarrelTag) {
        // Init and register opening stats
        Identifier ironOpenStat = BaseCommon.registerStat(Utils.resloc("open_iron_barrel"));
        Identifier goldOpenStat = BaseCommon.registerStat(Utils.resloc("open_gold_barrel"));
        Identifier diamondOpenStat = BaseCommon.registerStat(Utils.resloc("open_diamond_barrel"));
        Identifier obsidianOpenStat = BaseCommon.registerStat(Utils.resloc("open_obsidian_barrel"));
        Identifier netheriteOpenStat = BaseCommon.registerStat(Utils.resloc("open_netherite_barrel"));
        // Init block properties
        AbstractBlock.Settings ironProperties = AbstractBlock.Settings.of(Material.WOOD, MapColor.OAK_TAN)
                                                                      .strength(5.0F, 6.0F)
                                                                      .sounds(BlockSoundGroup.WOOD);
        AbstractBlock.Settings goldProperties = AbstractBlock.Settings.of(Material.WOOD, MapColor.OAK_TAN)
                                                                      .strength(3.0F, 6.0F)
                                                                      .sounds(BlockSoundGroup.WOOD);
        AbstractBlock.Settings diamondProperties = AbstractBlock.Settings.of(Material.WOOD, MapColor.OAK_TAN)
                                                                         .strength(5.0F, 6.0F)
                                                                         .sounds(BlockSoundGroup.WOOD);
        AbstractBlock.Settings obsidianProperties = AbstractBlock.Settings.of(Material.WOOD, MapColor.OAK_TAN)
                                                                          .strength(50.0F, 1200.0F)
                                                                          .sounds(BlockSoundGroup.WOOD);
        AbstractBlock.Settings netheriteProperties = AbstractBlock.Settings.of(Material.WOOD, MapColor.OAK_TAN)
                                                                           .strength(50.0F, 1200.0F)
                                                                           .sounds(BlockSoundGroup.WOOD);
        // Init blocks
        BarrelBlock ironBarrelBlock = BarrelCommon.barrelBlock(Utils.resloc("iron_barrel"), ironOpenStat, Utils.IRON_TIER, ironProperties);
        BarrelBlock goldBarrelBlock = BarrelCommon.barrelBlock(Utils.resloc("gold_barrel"), goldOpenStat, Utils.GOLD_TIER, goldProperties);
        BarrelBlock diamondBarrelBlock = BarrelCommon.barrelBlock(Utils.resloc("diamond_barrel"), diamondOpenStat, Utils.DIAMOND_TIER, diamondProperties);
        BarrelBlock obsidianBarrelBlock = BarrelCommon.barrelBlock(Utils.resloc("obsidian_barrel"), obsidianOpenStat, Utils.OBSIDIAN_TIER, obsidianProperties);
        BarrelBlock netheriteBarrelBlock = BarrelCommon.barrelBlock(Utils.resloc("netherite_barrel"), netheriteOpenStat, Utils.NETHERITE_TIER, netheriteProperties);
        Set<BarrelBlock> blocks = ImmutableSet.copyOf(new BarrelBlock[]{ironBarrelBlock, goldBarrelBlock, diamondBarrelBlock, obsidianBarrelBlock, netheriteBarrelBlock});
        blockReg.accept(blocks);
        // Init items
        BlockItem ironBarrelItem = BarrelCommon.barrelItem(Utils.IRON_TIER, ironBarrelBlock);
        BlockItem goldBarrelItem = BarrelCommon.barrelItem(Utils.GOLD_TIER, goldBarrelBlock);
        BlockItem diamondBarrelItem = BarrelCommon.barrelItem(Utils.DIAMOND_TIER, diamondBarrelBlock);
        BlockItem obsidianBarrelItem = BarrelCommon.barrelItem(Utils.OBSIDIAN_TIER, obsidianBarrelBlock);
        BlockItem netheriteBarrelItem = BarrelCommon.barrelItem(Utils.NETHERITE_TIER, netheriteBarrelBlock);
        Set<BlockItem> items = ImmutableSet.copyOf(new BlockItem[]{ironBarrelItem, goldBarrelItem, diamondBarrelItem, obsidianBarrelItem, netheriteBarrelItem});
        itemReg.accept(items);
        // Init block entity type
        BlockEntityType<BarrelBlockEntity> blockEntityType = PlatformUtils.getInstance().createBlockEntityType((pos, state) -> new BarrelBlockEntity(BarrelCommon.getBlockEntityType(), pos, state), Collections.unmodifiableSet(blocks), null);
        BarrelCommon.blockEntityType = blockEntityType;
        blockEntityTypeConsumer.accept(blockEntityType);
        // Register chest module icon & upgrade behaviours
        BaseApi.getInstance().offerTabIcon(netheriteBarrelItem, BarrelCommon.ICON_SUITABILITY);
        Predicate<Block> isUpgradableBarrelBlock = (block) -> block instanceof BarrelBlock || block instanceof net.minecraft.block.BarrelBlock || woodenBarrelTag.contains(block);
        BaseApi.getInstance().defineBlockUpgradeBehaviour(isUpgradableBarrelBlock, BarrelCommon::tryUpgradeBlock);
    }

    private static BarrelBlock barrelBlock(Identifier blockId, Identifier stat, Tier tier, AbstractBlock.Settings properties) {
        BarrelBlock block = new BarrelBlock(tier.getBlockProperties().apply(properties), blockId, tier.getId(), stat, tier.getSlotCount());
        BaseApi.getInstance().registerTieredBlock(block);
        return block;
    }

    private static BlockItem barrelItem(Tier tier, BarrelBlock block) {
        return new BlockItem(block, tier.getItemProperties().apply(new Item.Settings().group(Utils.TAB)));
    }

    private static boolean tryUpgradeBlock(ItemUsageContext context, Identifier from, Identifier to) {
        World level = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        boolean isExpandedStorageBarrel = block instanceof BarrelBlock;
        int containerSize = !isExpandedStorageBarrel ? Utils.WOOD_STACK_COUNT : ((BarrelBlock) BaseApi.getInstance().getTieredBlock(BarrelCommon.BLOCK_TYPE, ((BarrelBlock) block).getBlockTier())).getSlotCount();
        if (isExpandedStorageBarrel && ((BarrelBlock) block).getBlockTier() == from || !isExpandedStorageBarrel && from == Utils.WOOD_TIER.getId()) {
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
                AbstractOpenableStorageBlock toBlock = (AbstractOpenableStorageBlock) BaseApi.getInstance().getTieredBlock(BarrelCommon.BLOCK_TYPE, to);
                DefaultedList<ItemStack> inventory = DefaultedList.ofSize(toBlock.getSlotCount(), ItemStack.EMPTY);
                ContainerLock code = ContainerLock.fromNbt(tag);
                Inventories.readNbt(tag, inventory);
                level.removeBlockEntity(pos);
                BlockState newState = toBlock.getDefaultState().with(Properties.FACING, state.get(Properties.FACING));
                if (level.setBlockState(pos, newState)) {
                    BlockEntity newEntity = level.getBlockEntity(pos);
                    //noinspection ConstantConditions
                    NbtCompound newTag = newEntity.writeNbt(new NbtCompound());
                    Inventories.writeNbt(newTag, inventory);
                    code.writeNbt(newTag);
                    newEntity.readNbt(newTag);
                    context.getStack().decrement(1);
                    return true;
                }
            }
        }
        return false;
    }
}
