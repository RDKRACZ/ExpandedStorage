package ninjaphenix.expandedstorage.internal_api.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Experimental
public interface ClientMenuFactory<T extends AbstractContainerMenu> {
    T create(int windowId, Inventory inventory, FriendlyByteBuf buffer);
}
