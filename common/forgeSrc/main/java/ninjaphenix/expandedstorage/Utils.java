package ninjaphenix.expandedstorage;

import ninjaphenix.expandedstorage.tier.Tier;

import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public final class Utils {
    public static final String MOD_ID = "expandedstorage";
    public static final Component ALT_USE = new TranslatableComponent("tooltip.expandedstorage.alt_use",
            new KeybindComponent("key.sneak").withStyle(ChatFormatting.GOLD),
            new KeybindComponent("key.use").withStyle(ChatFormatting.GOLD));

    public static final int WOOD_STACK_COUNT = 27;

    public static final Tier WOOD_TIER = new Tier(Utils.id("wood"), WOOD_STACK_COUNT, UnaryOperator.identity(), UnaryOperator.identity());

    // Item Cooldown
    public static final int QUARTER_SECOND = 5;

    private Utils() {

    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(Utils.MOD_ID, path);
    }

    public static MutableComponent translation(String key, Object... params) {
        return new TranslatableComponent(key, params);
    }
}
