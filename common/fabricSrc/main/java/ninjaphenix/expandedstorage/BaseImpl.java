package ninjaphenix.expandedstorage;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import ninjaphenix.expandedstorage.internal_api.BaseApi;
import ninjaphenix.expandedstorage.internal_api.block.AbstractStorageBlock;
import ninjaphenix.expandedstorage.internal_api.item.BlockUpgradeBehaviour;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public final class BaseImpl implements BaseApi {
    private static BaseImpl instance;
    private final Map<Predicate<Block>, BlockUpgradeBehaviour> BLOCK_UPGRADE_BEHAVIOURS = new HashMap<>();
    private final Map<Pair<Identifier, Identifier>, AbstractStorageBlock> BLOCKS = new HashMap<>();

    private BaseImpl() {

    }

    public static BaseImpl getInstance() {
        if (BaseImpl.instance == null) {
            BaseImpl.instance = new BaseImpl();
        }
        return BaseImpl.instance;
    }

    @Override
    public BlockUpgradeBehaviour getBlockUpgradeBehaviour(Block block) {
        for (Map.Entry<Predicate<Block>, BlockUpgradeBehaviour> entry : BLOCK_UPGRADE_BEHAVIOURS.entrySet()) {
            if (entry.getKey().test(block)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public void defineBlockUpgradeBehaviour(Predicate<Block> target, BlockUpgradeBehaviour behaviour) {
        BLOCK_UPGRADE_BEHAVIOURS.put(target, behaviour);
    }

    @Override
    public void registerTieredBlock(AbstractStorageBlock block) {
        BLOCKS.putIfAbsent(new Pair<>(block.getBlockType(), block.getBlockTier()), block);
    }

    @Override
    public AbstractStorageBlock getTieredBlock(Identifier blockType, Identifier tier) {
        return BLOCKS.get(new Pair<>(blockType, tier));
    }
}
