package ninjaphenix.expandedstorage;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.AbstractBlock.Settings;
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
import ninjaphenix.expandedstorage.block.OldChestBlock;
import ninjaphenix.expandedstorage.block.misc.OldChestBlockEntity;
import ninjaphenix.expandedstorage.internal_api.BaseApi;
import ninjaphenix.expandedstorage.internal_api.Utils;
import ninjaphenix.expandedstorage.internal_api.block.AbstractOpenableStorageBlock;
import ninjaphenix.expandedstorage.internal_api.tier.Tier;

import java.util.Set;
import java.util.function.Predicate;

public final class OldChestCommon {
    public static final Identifier BLOCK_TYPE = Utils.id("old_cursed_chest");
    private static final int ICON_SUITABILITY = 999;
    private static BlockEntityType<OldChestBlockEntity> blockEntityType;

    private OldChestCommon() {

    }

    public static BlockEntityType<OldChestBlockEntity> getBlockEntityType() {
        return blockEntityType;
    }

    static void registerContent(RegistrationConsumer<OldChestBlock, BlockItem, OldChestBlockEntity> registrationConsumer) {
        // Init and register opening stats
        Identifier woodOpenStat = BaseCommon.registerStat(Utils.id("open_old_wood_chest"));
        Identifier ironOpenStat = BaseCommon.registerStat(Utils.id("open_old_iron_chest"));
        Identifier goldOpenStat = BaseCommon.registerStat(Utils.id("open_old_gold_chest"));
        Identifier diamondOpenStat = BaseCommon.registerStat(Utils.id("open_old_diamond_chest"));
        Identifier obsidianOpenStat = BaseCommon.registerStat(Utils.id("open_old_obsidian_chest"));
        Identifier netheriteOpenStat = BaseCommon.registerStat(Utils.id("open_old_netherite_chest"));
        // Init block properties
        Settings woodSettings = Settings.of(Material.WOOD, MapColor.OAK_TAN).strength(2.5f).sounds(BlockSoundGroup.WOOD);
        Settings ironSettings = Settings.of(Material.METAL, MapColor.IRON_GRAY).strength(5, 6).sounds(BlockSoundGroup.METAL);
        Settings goldSettings = Settings.of(Material.METAL, MapColor.GOLD).strength(3, 6).sounds(BlockSoundGroup.METAL);
        Settings diamondSettings = Settings.of(Material.METAL, MapColor.DIAMOND_BLUE).strength(5, 6).sounds(BlockSoundGroup.METAL);
        Settings obsidianSettings = Settings.of(Material.STONE, MapColor.BLACK).strength(50, 1200);
        Settings netheriteSettings = Settings.of(Material.METAL, MapColor.BLACK).strength(50, 1200).sounds(BlockSoundGroup.NETHERITE);
        // Init blocks
        OldChestBlock woodChestBlock = OldChestCommon.oldChestBlock(Utils.id("old_wood_chest"), woodOpenStat, Utils.WOOD_TIER, woodSettings);
        OldChestBlock ironChestBlock = OldChestCommon.oldChestBlock(Utils.id("old_iron_chest"), ironOpenStat, Utils.IRON_TIER, ironSettings);
        OldChestBlock goldChestBlock = OldChestCommon.oldChestBlock(Utils.id("old_gold_chest"), goldOpenStat, Utils.GOLD_TIER, goldSettings);
        OldChestBlock diamondChestBlock = OldChestCommon.oldChestBlock(Utils.id("old_diamond_chest"), diamondOpenStat, Utils.DIAMOND_TIER, diamondSettings);
        OldChestBlock obsidianChestBlock = OldChestCommon.oldChestBlock(Utils.id("old_obsidian_chest"), obsidianOpenStat, Utils.OBSIDIAN_TIER, obsidianSettings);
        OldChestBlock netheriteChestBlock = OldChestCommon.oldChestBlock(Utils.id("old_netherite_chest"), netheriteOpenStat, Utils.NETHERITE_TIER, netheriteSettings);
        Set<OldChestBlock> blocks = ImmutableSet.copyOf(new OldChestBlock[]{woodChestBlock, ironChestBlock, goldChestBlock, diamondChestBlock, obsidianChestBlock, netheriteChestBlock});
        // Init items
        BlockItem woodChestItem = OldChestCommon.oldChestItem(Utils.WOOD_TIER, woodChestBlock);
        BlockItem ironChestItem = OldChestCommon.oldChestItem(Utils.IRON_TIER, ironChestBlock);
        BlockItem goldChestItem = OldChestCommon.oldChestItem(Utils.GOLD_TIER, goldChestBlock);
        BlockItem diamondChestItem = OldChestCommon.oldChestItem(Utils.DIAMOND_TIER, diamondChestBlock);
        BlockItem obsidianChestItem = OldChestCommon.oldChestItem(Utils.OBSIDIAN_TIER, obsidianChestBlock);
        BlockItem netheriteChestItem = OldChestCommon.oldChestItem(Utils.NETHERITE_TIER, netheriteChestBlock);
        Set<BlockItem> items = ImmutableSet.copyOf(new BlockItem[]{woodChestItem, ironChestItem, goldChestItem, diamondChestItem, obsidianChestItem, netheriteChestItem});
        // Init block entity type
        OldChestCommon.blockEntityType = BlockEntityType.Builder.create((pos, state) -> new OldChestBlockEntity(OldChestCommon.getBlockEntityType(), pos, state), blocks.toArray(OldChestBlock[]::new)).build(null);
        registrationConsumer.accept(blocks, items, OldChestCommon.blockEntityType);
        // Register chest module icon & upgrade behaviours
        BaseApi.getInstance().offerTabIcon(netheriteChestItem, OldChestCommon.ICON_SUITABILITY);
        Predicate<Block> isUpgradableChestBlock = (block) -> block instanceof OldChestBlock;
        BaseApi.getInstance().defineBlockUpgradeBehaviour(isUpgradableChestBlock, OldChestCommon::tryUpgradeBlock);
    }

