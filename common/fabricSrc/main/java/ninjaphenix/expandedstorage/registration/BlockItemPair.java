package ninjaphenix.expandedstorage.registration;

@SuppressWarnings("ClassCanBeRecord")
public final class BlockItemPair<B, I> {
    private final B block;
    private final I item;

    public BlockItemPair(B block, I item) {
        this.block = block;
        this.item = item;
    }

    public B getBlock() {
        return block;
    }

    public I getItem() {
        return item;
    }
}
