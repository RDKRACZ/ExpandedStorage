package ninjaphenix.expandedstorage.registration;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

import java.util.function.IntFunction;

public final class BlockItemCollection<B extends Block, I extends BlockItem> {
    private final B[] blocks;
    private final I[] items;

    @SafeVarargs
    private BlockItemCollection(IntFunction<B[]> blockArrayMaker, IntFunction<I[]> itemArrayMaker, BlockItemPair<B, I>... pairs) {
        blocks = blockArrayMaker.apply(pairs.length);
        items = itemArrayMaker.apply(pairs.length);
        for (int i = 0; i < pairs.length; i++) {
            var pair = pairs[i];
            blocks[i] = pair.getBlock();
            items[i] = pair.getItem();
        }
    }

    @SafeVarargs
    public static <B extends Block, I extends BlockItem> BlockItemCollection<B, I> of(IntFunction<B[]> blockArrayMaker, IntFunction<I[]> itemArrayMaker, BlockItemPair<B, I>... pairs) {
        return new BlockItemCollection<>(blockArrayMaker, itemArrayMaker, pairs);
    }

    public B[] getBlocks() {
        return blocks;
    }

    public I[] getItems() {
        return items;
    }
}
