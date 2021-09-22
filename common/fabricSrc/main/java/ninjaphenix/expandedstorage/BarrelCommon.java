package ninjaphenix.expandedstorage;

import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
import net.minecraft.tag.Tag;
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

import java.util.function.Predicate;

public final class BarrelCommon {
    public static final Identifier BLOCK_TYPE = Utils.id("barrel");
    private static final int ICON_SUITABILITY = 998;
    private static BlockEntityType<BarrelBlockEntity> blockEntityType;

    private BarrelCommon() {

    }

    public static BlockEntityType<BarrelBlockEntity> getBlockEntityType() {
        return blockEntityType;
    }

    static void registerContent(RegistrationConsumer<BarrelBlock, BlockItem, BarrelBlockEntity> registration, Tag<Block> woodenBarrelTag) {
        // Init and register opening stats
        Identifier ironOpenStat = BaseCommon.registerStat(Utils.id("open_iron_barrel"));
        Identifier goldOpenStat = BaseCommon.registerStat(Utils.id("open_gold_barrel"));
        Identifier diamondOpenStat = BaseCommon.registerStat(Utils.id("open_diamond_barrel"));
        Identifier obsidianOpenStat = BaseCommon.registerStat(Utils.id("open_obsidian_barrel"));
        Identifier netheriteOpenStat = BaseCommon.registerStat(Utils.id("open_netherite_barrel"));
        // Init block properties
        Settings ironSettings = Settings.of(Material.WOOD).strength(5, 6).sounds(BlockSoundGroup.WOOD);
        Settings goldSettings = Settings.of(Material.WOOD).strength(3, 6).sounds(BlockSoundGroup.WOOD);
        Settings diamondSettings = Settings.of(Material.WOOD).strength(5, 6).sounds(BlockSoundGroup.WOOD);
        Settings obsidianSettings = Settings.of(Material.WOOD).strength(50, 1200).sounds(BlockSoundGroup.WOOD);
        Settings netheriteSettings = Settings.of(Material.WOOD).strength(50, 1200).sounds(BlockSoundGroup.WOOD);
        // Init blocks
        BarrelBlock ironBarrelBlock = BarrelCommon.barrelBlock(Utils.id("iron_barrel"), ironOpenStat, Utils.IRON_TIER, ironSettings);
        BarrelBlock goldBarrelBlock = BarrelCommon.barrelBlock(Utils.id("gold_barrel"), goldOpenStat, Utils.GOLD_TIER, goldSettings);
        BarrelBlock diamondBarrelBlock = BarrelCommon.barrelBlock(Utils.id("diamond_barrel"), diamondOpenStat, Utils.DIAMOND_TIER, diamondSettings);
        BarrelBlock obsidianBarrelBlock = BarrelCommon.barrelBlock(Utils.id("obsidian_barrel"), obsidianOpenStat, Utils.OBSIDIAN_TIER, obsidianSettings);
        BarrelBlock netheriteBarrelBlock = BarrelCommon.barrelBlock(Utils.id("netherite_barrel"), netheriteOpenStat, Utils.NETHERITE_TIER, netheriteSettings);
        BarrelBlock[] blocks = new BarrelBlock[]{ironBarrelBlock, goldBarrelBlock, diamondBarrelBlock, obsidianBarrelBlock, netheriteBarrelBlock};
        // Init items
        BlockItem ironBarrelItem = BarrelCommon.barrelItem(Utils.IRON_TIER, ironBarrelBlock);
        BlockItem goldBarrelItem = BarrelCommon.barrelItem(Utils.GOLD_TIER, goldBarrelBlock);
        BlockItem diamondBarrelItem = BarrelCommon.barrelItem(Utils.DIAMOND_TIER, diamondBarrelBlock);
        BlockItem obsidianBarrelItem = BarrelCommon.barrelItem(Utils.OBSIDIAN_TIER, obsidianBarrelBlock);
        BlockItem netheriteBarrelItem = BarrelCommon.barrelItem(Utils.NETHERITE_TIER, netheriteBarrelBlock);
        BlockItem[] items = new BlockItem[]{ironBarrelItem, goldBarrelItem, diamondBarrelItem, obsidianBarrelItem, netheriteBarrelItem};
        // Init block entity type
        BarrelCommon.blockEntityType = BlockEntityType.Builder.create((pos, state) -> new BarrelBlockEntity(BarrelCommon.getBlockEntityType(), pos, state), blocks).build(null);
        registration.accept(blocks, items, BarrelCommon.blockEntityType);
        // Register chest module icon & upgrade behaviours
        BaseApi.getInstance().offerTabIcon(netheriteBarrelItem, BarrelCommon.ICON_SUITABILITY);
        Predicate<Block> isUpgradableBarrelBlock = (block) -> block instanceof BarrelBlock || block instanceof net.minecraft.block.BarrelBlock || woodenBarrelTag.contains(block);
        BaseApi.getInstance().defineBlockUpgradeBehaviour(isUpgradableBarrelBlock, BarrelCommon::tryUpgradeBlock);
    }

    private static BarrelBlock barrelBlock(Identifier blockId, Identifier stat, Tier tier, Settings settings) {
        BarrelBlock block = new BarrelBlock(tier.getBlockSettings().apply(settings), blockId, tier.getId(), stat, tier.getSlotCount());
        BaseApi.getInstance().registerTieredBlock(block);
        return block;
    }

    private static BlockItem barrelItem(Tier tier, BarrelBlock block) {
        return new BlockItem(block, tier.getItemSettings().apply(new Item.Settings().group(Utils.TAB)));
    }

    private static boolean tryUpgradeBlock(ItemUsageContext context, Identifier from, Identifier to) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        boolean isExpandedStorageBarrel = block instanceof BarrelBlock;
        int containerSize = !isExpandedStorageBarrel ? Utils.WOOD_STACK_COUNT : ((BarrelBlock) BaseApi.getInstance().getTieredBlock(BarrelCommon.BLOCK_TYPE, ((BarrelBlock) block).getBlockTier())).getSlotCount();
        if (isExpandedStorageBarrel && ((BarrelBlock) block).getBlockTier() == from || !isExpandedStorageBarrel && from == Utils.WOOD_TIER.getId()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
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
                world.removeBlockEntity(pos);
                BlockState newState = toBlock.getDefaultState().with(Properties.FACING, state.get(Properties.FACING));
                if (world.setBlockState(pos, newState)) {
                    BlockEntity newEntity = world.getBlockEntity(pos);
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
