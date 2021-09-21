package ninjaphenix.expandedstorage.internal_api.item;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.Locale;

@Internal
@Experimental
public enum MutationMode {
    MERGE,
    SPLIT,
    ROTATE;

    private static final MutationMode[] VALUES = MutationMode.values();

    public static MutationMode from(byte index) {
        if (index >= 0 && index < MutationMode.VALUES.length) {
            return MutationMode.VALUES[index];
        }
        return null;
    }

    public byte toByte() {
        return (byte) ordinal();
    }

    public MutationMode next() {
        return MutationMode.VALUES[(ordinal() + 1) % MutationMode.VALUES.length];
    }
}
