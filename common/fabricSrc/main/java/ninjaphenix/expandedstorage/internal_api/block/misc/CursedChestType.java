package ninjaphenix.expandedstorage.internal_api.block.misc;

import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.Locale;

@Internal
@Experimental
public enum CursedChestType implements StringIdentifiable {
    TOP(-1),
    BOTTOM(-1),
    FRONT(0),
    BACK(2),
    LEFT(1),
    RIGHT(3),
    SINGLE(-1);

    private final String name;
    private final int offset;

    CursedChestType(int offset) {
        this.name = name().toLowerCase(Locale.ROOT);
        this.offset = offset;
    }

    @Override
    public String asString() {
        return name;
    }

    public int getOffset() {
        return offset;
    }

    public CursedChestType getOpposite() {
        if (this == CursedChestType.TOP) {
            return CursedChestType.BOTTOM;
        } else if (this == CursedChestType.BOTTOM) {
            return CursedChestType.TOP;
        } else if (this == CursedChestType.FRONT) {
            return CursedChestType.BACK;
        } else if (this == CursedChestType.BACK) {
            return CursedChestType.FRONT;
        } else if (this == CursedChestType.LEFT) {
            return CursedChestType.RIGHT;
        } else if (this == CursedChestType.RIGHT) {
            return CursedChestType.LEFT;
        }
        throw new IllegalStateException("CursedChestType.SINGLE CursedChestType has no opposite");
    }
}
