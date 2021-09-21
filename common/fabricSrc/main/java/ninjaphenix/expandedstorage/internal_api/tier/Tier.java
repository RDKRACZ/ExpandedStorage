package ninjaphenix.expandedstorage.internal_api.tier;

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
    private final UnaryOperator<Item.Settings> itemProperties;
    private final UnaryOperator<AbstractBlock.Settings> blockProperties;
    private final int slots;

    public Tier(Identifier id, int slots, UnaryOperator<AbstractBlock.Settings> blockProperties,
                UnaryOperator<Item.Settings> itemProperties) {
        this.id = id;
        this.slots = slots;
        this.itemProperties = itemProperties;
        this.blockProperties = blockProperties;
    }

    public final Identifier getId() {
        return id;
    }

    public final UnaryOperator<Item.Settings> getItemProperties() {
        return itemProperties;
    }

    public UnaryOperator<AbstractBlock.Settings> getBlockProperties() {
        return blockProperties;
    }

    public final int getSlotCount() {
        return slots;
    }
}


