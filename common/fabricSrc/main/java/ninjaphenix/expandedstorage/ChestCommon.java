package ninjaphenix.expandedstorage;

import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.*;
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
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.block.ChestBlock;
import ninjaphenix.expandedstorage.block.misc.ChestBlockEntity;
import ninjaphenix.expandedstorage.client.ChestApi;
import ninjaphenix.expandedstorage.internal_api.BaseApi;
import ninjaphenix.expandedstorage.internal_api.Utils;
import ninjaphenix.expandedstorage.internal_api.block.AbstractOpenableStorageBlock;
import ninjaphenix.expandedstorage.internal_api.block.misc.CursedChestType;
import ninjaphenix.expandedstorage.internal_api.tier.Tier;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public final class ChestCommon {
    public static final Identifier BLOCK_TYPE = Utils.id("cursed_chest");
    private static final int ICON_SUITABILITY = 1000;
    private static BlockEntityType<ChestBlockEntity> blockEntityType;

    private ChestCommon() {

    }

    static void registerContent(RegistrationConsumer<ChestBlock, BlockItem, ChestBlockEntity> registrationConsumer, Tag<Block> woodenChestTag, BiFunction<Block, Item.Settings, BlockItem> blockItemMaker) {
        // Init and register opening stats
        Identifier woodOpenStat = BaseCommon.registerStat(Utils.id("open_wood_chest"));
        Identifier pumpkinOpenStat = BaseCommon.registerStat(Utils.id("open_pumpkin_chest"));
        Identifier christmasOpenStat = BaseCommon.registerStat(Utils.id("open_christmas_chest"));
        Identifier ironOpenStat = BaseCommon.registerStat(Utils.id("open_iron_chest"));
        Identifier goldOpenStat = BaseCommon.registerStat(Utils.id("open_gold_chest"));
        Identifier diamondOpenStat = BaseCommon.registerStat(Utils.id("open_diamond_chest"));
        Identifier obsidianOpenStat = BaseCommon.registerStat(Utils.id("open_obsidian_chest"));
        Identifier netheriteOpenStat = BaseCommon.registerStat(Utils.id("open_netherite_chest"));
        // Init block properties
        Settings woodSettings = Settings.of(Material.WOOD, MapColor.OAK_TAN).strength(2.5f).sounds(BlockSoundGroup.WOOD);
        Settings pumpkinSettings = Settings.of(Material.GOURD, MapColor.ORANGE).strength(1).sounds(BlockSoundGroup.WOOD);
        // todo: use mapColorProvider
        Settings christmasSettings = Settings.of(Material.WOOD, MapColor.OAK_TAN).strength(2.5f).sounds(BlockSoundGroup.WOOD);
        Settings ironSettings = Settings.of(Material.METAL, MapColor.IRON_GRAY).strength(5, 6).sounds(BlockSoundGroup.METAL);
        Settings goldSettings = Settings.of(Material.METAL, MapColor.GOLD).strength(3, 6).sounds(BlockSoundGroup.METAL);
        Settings diamondSettings = Settings.of(Material.METAL, MapColor.DIAMOND_BLUE).strength(5, 6).sounds(BlockSoundGroup.METAL);
        Settings obsidianSettings = Settings.of(Material.STONE, MapColor.BLACK).strength(50, 1200);
        Settings netheriteSettings = Settings.of(Material.METAL, MapColor.BLACK).strength(50, 1200).sounds(BlockSoundGroup.NETHERITE);
        // Init and register blocks
        ChestBlock woodChestBlock = ChestCommon.chestBlock(Utils.id("wood_chest"), woodOpenStat, Utils.WOOD_TIER, woodSettings);
        ChestBlock pumpkinChestBlock = ChestCommon.chestBlock(Utils.id("pumpkin_chest"), pumpkinOpenStat, Utils.WOOD_TIER, pumpkinSettings);
        ChestBlock christmasChestBlock = ChestCommon.chestBlock(Utils.id("christmas_chest"), christmasOpenStat, Utils.WOOD_TIER, christmasSettings);
        ChestBlock ironChestBlock = ChestCommon.chestBlock(Utils.id("iron_chest"), ironOpenStat, Utils.IRON_TIER, ironSettings);
        ChestBlock goldChestBlock = ChestCommon.chestBlock(Utils.id("gold_chest"), goldOpenStat, Utils.GOLD_TIER, goldSettings);
        ChestBlock diamondChestBlock = ChestCommon.chestBlock(Utils.id("diamond_chest"), diamondOpenStat, Utils.DIAMOND_TIER, diamondSettings);
        ChestBlock obsidianChestBlock = ChestCommon.chestBlock(Utils.id("obsidian_chest"), obsidianOpenStat, Utils.OBSIDIAN_TIER, obsidianSettings);
        ChestBlock netheriteChestBlock = ChestCommon.chestBlock(Utils.id("netherite_chest"), netheriteOpenStat, Utils.NETHERITE_TIER, netheriteSettings);
        ChestBlock[] blocks = new ChestBlock[]{woodChestBlock, pumpkinChestBlock, christmasChestBlock, ironChestBlock, goldChestBlock, diamondChestBlock, obsidianChestBlock, netheriteChestBlock};
        // Init and register items
        BlockItem woodChestItem = ChestCommon.chestItem(Utils.WOOD_TIER, woodChestBlock, blockItemMaker);
        BlockItem pumpkinChestItem = ChestCommon.chestItem(Utils.WOOD_TIER, pumpkinChestBlock, blockItemMaker);
        BlockItem christmasChestItem = ChestCommon.chestItem(Utils.WOOD_TIER, christmasChestBlock, blockItemMaker);
        BlockItem ironChestItem = ChestCommon.chestItem(Utils.IRON_TIER, ironChestBlock, blockItemMaker);
        BlockItem goldChestItem = ChestCommon.chestItem(Utils.GOLD_TIER, goldChestBlock, blockItemMaker);
        BlockItem diamondChestItem = ChestCommon.chestItem(Utils.DIAMOND_TIER, diamondChestBlock, blockItemMaker);
        BlockItem obsidianChestItem = ChestCommon.chestItem(Utils.OBSIDIAN_TIER, obsidianChestBlock, blockItemMaker);
        BlockItem netheriteChestItem = ChestCommon.chestItem(Utils.NETHERITE_TIER, netheriteChestBlock, blockItemMaker);
        BlockItem[] items = new BlockItem[]{woodChestItem, pumpkinChestItem, christmasChestItem, ironChestItem, goldChestItem, diamondChestItem, obsidianChestItem, netheriteChestItem};
        // Init and register block entity type
        ChestCommon.blockEntityType = BlockEntityType.Builder.create((pos, state) -> new ChestBlockEntity(ChestCommon.getBlockEntityType(), pos, state), blocks).build(null);
        registrationConsumer.accept(blocks, items, ChestCommon.blockEntityType);
        // Register chest module icon & upgrade behaviours
        BaseApi.getInstance().offerTabIcon(netheriteChestItem, ChestCommon.ICON_SUITABILITY);
        Predicate<Block> isUpgradableChestBlock = (block) -> block instanceof ChestBlock || block instanceof net.minecraft.block.ChestBlock || woodenChestTag.contains(block);
        BaseApi.getInstance().defineBlockUpgradeBehaviour(isUpgradableChestBlock, ChestCommon::tryUpgradeBlock);
    }

    public static BlockEntityType<ChestBlockEntity> getBlockEntityType() {
        return blockEntityType;
    }

    private static ChestBlock chestBlock(Identifier blockId, Identifier stat, Tier tier, Settings settings) {
        ChestBlock block = new ChestBlock(tier.getBlockSettings().apply(settings.dynamicBounds()), blockId, tier.getId(), stat, tier.getSlotCount());
        BaseApi.getInstance().registerTieredBlock(block);
        return block;
    }

    private static BlockItem chestItem(Tier tier, ChestBlock block, BiFunction<Block, Item.Settings, BlockItem> blockItemMaker) {
        return blockItemMaker.apply(block, tier.getItemSettings().apply(new Item.Settings().group(Utils.TAB)));
    }

    static Identifier[] getChestTextures(ChestBlock[] blocks) {
        Identifier[] textures = new Identifier[blocks.length * CursedChestType.values().length];
        int index = 0;
        for (ChestBlock block : blocks) {
            Identifier blockId = block.getBlockId();
            for (CursedChestType type : CursedChestType.values()) {
                textures[index++] = ChestApi.INSTANCE.getChestTexture(blockId, type);
            }
        }
        return textures;
    }

    static void registerChestTextures(ChestBlock[] blocks) {
        for (ChestBlock block : blocks) {
            Identifier blockId = block.getBlockId();
            ChestApi.INSTANCE.declareChestTextures(
                    blockId, Utils.id("entity/" + blockId.getPath() + "/single"),
                    Utils.id("entity/" + blockId.getPath() + "/left"),
                    Utils.id("entity/" + blockId.getPath() + "/right"),
                    Utils.id("entity/" + blockId.getPath() + "/top"),
                    Utils.id("entity/" + blockId.getPath() + "/bottom"),
                    Utils.id("entity/" + blockId.getPath() + "/front"),
                    Utils.id("entity/" + blockId.getPath() + "/back"));
        }
    }

    private static boolean tryUpgradeBlock(ItemUsageContext context, Identifier from, Identifier to) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        ItemStack handStack = context.getStack();
        if (state.getBlock() instanceof ChestBlock) {
            if (ChestBlock.getBlockType(state) == DoubleBlockProperties.Type.SINGLE) {
                ChestCommon.upgradeSingleBlock(world, state, pos, from, to);
                handStack.decrement(1);
                return true;
            } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
                BlockPos otherPos = pos.offset(ChestBlock.getDirectionToAttached(state));
                BlockState otherState = world.getBlockState(otherPos);
                ChestCommon.upgradeSingleBlock(world, state, pos, from, to);
                ChestCommon.upgradeSingleBlock(world, otherState, otherPos, from, to);
                handStack.decrement(2);
                return true;
            }
        } else {
            if (net.minecraft.block.ChestBlock.getDoubleBlockType(state) == DoubleBlockProperties.Type.SINGLE) {
                ChestCommon.upgradeSingleBlock(world, state, pos, from, to);
                handStack.decrement(1);
                return true;
            } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
                BlockPos otherPos = pos.offset(net.minecraft.block.ChestBlock.getFacing(state));
                BlockState otherState = world.getBlockState(otherPos);
                ChestCommon.upgradeSingleBlock(world, state, pos, from, to);
                ChestCommon.upgradeSingleBlock(world, otherState, otherPos, from, to);
                handStack.decrement(2);
                return true;
            }
        }

        return false;
    }

    private static void upgradeSingleBlock(World world, BlockState state, BlockPos pos, Identifier from, Identifier to) {
        Block block = state.getBlock();
        boolean isExpandedStorageChest = block instanceof ChestBlock;
        int containerSize = !isExpandedStorageChest ? Utils.WOOD_STACK_COUNT : ((ChestBlock) BaseApi.getInstance().getTieredBlock(ChestCommon.BLOCK_TYPE, ((ChestBlock) block).getBlockTier())).getSlotCount();
        if (isExpandedStorageChest && ((ChestBlock) block).getBlockTier() == from || !isExpandedStorageChest && from == Utils.WOOD_TIER.getId()) {
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
                AbstractOpenableStorageBlock toBlock = (AbstractOpenableStorageBlock) BaseApi.getInstance().getTieredBlock(ChestCommon.BLOCK_TYPE, to);
                DefaultedList<ItemStack> inventory = DefaultedList.ofSize(toBlock.getSlotCount(), ItemStack.EMPTY);
                ContainerLock code = ContainerLock.fromNbt(tag);
                Inventories.readNbt(tag, inventory);
                world.removeBlockEntity(pos);
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
}
