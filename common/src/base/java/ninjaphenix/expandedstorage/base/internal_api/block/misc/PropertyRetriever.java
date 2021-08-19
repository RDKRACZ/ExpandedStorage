package ninjaphenix.expandedstorage.base.internal_api.block.misc;

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
    static <A> PropertyRetriever<A> create() {
        // todo: implement
        return null;
    }

    static <A> PropertyRetriever<A> createDirect(A single) {
        return new PropertyRetriever.SingleRetriever<>(single);
    }
}
