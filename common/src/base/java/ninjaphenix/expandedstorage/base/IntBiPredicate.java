package ninjaphenix.expandedstorage.base;

public interface IntBiPredicate {
    boolean test(int x, int y);

    static boolean never(int x, int y) {
        return false;
    }
}
