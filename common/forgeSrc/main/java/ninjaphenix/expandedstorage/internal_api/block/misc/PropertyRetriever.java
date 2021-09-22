package ninjaphenix.expandedstorage.internal_api.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface PropertyRetriever<A> {

    <B> B get(Property<A, B> property);

    @SuppressWarnings("ClassCanBeRecord")
    class SingleRetriever<A> implements PropertyRetriever<A> {
        private final A single;

        public SingleRetriever(A single) {
            this.single = single;
        }

        @Override
        public <B> B get(Property<A, B> property) {
            return property.get(single);
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    class DoubleRetriever<A> implements PropertyRetriever<A> {
        private final A first;
        private final A second;

        public DoubleRetriever(A first, A second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public <B> B get(Property<A, B> property) {
            return property.get(first, second);
        }
    }

    // todo: decide params
    // Should essentially be a copy of mojangs code except Function<BlockState, Direction> instead of a property.
    static <A extends BlockEntity> PropertyRetriever<A> create(LevelAccessor level, BlockState state, BlockPos pos) {
        return PropertyRetriever.createDirect((A) level.getBlockEntity(pos));
    }

    static <A> PropertyRetriever<A> createDirect(A single) {
        return new PropertyRetriever.SingleRetriever<>(single);
    }
}
