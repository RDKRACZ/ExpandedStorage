package ninjaphenix.expandedstorage.tier;

import net.minecraft.block.AbstractBlock;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.function.UnaryOperator;

@Internal
@Experimental
@SuppressWarnings("ClassCanBeRecord")
public class Tier {
    private final Identifier id;
    private final UnaryOperator<Item.Settings> itemSettings;
    private final UnaryOperator<AbstractBlock.Settings> blockSettings;
    private final int slots;

    public Tier(Identifier id, int slots, UnaryOperator<AbstractBlock.Settings> blockSettings, UnaryOperator<Item.Settings> itemSettings) {
        this.id = id;
        this.slots = slots;
        this.blockSettings = blockSettings;
        this.itemSettings = itemSettings;
    }

    public final Identifier getId() {
        return id;
    }

    public final UnaryOperator<Item.Settings> getItemSettings() {
        return itemSettings;
    }

    public UnaryOperator<AbstractBlock.Settings> getBlockSettings() {
        return blockSettings;
    }

    public final int getSlotCount() {
        return slots;
    }
}


