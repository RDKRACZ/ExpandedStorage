package ninjaphenix.expandedstorage.block.misc;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public interface PropertyRetriever<A> {
    <B> Optional<B> get(Property<A, B> property);

    @SuppressWarnings("ClassCanBeRecord")
    class SingleRetriever<A> implements PropertyRetriever<A> {
        private final A single;

        public SingleRetriever(A single) {
            this.single = single;
        }

        @Override
        public <B> Optional<B> get(Property<A, B> property) {
            return Optional.ofNullable(property.get(single));
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
        public <B> Optional<B> get(Property<A, B> property) {
            return Optional.ofNullable(property.get(first, second));
        }
    }

    class NullRetriever<A> implements PropertyRetriever<A> {
        @Override
        public <B> Optional<B> get(Property<A, B> property) {
            return Optional.empty();
        }
    }

    static <A extends BlockEntity> PropertyRetriever<A> create(
            BlockEntityType<A> blockEntityType,
            Function<BlockState, DoubleBlockCombiner.BlockType> typeGetter,
            Function<BlockState, Direction> attachedDirectionGetter,
            Function<BlockState, Direction> directionGetter,
            BlockState state,
            LevelAccessor world,
            BlockPos pos,
            BiPredicate<LevelAccessor, BlockPos> blockInaccessible) {
        A entity = blockEntityType.getBlockEntity(world, pos);
        if (entity == null || blockInaccessible.test(world, pos)) {
            return new NullRetriever<>();
        } else {
            DoubleBlockCombiner.BlockType type = typeGetter.apply(state);
            if (type != DoubleBlockCombiner.BlockType.SINGLE) {
                BlockPos attachedPos = pos.relative(attachedDirectionGetter.apply(state));
                BlockState attachedState = world.getBlockState(attachedPos);
                if (attachedState.is(state.getBlock())) {
                    if (areTypesOpposite(type, typeGetter.apply(attachedState)) && directionGetter.apply(state) == directionGetter.apply(attachedState)) {
                        if (blockInaccessible.test(world, attachedPos)) {
                            return new NullRetriever<>();
                        }

                        A attachedEntity = blockEntityType.getBlockEntity(world, attachedPos);
                        if (attachedEntity != null) {
                            if (type == DoubleBlockCombiner.BlockType.FIRST) {
                                return new DoubleRetriever<>(entity, attachedEntity);
                            } else {
                                return new DoubleRetriever<>(attachedEntity, entity);
                            }
                        }
                    }
                }
            }
            return new SingleRetriever<>(entity);
        }
    }

    static boolean areTypesOpposite(DoubleBlockCombiner.BlockType type, DoubleBlockCombiner.BlockType otherType) {
        return type == DoubleBlockCombiner.BlockType.FIRST && otherType == DoubleBlockCombiner.BlockType.SECOND ||
                type == DoubleBlockCombiner.BlockType.SECOND && otherType == DoubleBlockCombiner.BlockType.FIRST;
    }

    static <A> PropertyRetriever<A> createDirect(A single) {
        return new PropertyRetriever.SingleRetriever<>(single);
    }
}
