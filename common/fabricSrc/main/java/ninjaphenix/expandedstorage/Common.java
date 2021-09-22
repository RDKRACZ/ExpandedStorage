package ninjaphenix.expandedstorage;

import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import ninjaphenix.expandedstorage.block.BarrelBlock;
import ninjaphenix.expandedstorage.block.ChestBlock;
import ninjaphenix.expandedstorage.block.OldChestBlock;
import ninjaphenix.expandedstorage.block.misc.BarrelBlockEntity;
import ninjaphenix.expandedstorage.block.misc.ChestBlockEntity;
import ninjaphenix.expandedstorage.block.misc.AbstractChestBlockEntity;
import ninjaphenix.expandedstorage.internal_api.Utils;
import ninjaphenix.expandedstorage.internal_api.block.AbstractOpenableStorageBlock;
import ninjaphenix.expandedstorage.internal_api.block.AbstractStorageBlock;
import ninjaphenix.expandedstorage.internal_api.block.misc.CursedChestType;
import ninjaphenix.expandedstorage.internal_api.item.BlockUpgradeBehaviour;
import ninjaphenix.expandedstorage.internal_api.tier.Tier;
import ninjaphenix.expandedstorage.item.StorageConversionKit;
import ninjaphenix.expandedstorage.item.StorageMutator;
import ninjaphenix.expandedstorage.wrappers.PlatformUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public final class Common {
    public static final ItemGroup GROUP = PlatformUtils.getInstance().createTab(() -> new ItemStack(Registry.ITEM.get(Utils.id("netherite_chest"))));
    public static final Identifier BARREL_BLOCK_TYPE = Utils.id("barrel");
    public static final Identifier CHEST_BLOCK_TYPE = Utils.id("cursed_chest");
    public static final Identifier OLD_CHEST_BLOCK_TYPE = Utils.id("old_cursed_chest");

    private static final Map<Predicate<Block>, BlockUpgradeBehaviour> BLOCK_UPGRADE_BEHAVIOURS = new HashMap<>();
    private static final Map<Pair<Identifier, Identifier>, AbstractStorageBlock> BLOCKS = new HashMap<>();
    private static final Map<Identifier, TextureCollection> CHEST_TEXTURES = PlatformUtils.getInstance().isClient() ? new HashMap<>() : null;

    private static final int IRON_STACK_COUNT = 54;
    private static final int GOLD_STACK_COUNT = 81;
    private static final int DIAMOND_STACK_COUNT = 108;
    private static final int OBSIDIAN_STACK_COUNT = 108;
    private static final int NETHERITE_STACK_COUNT = 135;
    private static final Tier IRON_TIER = new Tier(Utils.id("iron"), Common.IRON_STACK_COUNT, Settings::requiresTool, UnaryOperator.identity());
    private static final Tier GOLD_TIER = new Tier(Utils.id("gold"), Common.GOLD_STACK_COUNT, Settings::requiresTool, UnaryOperator.identity());
    private static final Tier DIAMOND_TIER = new Tier(Utils.id("diamond"), Common.DIAMOND_STACK_COUNT, Settings::requiresTool, UnaryOperator.identity());
    private static final Tier OBSIDIAN_TIER = new Tier(Utils.id("obsidian"), Common.OBSIDIAN_STACK_COUNT, Settings::requiresTool, UnaryOperator.identity());
    private static final Tier NETHERITE_TIER = new Tier(Utils.id("netherite"), Common.NETHERITE_STACK_COUNT, Settings::requiresTool, Item.Settings::fireproof);
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

    private static ChestBlock chestBlock(Identifier blockId, Identifier stat, Tier tier, Settings settings) {
        ChestBlock block = new ChestBlock(tier.getBlockSettings().apply(settings.dynamicBounds()), blockId, tier.getId(), stat, tier.getSlotCount());
        Common.registerTieredBlock(block);
        return block;
    }

    private static BlockItem chestItem(Tier tier, ChestBlock block, BiFunction<Block, Item.Settings, BlockItem> blockItemMaker) {
        return blockItemMaker.apply(block, tier.getItemSettings().apply(new Item.Settings().group(GROUP)));
    }

    private static BlockItem oldChestItem(Tier tier, OldChestBlock block) {
        return new BlockItem(block, tier.getItemSettings().apply(new Item.Settings().group(GROUP)));
    }

    private static OldChestBlock oldChestBlock(Identifier blockId, Identifier stat, Tier tier, Settings settings) {
        OldChestBlock block = new OldChestBlock(tier.getBlockSettings().apply(settings), blockId, tier.getId(), stat, tier.getSlotCount());
        Common.registerTieredBlock(block);
        return block;
    }

    private static BarrelBlock barrelBlock(Identifier blockId, Identifier stat, Tier tier, Settings settings) {
        BarrelBlock block = new BarrelBlock(tier.getBlockSettings().apply(settings), blockId, tier.getId(), stat, tier.getSlotCount());
        Common.registerTieredBlock(block);
        return block;
    }

    private static BlockItem barrelItem(Tier tier, BarrelBlock block) {
        return new BlockItem(block, tier.getItemSettings().apply(new Item.Settings().group(GROUP)));
    }

    static void registerChestContent(RegistrationConsumer<ChestBlock, BlockItem, ChestBlockEntity> registrationConsumer, Tag<Block> woodenChestTag, BiFunction<Block, Item.Settings, BlockItem> blockItemMaker) {
        // Init and register opening stats
        Identifier woodOpenStat = Common.registerStat(Utils.id("open_wood_chest"));
        Identifier pumpkinOpenStat = Common.registerStat(Utils.id("open_pumpkin_chest"));
        Identifier christmasOpenStat = Common.registerStat(Utils.id("open_christmas_chest"));
        Identifier ironOpenStat = Common.registerStat(Utils.id("open_iron_chest"));
        Identifier goldOpenStat = Common.registerStat(Utils.id("open_gold_chest"));
        Identifier diamondOpenStat = Common.registerStat(Utils.id("open_diamond_chest"));
        Identifier obsidianOpenStat = Common.registerStat(Utils.id("open_obsidian_chest"));
        Identifier netheriteOpenStat = Common.registerStat(Utils.id("open_netherite_chest"));
        // Init block properties
        Settings woodSettings = Settings.of(Material.WOOD, MapColor.OAK_TAN).strength(2.5f).sounds(BlockSoundGroup.WOOD);
        Settings pumpkinSettings = Settings.of(Material.GOURD, MapColor.ORANGE).strength(1).sounds(BlockSoundGroup.WOOD);
        // todo: use mapColorProvider, would require mixins...
        Settings christmasSettings = Settings.of(Material.WOOD, MapColor.OAK_TAN).strength(2.5f).sounds(BlockSoundGroup.WOOD);
        Settings ironSettings = Settings.of(Material.METAL, MapColor.IRON_GRAY).strength(5, 6).sounds(BlockSoundGroup.METAL);
        Settings goldSettings = Settings.of(Material.METAL, MapColor.GOLD).strength(3, 6).sounds(BlockSoundGroup.METAL);
        Settings diamondSettings = Settings.of(Material.METAL, MapColor.DIAMOND_BLUE).strength(5, 6).sounds(BlockSoundGroup.METAL);
        Settings obsidianSettings = Settings.of(Material.STONE, MapColor.BLACK).strength(50, 1200);
        Settings netheriteSettings = Settings.of(Material.METAL, MapColor.BLACK).strength(50, 1200).sounds(BlockSoundGroup.NETHERITE);
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
        Common.registerChestTextures(blocks);
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
        chestBlockEntityType = BlockEntityType.Builder.create((pos, state) -> new ChestBlockEntity(Common.getChestBlockEntityType(), pos, state), blocks).build(null);
        registrationConsumer.accept(blocks, items, chestBlockEntityType);
        // Register chest module icon & upgrade behaviours
        Predicate<Block> isUpgradableChestBlock = (block) -> block instanceof ChestBlock || block instanceof net.minecraft.block.ChestBlock || woodenChestTag.contains(block);
        Common.defineBlockUpgradeBehaviour(isUpgradableChestBlock, Common::tryUpgradeBlockToChest);
    }

    static void registerOldChestContent(RegistrationConsumer<OldChestBlock, BlockItem, AbstractChestBlockEntity> registrationConsumer) {
        // Init and register opening stats
        Identifier woodOpenStat = Common.registerStat(Utils.id("open_old_wood_chest"));
        Identifier ironOpenStat = Common.registerStat(Utils.id("open_old_iron_chest"));
        Identifier goldOpenStat = Common.registerStat(Utils.id("open_old_gold_chest"));
        Identifier diamondOpenStat = Common.registerStat(Utils.id("open_old_diamond_chest"));
        Identifier obsidianOpenStat = Common.registerStat(Utils.id("open_old_obsidian_chest"));
        Identifier netheriteOpenStat = Common.registerStat(Utils.id("open_old_netherite_chest"));
        // Init block properties
        Settings woodSettings = Settings.of(Material.WOOD, MapColor.OAK_TAN).strength(2.5f).sounds(BlockSoundGroup.WOOD);
        Settings ironSettings = Settings.of(Material.METAL, MapColor.IRON_GRAY).strength(5, 6).sounds(BlockSoundGroup.METAL);
        Settings goldSettings = Settings.of(Material.METAL, MapColor.GOLD).strength(3, 6).sounds(BlockSoundGroup.METAL);
        Settings diamondSettings = Settings.of(Material.METAL, MapColor.DIAMOND_BLUE).strength(5, 6).sounds(BlockSoundGroup.METAL);
        Settings obsidianSettings = Settings.of(Material.STONE, MapColor.BLACK).strength(50, 1200);
        Settings netheriteSettings = Settings.of(Material.METAL, MapColor.BLACK).strength(50, 1200).sounds(BlockSoundGroup.NETHERITE);
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
        oldChestBlockEntityType = BlockEntityType.Builder.create((pos, state) -> new AbstractChestBlockEntity(Common.getOldChestBlockEntityType(), pos, state, ((AbstractStorageBlock) state.getBlock()).getBlockId()), blocks).build(null);
        registrationConsumer.accept(blocks, items, oldChestBlockEntityType);
        // Register chest module icon & upgrade behaviours
        Predicate<Block> isUpgradableChestBlock = (block) -> block instanceof OldChestBlock;
        Common.defineBlockUpgradeBehaviour(isUpgradableChestBlock, Common::tryUpgradeBlockToOldChest);
    }

    static void registerBarrelContent(RegistrationConsumer<BarrelBlock, BlockItem, BarrelBlockEntity> registration, Tag<Block> woodenBarrelTag) {
        // Init and register opening stats
        Identifier ironOpenStat = Common.registerStat(Utils.id("open_iron_barrel"));
        Identifier goldOpenStat = Common.registerStat(Utils.id("open_gold_barrel"));
        Identifier diamondOpenStat = Common.registerStat(Utils.id("open_diamond_barrel"));
        Identifier obsidianOpenStat = Common.registerStat(Utils.id("open_obsidian_barrel"));
        Identifier netheriteOpenStat = Common.registerStat(Utils.id("open_netherite_barrel"));
        // Init block properties
        Settings ironSettings = Settings.of(Material.WOOD).strength(5, 6).sounds(BlockSoundGroup.WOOD);
        Settings goldSettings = Settings.of(Material.WOOD).strength(3, 6).sounds(BlockSoundGroup.WOOD);
        Settings diamondSettings = Settings.of(Material.WOOD).strength(5, 6).sounds(BlockSoundGroup.WOOD);
        Settings obsidianSettings = Settings.of(Material.WOOD).strength(50, 1200).sounds(BlockSoundGroup.WOOD);
        Settings netheriteSettings = Settings.of(Material.WOOD).strength(50, 1200).sounds(BlockSoundGroup.WOOD);
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
        barrelBlockEntityType = BlockEntityType.Builder.create((pos, state) -> new BarrelBlockEntity(Common.getBarrelBlockEntityType(), pos, state), blocks).build(null);
        registration.accept(blocks, items, barrelBlockEntityType);
        // Register chest module icon & upgrade behaviours
        Predicate<Block> isUpgradableBarrelBlock = (block) -> block instanceof BarrelBlock || block instanceof net.minecraft.block.BarrelBlock || woodenBarrelTag.contains(block);
        Common.defineBlockUpgradeBehaviour(isUpgradableBarrelBlock, Common::tryUpgradeBlockToBarrel);
    }

    private static boolean tryUpgradeBlockToBarrel(ItemUsageContext context, Identifier from, Identifier to) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        boolean isExpandedStorageBarrel = block instanceof BarrelBlock;
        int containerSize = !isExpandedStorageBarrel ? Utils.WOOD_STACK_COUNT : ((BarrelBlock) Common.getTieredBlock(BARREL_BLOCK_TYPE, ((BarrelBlock) block).getBlockTier())).getSlotCount();
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
                AbstractOpenableStorageBlock toBlock = (AbstractOpenableStorageBlock) Common.getTieredBlock(BARREL_BLOCK_TYPE, to);
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

    static Identifier[] getChestTextures(ChestBlock[] blocks) {
        Identifier[] textures = new Identifier[blocks.length * CursedChestType.values().length];
        int index = 0;
        for (ChestBlock block : blocks) {
            Identifier blockId = block.getBlockId();
            for (CursedChestType type : CursedChestType.values()) {
                textures[index++] = Common.getChestTexture(blockId, type);
            }
        }
        return textures;
    }

    static void registerChestTextures(ChestBlock[] blocks) {
        for (ChestBlock block : blocks) {
            Identifier blockId = block.getBlockId();
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

    private static boolean tryUpgradeBlockToChest(ItemUsageContext context, Identifier from, Identifier to) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        ItemStack handStack = context.getStack();
        if (state.getBlock() instanceof ChestBlock) {
            if (ChestBlock.getBlockType(state) == DoubleBlockProperties.Type.SINGLE) {
                Common.upgradeSingleBlockToChest(world, state, pos, from, to);
                handStack.decrement(1);
                return true;
            } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
                BlockPos otherPos = pos.offset(ChestBlock.getDirectionToAttached(state));
                BlockState otherState = world.getBlockState(otherPos);
                Common.upgradeSingleBlockToChest(world, state, pos, from, to);
                Common.upgradeSingleBlockToChest(world, otherState, otherPos, from, to);
                handStack.decrement(2);
                return true;
            }
        } else {
            if (net.minecraft.block.ChestBlock.getDoubleBlockType(state) == DoubleBlockProperties.Type.SINGLE) {
                Common.upgradeSingleBlockToChest(world, state, pos, from, to);
                handStack.decrement(1);
                return true;
            } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
                BlockPos otherPos = pos.offset(net.minecraft.block.ChestBlock.getFacing(state));
                BlockState otherState = world.getBlockState(otherPos);
                Common.upgradeSingleBlockToChest(world, state, pos, from, to);
                Common.upgradeSingleBlockToChest(world, otherState, otherPos, from, to);
                handStack.decrement(2);
                return true;
            }
        }

        return false;
    }

    private static void upgradeSingleBlockToChest(World world, BlockState state, BlockPos pos, Identifier from, Identifier to) {
        Block block = state.getBlock();
        boolean isExpandedStorageChest = block instanceof ChestBlock;
        int containerSize = !isExpandedStorageChest ? Utils.WOOD_STACK_COUNT : ((ChestBlock) Common.getTieredBlock(CHEST_BLOCK_TYPE, ((ChestBlock) block).getBlockTier())).getSlotCount();
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
                AbstractOpenableStorageBlock toBlock = (AbstractOpenableStorageBlock) Common.getTieredBlock(CHEST_BLOCK_TYPE, to);
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

    private static boolean tryUpgradeBlockToOldChest(ItemUsageContext context, Identifier from, Identifier to) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        ItemStack handStack = context.getStack();
        if (OldChestBlock.getBlockType(state) == DoubleBlockProperties.Type.SINGLE) {
            Common.upgradeSingleBlockToOldChest(world, state, pos, from, to);
            handStack.decrement(1);
            return true;
        } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
            BlockPos otherPos = pos.offset(OldChestBlock.getDirectionToAttached(state));
            BlockState otherState = world.getBlockState(otherPos);
            Common.upgradeSingleBlockToOldChest(world, state, pos, from, to);
            Common.upgradeSingleBlockToOldChest(world, otherState, otherPos, from, to);
            handStack.decrement(2);
            return true;
        }
        return false;
    }

    private static void upgradeSingleBlockToOldChest(World world, BlockState state, BlockPos pos, Identifier from, Identifier to) {
        if (((OldChestBlock) state.getBlock()).getBlockTier() == from) {
            AbstractOpenableStorageBlock toBlock = (AbstractOpenableStorageBlock) Common.getTieredBlock(OLD_CHEST_BLOCK_TYPE, to);
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

    static void registerBaseContent(Consumer<Pair<Identifier, Item>[]> itemRegistration) {
        //noinspection unchecked
        Pair<Identifier, Item>[] items = new Pair[16];
        items[0] = new Pair<>(Utils.id("chest_mutator"), new StorageMutator(new Item.Settings().maxCount(1).group(Common.GROUP)));
        Common.defineTierUpgradePath(items, Utils.WOOD_TIER, Common.IRON_TIER, Common.GOLD_TIER, Common.DIAMOND_TIER, Common.OBSIDIAN_TIER, Common.NETHERITE_TIER);
        itemRegistration.accept(items);
    }

    public static Identifier registerStat(Identifier stat) {
        Identifier rv = Registry.register(Registry.CUSTOM_STAT, stat, stat); // Forge doesn't provide registries for stats
        Stats.CUSTOM.getOrCreateStat(rv);
        return rv;
    }

    private static void defineTierUpgradePath(Pair<Identifier, Item>[] items, Tier... tiers) {
        int numTiers = tiers.length;
        int index = 1;
        for (int fromIndex = 0; fromIndex < numTiers - 1; fromIndex++) {
            Tier fromTier = tiers[fromIndex];
            for (int toIndex = fromIndex + 1; toIndex < numTiers; toIndex++) {
                Tier toTier = tiers[toIndex];
                Identifier itemId = Utils.id(fromTier.getId().getPath() + "_to_" + toTier.getId().getPath() + "_conversion_kit");
                Item.Settings properties = fromTier.getItemSettings()
                                                   .andThen(toTier.getItemSettings())
                                                   .apply(new Item.Settings().group(Common.GROUP).maxCount(16));
                Item kit = new StorageConversionKit(properties, fromTier.getId(), toTier.getId());
                items[index++] = new Pair<>(itemId, kit);
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

    private static void registerTieredBlock(AbstractStorageBlock block) {
        Common.BLOCKS.putIfAbsent(new Pair<>(block.getBlockType(), block.getBlockTier()), block);
    }

    public static AbstractStorageBlock getTieredBlock(Identifier blockType, Identifier tier) {
        return Common.BLOCKS.get(new Pair<>(blockType, tier));
    }

    public static void declareChestTextures(Identifier block, Identifier singleTexture, Identifier leftTexture, Identifier rightTexture, Identifier topTexture, Identifier bottomTexture, Identifier frontTexture, Identifier backTexture) {
        if (!Common.CHEST_TEXTURES.containsKey(block)) {
            TextureCollection collection = new TextureCollection(singleTexture, leftTexture, rightTexture, topTexture, bottomTexture, frontTexture, backTexture);
            Common.CHEST_TEXTURES.put(block, collection);
        } else {
            throw new IllegalArgumentException("Tried registering chest textures for \"" + block + "\" which already has textures.");
        }
    }

    public static Identifier getChestTexture(Identifier block, CursedChestType chestType) {
        if (Common.CHEST_TEXTURES.containsKey(block)) {
            return Common.CHEST_TEXTURES.get(block).getTexture(chestType);
        } else {
            return MissingSprite.getMissingSpriteId();
        }
    }
}
