package ninjaphenix.expandedstorage;

import net.minecraft.util.Identifier;
import ninjaphenix.expandedstorage.internal_api.block.misc.CursedChestType;

@SuppressWarnings("ClassCanBeRecord")
public final class TextureCollection {
    private final Identifier single;
    private final Identifier left;
    private final Identifier right;
    private final Identifier top;
    private final Identifier bottom;
    private final Identifier front;
    private final Identifier back;

    public TextureCollection(Identifier single, Identifier left, Identifier right,
                             Identifier top, Identifier bottom, Identifier front, Identifier back) {
        this.single = single;
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.front = front;
        this.back = back;
    }

    Identifier getTexture(CursedChestType type) {
        if (type == CursedChestType.TOP) {
            return this.top;
        } else if (type == CursedChestType.BOTTOM) {
            return this.bottom;
        } else if (type == CursedChestType.FRONT) {
            return this.front;
        } else if (type == CursedChestType.BACK) {
            return this.back;
        } else if (type == CursedChestType.LEFT) {
            return this.left;
        } else if (type == CursedChestType.RIGHT) {
            return this.right;
        } else if (type == CursedChestType.SINGLE) {
            return this.single;
        }
        throw new IllegalArgumentException("TextureCollection#getTexture received an unknown CursedChestType.");
    }
}
