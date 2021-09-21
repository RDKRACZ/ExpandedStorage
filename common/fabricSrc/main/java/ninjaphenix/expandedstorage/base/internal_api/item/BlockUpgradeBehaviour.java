package ninjaphenix.expandedstorage.base.internal_api.item;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Experimental
public interface BlockUpgradeBehaviour {
    boolean tryUpgradeBlock(ItemUsageContext context, Identifier from, Identifier to);
}