    private static BlockItem oldChestItem(Tier tier, OldChestBlock block) {
        return new BlockItem(block, tier.getItemSettings().apply(new Item.Settings().group(Utils.TAB)));
    }

    private static OldChestBlock oldChestBlock(Identifier blockId, Identifier stat, Tier tier, Settings settings) {
        OldChestBlock block = new OldChestBlock(tier.getBlockSettings().apply(settings), blockId, tier.getId(), stat, tier.getSlotCount());
        BaseApi.getInstance().registerTieredBlock(block);
        return block;
    }

    private static boolean tryUpgradeBlock(ItemUsageContext context, Identifier from, Identifier to) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        ItemStack handStack = context.getStack();
        if (OldChestBlock.getBlockType(state) == DoubleBlockProperties.Type.SINGLE) {
            OldChestCommon.upgradeSingleBlock(world, state, pos, from, to);
            handStack.decrement(1);
            return true;
        } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
            BlockPos otherPos = pos.offset(OldChestBlock.getDirectionToAttached(state));
            BlockState otherState = world.getBlockState(otherPos);
            OldChestCommon.upgradeSingleBlock(world, state, pos, from, to);
            OldChestCommon.upgradeSingleBlock(world, otherState, otherPos, from, to);
            handStack.decrement(2);
            return true;
        }
        return false;
    }

    private static void upgradeSingleBlock(World world, BlockState state, BlockPos pos, Identifier from, Identifier to) {
        if (((OldChestBlock) state.getBlock()).getBlockTier() == from) {
            AbstractOpenableStorageBlock toBlock = (AbstractOpenableStorageBlock) BaseApi.getInstance().getTieredBlock(OldChestCommon.BLOCK_TYPE, to);
            DefaultedList<ItemStack> inventory = DefaultedList.ofSize(toBlock.getSlotCount(), ItemStack.EMPTY);
            //noinspection ConstantConditions
            NbtCompound tag = world.getBlockEntity(pos).writeNbt(new NbtCompound());
            ContainerLock code = ContainerLock.fromNbt(tag);
            Inventories.readNbt(tag, inventory);
            world.removeBlockEntity(pos);
            BlockState newState = toBlock.getDefaultState().with(Properties.HORIZONTAL_FACING, state.get(Properties.HORIZONTAL_FACING)).with(OldChestBlock.CURSED_CHEST_TYPE, state.get(OldChestBlock.CURSED_CHEST_TYPE));
            if (world.setBlockState(pos, newState)) {
                BlockEntity newEntity = world.getBlockEntity(pos);
                //noinspection ConstantConditions
                NbtCompound newTag = newEntity.writeNbt(new NbtCompound());
                Inventories.writeNbt(newTag, inventory);
                code.writeNbt(newTag);
                newEntity.readNbt(newTag);
            }
        }
    }
}
