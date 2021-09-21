package ninjaphenix.expandedstorage;

import net.minecraft.client.texture.MissingSprite;
import net.minecraft.util.Identifier;
import ninjaphenix.expandedstorage.internal_api.ChestApi;
import ninjaphenix.expandedstorage.internal_api.block.misc.CursedChestType;

import java.util.HashMap;
import java.util.Map;

public final class ChestImpl implements ChestApi {
    private static ChestImpl instance;
    private final Map<Identifier, TextureCollection> textures = new HashMap<>();

    private ChestImpl() {

    }

    public static ChestApi getInstance() {
        if (instance == null) {
            instance = new ChestImpl();
        }
        return instance;
    }

    @Override
    public void declareChestTextures(Identifier block, Identifier singleTexture, Identifier leftTexture, Identifier rightTexture, Identifier topTexture, Identifier bottomTexture, Identifier frontTexture, Identifier backTexture) {
        if (!textures.containsKey(block)) {
            TextureCollection collection = new TextureCollection(singleTexture, leftTexture, rightTexture, topTexture, bottomTexture, frontTexture, backTexture);
            textures.put(block, collection);
        } else {
            throw new IllegalArgumentException("Tried registering chest textures for \"" + block + "\" which already has textures.");
        }
    }

    @Override
    public Identifier getChestTexture(Identifier block, CursedChestType chestType) {
        if (textures.containsKey(block)) {
            return textures.get(block).getTexture(chestType);
        } else {
            return MissingSprite.getMissingSpriteId();
        }
    }
}
