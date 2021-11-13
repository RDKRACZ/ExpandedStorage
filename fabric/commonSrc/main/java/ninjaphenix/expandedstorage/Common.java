/*
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
import ninjaphenix.expandedstorage.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.block.BarrelBlock;
import ninjaphenix.expandedstorage.block.ChestBlock;
import ninjaphenix.expandedstorage.block.MiniChestBlock;
import ninjaphenix.expandedstorage.block.OpenableBlock;
import ninjaphenix.expandedstorage.block.entity.BarrelBlockEntity;
import ninjaphenix.expandedstorage.block.entity.ChestBlockEntity;
import ninjaphenix.expandedstorage.block.entity.MiniChestBlockEntity;
import ninjaphenix.expandedstorage.block.entity.OldChestBlockEntity;
import ninjaphenix.expandedstorage.block.entity.extendable.OpenableBlockEntity;
import ninjaphenix.expandedstorage.block.misc.CursedChestType;
import ninjaphenix.expandedstorage.block.strategies.ItemAccess;
import ninjaphenix.expandedstorage.block.strategies.Lockable;
import ninjaphenix.expandedstorage.client.TextureCollection;
import ninjaphenix.expandedstorage.item.BlockUpgradeBehaviour;
import ninjaphenix.expandedstorage.item.StorageConversionKit;
import ninjaphenix.expandedstorage.item.StorageMutator;
import ninjaphenix.expandedstorage.registration.BlockItemCollection;
import ninjaphenix.expandedstorage.registration.BlockItemPair;
import ninjaphenix.expandedstorage.registration.RegistrationConsumer;
import ninjaphenix.expandedstorage.tier.Tier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public final class Common {
    public static ItemGroup group;
    public static final Identifier BARREL_BLOCK_TYPE = Utils.id("barrel");
    public static final Identifier CHEST_BLOCK_TYPE = Utils.id("cursed_chest");
    public static final Identifier OLD_CHEST_BLOCK_TYPE = Utils.id("old_cursed_chest");
    public static final Identifier MINI_CHEST_BLOCK_TYPE = Utils.id("mini_chest");

    private static final Map<Predicate<Block>, BlockUpgradeBehaviour> BLOCK_UPGRADE_BEHAVIOURS = new HashMap<>();
    private static final Map<BlockTierId, OpenableBlock> BLOCKS = new HashMap<>();
    private static final Map<Identifier, TextureCollection> CHEST_TEXTURES = new HashMap<>();

    private static final int IRON_STACK_COUNT = 54;
    private static final int GOLD_STACK_COUNT = 81;
    private static final int DIAMOND_STACK_COUNT = 108;
    private static final int OBSIDIAN_STACK_COUNT = 108;
    private static final int NETHERITE_STACK_COUNT = 135;
    public static final Tier WOOD_TIER = new Tier(Utils.WOOD_TIER_ID, Utils.WOOD_STACK_COUNT, UnaryOperator.identity(), UnaryOperator.identity());
    private static final Tier IRON_TIER = new Tier(Utils.id("iron"), Common.IRON_STACK_COUNT, Settings::requiresTool, UnaryOperator.identity());
    private static final Tier GOLD_TIER = new Tier(Utils.id("gold"), Common.GOLD_STACK_COUNT, Settings::requiresTool, UnaryOperator.identity());
    private static final Tier DIAMOND_TIER = new Tier(Utils.id("diamond"), Common.DIAMOND_STACK_COUNT, Settings::requiresTool, UnaryOperator.identity());
    private static final Tier OBSIDIAN_TIER = new Tier(Utils.id("obsidian"), Common.OBSIDIAN_STACK_COUNT, Settings::requiresTool, UnaryOperator.identity());
    private static final Tier NETHERITE_TIER = new Tier(Utils.id("netherite"), Common.NETHERITE_STACK_COUNT, Settings::requiresTool, Item.Settings::fireproof);
    private static BlockEntityType<ChestBlockEntity> chestBlockEntityType;
    private static BlockEntityType<OldChestBlockEntity> oldChestBlockEntityType;
    private static BlockEntityType<BarrelBlockEntity> barrelBlockEntityType;
    private static BlockEntityType<MiniChestBlockEntity> miniChestBlockEntityType;

    private static Function<OpenableBlockEntity, ItemAccess> itemAccess;
    private static Function<OpenableBlockEntity, Lockable> lockable;

    public static BlockEntityType<ChestBlockEntity> getChestBlockEntityType() {
        return chestBlockEntityType;
    }

    public static BlockEntityType<OldChestBlockEntity> getOldChestBlockEntityType() {
        return oldChestBlockEntityType;
    }

    public static BlockEntityType<BarrelBlockEntity> getBarrelBlockEntityType() {
        return barrelBlockEntityType;
    }

    public static BlockEntityType<MiniChestBlockEntity> getMiniChestBlockEntityType() {
        return miniChestBlockEntityType;
    }

    private static BlockItemPair<ChestBlock, BlockItem> chestBlock(Identifier blockId, Identifier stat, Tier tier, Settings settings, BiFunction<Block, Item.Settings, BlockItem> blockItemMaker) {
        ChestBlock block = new ChestBlock(tier.getBlockSettings().apply(settings), blockId, tier.getId(), stat, tier.getSlotCount());
        Common.registerTieredBlock(block);
        return new BlockItemPair<>(block, blockItemMaker.apply(block, new Item.Settings().group(Common.group)));
    }

    private static BlockItemPair<MiniChestBlock, BlockItem> miniChestBlock(Identifier blockId, Identifier stat, Settings settings) {
        MiniChestBlock block = new MiniChestBlock(Common.WOOD_TIER.getBlockSettings().apply(settings), blockId, stat);
        Common.registerTieredBlock(block);
        BlockItem item = new BlockItem(block, Common.WOOD_TIER.getItemSettings().apply(new Item.Settings().group(Common.group)));
        return new BlockItemPair<>(block, item);
    }

    private static BlockItemPair<AbstractChestBlock, BlockItem> oldChestBlock(Identifier blockId, Identifier stat, Tier tier, Settings settings) {
        AbstractChestBlock block = new AbstractChestBlock(tier.getBlockSettings().apply(settings), blockId, tier.getId(), stat, tier.getSlotCount());
        Common.registerTieredBlock(block);
        BlockItem item = new BlockItem(block, tier.getItemSettings().apply(new Item.Settings().group(Common.group)));
        return new BlockItemPair<>(block, item);
    }

    private static BlockItemPair<BarrelBlock, BlockItem> barrelBlock(Identifier blockId, Identifier stat, Tier tier, Settings settings) {
        BarrelBlock block = new BarrelBlock(tier.getBlockSettings().apply(settings), blockId, tier.getId(), stat, tier.getSlotCount());
        Common.registerTieredBlock(block);
        BlockItem item = new BlockItem(block, tier.getItemSettings().apply(new Item.Settings().group(Common.group)));
        return new BlockItemPair<>(block, item);
    }

    static void setGroup(ItemGroup group) {
        Common.group = group;
    }

    static void registerChestContent(RegistrationConsumer<ChestBlock, BlockItem, ChestBlockEntity> registrationConsumer, Tag<Block> woodenChestTag, BiFunction<Block, Item.Settings, BlockItem> blockItemMaker, boolean isClient) {
        // Init block settings
        Settings woodSettings = Settings.of(Material.WOOD, MapColor.OAK_TAN).strength(2.5f).sounds(BlockSoundGroup.WOOD);
        Settings pumpkinSettings = Settings.of(Material.GOURD, MapColor.ORANGE).strength(1).sounds(BlockSoundGroup.WOOD);
        Settings christmasSettings = Settings.of(Material.WOOD, state -> {
            CursedChestType type = state.get(AbstractChestBlock.CURSED_CHEST_TYPE);
            if (type == CursedChestType.SINGLE) {
                return MapColor.RED;
            } else if (type == CursedChestType.FRONT || type == CursedChestType.BACK) {
                return MapColor.DARK_GREEN;
            }
            return MapColor.WHITE;
        }).strength(2.5f).sounds(BlockSoundGroup.WOOD);
        Settings ironSettings = Settings.of(Material.METAL, MapColor.IRON_GRAY).strength(5, 6).sounds(BlockSoundGroup.METAL);
        Settings goldSettings = Settings.of(Material.METAL, MapColor.GOLD).strength(3, 6).sounds(BlockSoundGroup.METAL);
        Settings diamondSettings = Settings.of(Material.METAL, MapColor.DIAMOND_BLUE).strength(5, 6).sounds(BlockSoundGroup.METAL);
        Settings obsidianSettings = Settings.of(Material.STONE, MapColor.BLACK).strength(50, 1200);
        Settings netheriteSettings = Settings.of(Material.METAL, MapColor.BLACK).strength(50, 1200).sounds(BlockSoundGroup.NETHERITE);
        // Init content
        BlockItemCollection<ChestBlock, BlockItem> content = BlockItemCollection.of(ChestBlock[]::new, BlockItem[]::new,
                Common.chestBlock(Utils.id("wood_chest"), Common.stat("open_wood_chest"), Common.WOOD_TIER, woodSettings, blockItemMaker),
                Common.chestBlock(Utils.id("pumpkin_chest"), Common.stat("open_pumpkin_chest"), Common.WOOD_TIER, pumpkinSettings, blockItemMaker),
                Common.chestBlock(Utils.id("christmas_chest"), Common.stat("open_christmas_chest"), Common.WOOD_TIER, christmasSettings, blockItemMaker),
                Common.chestBlock(Utils.id("iron_chest"), Common.stat("open_iron_chest"), Common.IRON_TIER, ironSettings, blockItemMaker),
                Common.chestBlock(Utils.id("gold_chest"), Common.stat("open_gold_chest"), Common.GOLD_TIER, goldSettings, blockItemMaker),
                Common.chestBlock(Utils.id("diamond_chest"), Common.stat("open_diamond_chest"), Common.DIAMOND_TIER, diamondSettings, blockItemMaker),
                Common.chestBlock(Utils.id("obsidian_chest"), Common.stat("open_obsidian_chest"), Common.OBSIDIAN_TIER, obsidianSettings, blockItemMaker),
                Common.chestBlock(Utils.id("netherite_chest"), Common.stat("open_netherite_chest"), Common.NETHERITE_TIER, netheriteSettings, blockItemMaker)
        );
        if (isClient) {
            Common.registerChestTextures(content.getBlocks());
        }
        // Init block entity type
        chestBlockEntityType = BlockEntityType.Builder.create(Common::createChestBlockEntity, content.getBlocks()).build(null);
        registrationConsumer.accept(content, chestBlockEntityType);
        // Register chest upgrade behaviours
        Predicate<Block> isUpgradableChestBlock = (block) -> block instanceof ChestBlock || block instanceof net.minecraft.block.ChestBlock || woodenChestTag.contains(block);
        Common.defineBlockUpgradeBehaviour(isUpgradableChestBlock, Common::tryUpgradeBlockToChest);
    }

    static void registerOldChestContent(RegistrationConsumer<AbstractChestBlock, BlockItem, OldChestBlockEntity> registrationConsumer) {
        // Init block settings
        Settings woodSettings = Settings.of(Material.WOOD, MapColor.OAK_TAN).strength(2.5f).sounds(BlockSoundGroup.WOOD);
        Settings ironSettings = Settings.of(Material.METAL, MapColor.IRON_GRAY).strength(5, 6).sounds(BlockSoundGroup.METAL);
        Settings goldSettings = Settings.of(Material.METAL, MapColor.GOLD).strength(3, 6).sounds(BlockSoundGroup.METAL);
        Settings diamondSettings = Settings.of(Material.METAL, MapColor.DIAMOND_BLUE).strength(5, 6).sounds(BlockSoundGroup.METAL);
        Settings obsidianSettings = Settings.of(Material.STONE, MapColor.BLACK).strength(50, 1200);
        Settings netheriteSettings = Settings.of(Material.METAL, MapColor.BLACK).strength(50, 1200).sounds(BlockSoundGroup.NETHERITE);
        // Init content
        BlockItemCollection<AbstractChestBlock, BlockItem> content = BlockItemCollection.of(AbstractChestBlock[]::new, BlockItem[]::new,
                Common.oldChestBlock(Utils.id("old_wood_chest"), Common.stat("open_old_wood_chest"), Common.WOOD_TIER, woodSettings),
                Common.oldChestBlock(Utils.id("old_iron_chest"), Common.stat("open_old_iron_chest"), Common.IRON_TIER, ironSettings),
                Common.oldChestBlock(Utils.id("old_gold_chest"), Common.stat("open_old_gold_chest"), Common.GOLD_TIER, goldSettings),
                Common.oldChestBlock(Utils.id("old_diamond_chest"), Common.stat("open_old_diamond_chest"), Common.DIAMOND_TIER, diamondSettings),
                Common.oldChestBlock(Utils.id("old_obsidian_chest"), Common.stat("open_old_obsidian_chest"), Common.OBSIDIAN_TIER, obsidianSettings),
                Common.oldChestBlock(Utils.id("old_netherite_chest"), Common.stat("open_old_netherite_chest"), Common.NETHERITE_TIER, netheriteSettings)
        );
        // Init block entity type
        oldChestBlockEntityType = BlockEntityType.Builder.create(Common::createOldChestBlockEntity, content.getBlocks()).build(null);
        registrationConsumer.accept(content, oldChestBlockEntityType);
        // Register upgrade behaviours
        Predicate<Block> isUpgradableChestBlock = (block) -> block.getClass() == AbstractChestBlock.class;
        Common.defineBlockUpgradeBehaviour(isUpgradableChestBlock, Common::tryUpgradeBlockToOldChest);
    }

    static void registerMiniChestContent(RegistrationConsumer<MiniChestBlock, BlockItem, MiniChestBlockEntity> registrationConsumer) {
        // Init and register opening stats
        Identifier woodOpenStat = Common.stat("open_wood_mini_chest");
        // Init block settings
        Settings woodSettings = Settings.of(Material.WOOD, MapColor.OAK_TAN).strength(2.5f).sounds(BlockSoundGroup.WOOD);
        Settings pumpkinSettings = Settings.of(Material.GOURD, MapColor.ORANGE).strength(1).sounds(BlockSoundGroup.WOOD);
        Settings redPresentSettings = Settings.of(Material.WOOD, MapColor.RED).strength(2.5f).sounds(BlockSoundGroup.WOOD);
        Settings whitePresentSettings = Settings.of(Material.WOOD, MapColor.WHITE).strength(2.5f).sounds(BlockSoundGroup.WOOD);
        Settings candyCanePresentSettings = Settings.of(Material.WOOD, MapColor.WHITE).strength(2.5f).sounds(BlockSoundGroup.WOOD);
        Settings greenPresentSettings = Settings.of(Material.WOOD, MapColor.DARK_GREEN).strength(2.5f).sounds(BlockSoundGroup.WOOD);
        Settings lavenderPresentSettings = Settings.of(Material.WOOD, MapColor.PURPLE).strength(2.5f).sounds(BlockSoundGroup.WOOD);
        Settings pinkAmethystPresentSettings = Settings.of(Material.WOOD, MapColor.PURPLE).strength(2.5f).sounds(BlockSoundGroup.WOOD);
        // Init content
        BlockItemCollection<MiniChestBlock, BlockItem> content = BlockItemCollection.of(MiniChestBlock[]::new, BlockItem[]::new,
                Common.miniChestBlock(Utils.id("vanilla_wood_mini_chest"), woodOpenStat, woodSettings),
                Common.miniChestBlock(Utils.id("wood_mini_chest"), woodOpenStat, woodSettings),
                Common.miniChestBlock(Utils.id("pumpkin_mini_chest"), Common.stat("open_pumpkin_mini_chest"), pumpkinSettings),
                Common.miniChestBlock(Utils.id("red_mini_present"), Common.stat("open_red_mini_present"), redPresentSettings),
                Common.miniChestBlock(Utils.id("white_mini_present"), Common.stat("open_white_mini_present"), whitePresentSettings),
                Common.miniChestBlock(Utils.id("candy_cane_mini_present"), Common.stat("open_candy_cane_mini_present"), candyCanePresentSettings),
                Common.miniChestBlock(Utils.id("green_mini_present"), Common.stat("open_green_mini_present"), greenPresentSettings),
                Common.miniChestBlock(Utils.id("lavender_mini_present"), Common.stat("open_lavender_mini_present"), lavenderPresentSettings),
                Common.miniChestBlock(Utils.id("pink_amethyst_mini_present"), Common.stat("open_pink_amethyst_mini_present"), pinkAmethystPresentSettings)
        );
        // Init block entity type
        miniChestBlockEntityType = BlockEntityType.Builder.create(Common::createMiniChestBlockEntity, content.getBlocks()).build(null);
        registrationConsumer.accept(content, miniChestBlockEntityType);
    }

    static void registerBarrelContent(RegistrationConsumer<BarrelBlock, BlockItem, BarrelBlockEntity> registration, Tag<Block> woodenBarrelTag) {
        // Init block settings
        Settings ironSettings = Settings.of(Material.WOOD).strength(5, 6).sounds(BlockSoundGroup.WOOD);
        Settings goldSettings = Settings.of(Material.WOOD).strength(3, 6).sounds(BlockSoundGroup.WOOD);
        Settings diamondSettings = Settings.of(Material.WOOD).strength(5, 6).sounds(BlockSoundGroup.WOOD);
        Settings obsidianSettings = Settings.of(Material.WOOD).strength(50, 1200).sounds(BlockSoundGroup.WOOD);
        Settings netheriteSettings = Settings.of(Material.WOOD).strength(50, 1200).sounds(BlockSoundGroup.WOOD);
        // Init content
        BlockItemCollection<BarrelBlock, BlockItem> content = BlockItemCollection.of(BarrelBlock[]::new, BlockItem[]::new,
                Common.barrelBlock(Utils.id("iron_barrel"), Common.stat("open_iron_barrel"), Common.IRON_TIER, ironSettings),
                Common.barrelBlock(Utils.id("gold_barrel"), Common.stat("open_gold_barrel"), Common.GOLD_TIER, goldSettings),
                Common.barrelBlock(Utils.id("diamond_barrel"), Common.stat("open_diamond_barrel"), Common.DIAMOND_TIER, diamondSettings),
                Common.barrelBlock(Utils.id("obsidian_barrel"), Common.stat("open_obsidian_barrel"), Common.OBSIDIAN_TIER, obsidianSettings),
                Common.barrelBlock(Utils.id("netherite_barrel"), Common.stat("open_netherite_barrel"), Common.NETHERITE_TIER, netheriteSettings)
        );
        // Init block entity type
        barrelBlockEntityType = BlockEntityType.Builder.create(Common::createBarrelBlockEntity, content.getBlocks()).build(null);
        registration.accept(content, barrelBlockEntityType);
        // Register upgrade behaviours
        Predicate<Block> isUpgradableBarrelBlock = (block) -> block instanceof BarrelBlock || block instanceof net.minecraft.block.BarrelBlock || woodenBarrelTag.contains(block);
        Common.defineBlockUpgradeBehaviour(isUpgradableBarrelBlock, Common::tryUpgradeBlockToBarrel);
    }

    private static boolean tryUpgradeBlockToBarrel(ItemUsageContext context, Identifier from, Identifier to) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        boolean isExpandedStorageBarrel = block instanceof BarrelBlock;
        int inventorySize = !isExpandedStorageBarrel ? Utils.WOOD_STACK_COUNT : Common.getTieredBlock(BARREL_BLOCK_TYPE, ((BarrelBlock) block).getBlockTier()).getSlotCount();
        if (isExpandedStorageBarrel && ((BarrelBlock) block).getBlockTier() == from || !isExpandedStorageBarrel && from == Utils.WOOD_TIER_ID) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            //noinspection ConstantConditions
            NbtCompound tag = blockEntity.createNbt();
            boolean verifiedSize = blockEntity instanceof Inventory inventory && inventory.size() == inventorySize;
            if (!verifiedSize) { // Cannot verify inventory size, we'll let it upgrade if it has or has less than 27 items
                if (tag.contains("Items", NbtElement.LIST_TYPE)) {
                    NbtList items = tag.getList("Items", NbtElement.COMPOUND_TYPE);
                    if (items.size() <= inventorySize) {
                        verifiedSize = true;
                    }
                }
            }
            if (verifiedSize) {
                OpenableBlock toBlock = Common.getTieredBlock(BARREL_BLOCK_TYPE, to);
                DefaultedList<ItemStack> inventory = DefaultedList.ofSize(toBlock.getSlotCount(), ItemStack.EMPTY);
                ContainerLock code = ContainerLock.fromNbt(tag);
                Inventories.readNbt(tag, inventory);
                world.removeBlockEntity(pos);
                BlockState newState = toBlock.getDefaultState().with(Properties.FACING, state.get(Properties.FACING));
                if (world.setBlockState(pos, newState)) {
                    BlockEntity newEntity = world.getBlockEntity(pos);
                    //noinspection ConstantConditions
                    NbtCompound newTag = newEntity.createNbt();
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
        int inventorySize = !isExpandedStorageChest ? Utils.WOOD_STACK_COUNT : Common.getTieredBlock(CHEST_BLOCK_TYPE, ((ChestBlock) block).getBlockTier()).getSlotCount();
        if (isExpandedStorageChest && ((ChestBlock) block).getBlockTier() == from || !isExpandedStorageChest && from == Utils.WOOD_TIER_ID) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            //noinspection ConstantConditions
            NbtCompound tag = blockEntity.createNbt();
            boolean verifiedSize = blockEntity instanceof Inventory inventory && inventory.size() == inventorySize;
            if (!verifiedSize) { // Cannot verify inventory size, we'll let it upgrade if it has or has less than 27 items
                if (tag.contains("Items", NbtElement.LIST_TYPE)) {
                    NbtList items = tag.getList("Items", NbtElement.COMPOUND_TYPE);
                    if (items.size() <= inventorySize) {
                        verifiedSize = true;
                    }
                }
            }
            if (verifiedSize) {
                ChestBlock toBlock = (ChestBlock) Common.getTieredBlock(CHEST_BLOCK_TYPE, to);
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
                    NbtCompound newTag = newEntity.createNbt();
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
        if (AbstractChestBlock.getBlockType(state) == DoubleBlockProperties.Type.SINGLE) {
            Common.upgradeSingleBlockToOldChest(world, state, pos, from, to);
            handStack.decrement(1);
            return true;
        } else if (handStack.getCount() > 1 || (player != null && player.isCreative())) {
            BlockPos otherPos = pos.offset(AbstractChestBlock.getDirectionToAttached(state));
            BlockState otherState = world.getBlockState(otherPos);
            Common.upgradeSingleBlockToOldChest(world, state, pos, from, to);
            Common.upgradeSingleBlockToOldChest(world, otherState, otherPos, from, to);
            handStack.decrement(2);
            return true;
        }
        return false;
    }

    private static void upgradeSingleBlockToOldChest(World world, BlockState state, BlockPos pos, Identifier from, Identifier to) {
        if (((AbstractChestBlock) state.getBlock()).getBlockTier() == from) {
            AbstractChestBlock toBlock = (AbstractChestBlock) Common.getTieredBlock(OLD_CHEST_BLOCK_TYPE, to);
            DefaultedList<ItemStack> inventory = DefaultedList.ofSize(toBlock.getSlotCount(), ItemStack.EMPTY);
            //noinspection ConstantConditions
            NbtCompound tag = world.getBlockEntity(pos).createNbt();
            ContainerLock code = ContainerLock.fromNbt(tag);
            Inventories.readNbt(tag, inventory);
            world.removeBlockEntity(pos);
            BlockState newState = toBlock.getDefaultState().with(Properties.HORIZONTAL_FACING, state.get(Properties.HORIZONTAL_FACING)).with(AbstractChestBlock.CURSED_CHEST_TYPE, state.get(AbstractChestBlock.CURSED_CHEST_TYPE));
            if (world.setBlockState(pos, newState)) {
                BlockEntity newEntity = world.getBlockEntity(pos);
                //noinspection ConstantConditions
                NbtCompound newTag = newEntity.createNbt();
                Inventories.writeNbt(newTag, inventory);
                code.writeNbt(newTag);
                newEntity.readNbt(newTag);
            }
        }
    }

    static void registerBaseContent(Consumer<Pair<Identifier, Item>[]> itemRegistration,
                                    @SuppressWarnings("SameParameterValue") boolean wrapTooltipsManually) {
        //noinspection unchecked
        Pair<Identifier, Item>[] items = new Pair[16];
        items[0] = new Pair<>(Utils.id("chest_mutator"), new StorageMutator(new Item.Settings().maxCount(1).group(Common.group)));
        Common.defineTierUpgradePath(items, wrapTooltipsManually, Common.WOOD_TIER, Common.IRON_TIER, Common.GOLD_TIER, Common.DIAMOND_TIER, Common.OBSIDIAN_TIER, Common.NETHERITE_TIER);
        itemRegistration.accept(items);
    }

    public static Identifier stat(String stat) {
        Identifier statId = Utils.id(stat);
        Registry.register(Registry.CUSTOM_STAT, statId, statId); // Forge doesn't provide custom registries for stats
        Stats.CUSTOM.getOrCreateStat(statId);
        return statId;
    }

    private static void defineTierUpgradePath(Pair<Identifier, Item>[] items, boolean wrapTooltipManually, Tier... tiers) {
        int numTiers = tiers.length;
        int index = 1;
        for (int fromIndex = 0; fromIndex < numTiers - 1; fromIndex++) {
            Tier fromTier = tiers[fromIndex];
            for (int toIndex = fromIndex + 1; toIndex < numTiers; toIndex++) {
                Tier toTier = tiers[toIndex];
                Identifier itemId = Utils.id(fromTier.getId().getPath() + "_to_" + toTier.getId().getPath() + "_conversion_kit");
                Item.Settings properties = fromTier.getItemSettings()
                                                   .andThen(toTier.getItemSettings())
                                                   .apply(new Item.Settings().group(Common.group).maxCount(16));
                Item kit = new StorageConversionKit(properties, fromTier.getId(), toTier.getId(), wrapTooltipManually);
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

    private static BarrelBlockEntity createBarrelBlockEntity(BlockPos pos, BlockState state) {
        return new BarrelBlockEntity(Common.getBarrelBlockEntityType(), pos, state, ((BarrelBlock) state.getBlock()).getBlockId(), Common.itemAccess, Common.lockable);
    }

    public static void setSharedStrategies(Function<OpenableBlockEntity, ItemAccess> itemAccess, Function<OpenableBlockEntity, Lockable> lockable) {
        Common.itemAccess = itemAccess;
        Common.lockable = lockable;
    }

    private static OldChestBlockEntity createOldChestBlockEntity(BlockPos pos, BlockState state) {
        return new OldChestBlockEntity(Common.getOldChestBlockEntityType(), pos, state, ((AbstractChestBlock) state.getBlock()).getBlockId(), Common.itemAccess, Common.lockable);
    }

    private static ChestBlockEntity createChestBlockEntity(BlockPos pos, BlockState state) {
        return new ChestBlockEntity(Common.getChestBlockEntityType(), pos, state, ((ChestBlock) state.getBlock()).getBlockId(), Common.itemAccess, Common.lockable);
    }

    private static MiniChestBlockEntity createMiniChestBlockEntity(BlockPos pos, BlockState state) {
        return new MiniChestBlockEntity(Common.getMiniChestBlockEntityType(), pos, state, ((MiniChestBlock) state.getBlock()).getBlockId(), Common.itemAccess, Common.lockable);

    }

    @SuppressWarnings("ClassCanBeRecord")
    private static class BlockTierId {
        private final Identifier blockType;
        private final Identifier blockTier;

        private BlockTierId(Identifier blockType, Identifier blockTier) {
            this.blockType = blockType;
            this.blockTier = blockTier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof BlockTierId other)
                return blockType.equals(other.blockType) && blockTier.equals(other.blockTier);
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(blockType, blockTier);
        }
    }

    private static void registerTieredBlock(OpenableBlock block) {
        Common.BLOCKS.putIfAbsent(new BlockTierId(block.getBlockType(), block.getBlockTier()), block);
    }

    public static OpenableBlock getTieredBlock(Identifier blockType, Identifier tier) {
        return Common.BLOCKS.get(new BlockTierId(blockType, tier));
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
