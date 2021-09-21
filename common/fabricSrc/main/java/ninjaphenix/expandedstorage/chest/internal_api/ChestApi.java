package ninjaphenix.expandedstorage.chest.internal_api;

import net.minecraft.util.Identifier;
import ninjaphenix.expandedstorage.base.internal_api.block.misc.CursedChestType;
import ninjaphenix.expandedstorage.chest.ChestImpl;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Note this API is client side only, never call it on a dedicated server.
 */
@Experimental
@Internal
public interface ChestApi {
    ChestApi INSTANCE = ChestImpl.getInstance();

    void declareChestTextures(Identifier block,
                              Identifier singleTexture,
                              Identifier leftTexture,
                              Identifier rightTexture,
                              Identifier topTexture,
                              Identifier bottomTexture,
                              Identifier frontTexture,
                              Identifier backTexture);

    Identifier getChestTexture(Identifier block, CursedChestType chestType);
}
