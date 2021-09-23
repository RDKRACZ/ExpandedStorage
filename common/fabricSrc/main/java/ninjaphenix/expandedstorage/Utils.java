package ninjaphenix.expandedstorage;

import net.minecraft.text.KeybindText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import ninjaphenix.expandedstorage.tier.Tier;

import java.util.function.UnaryOperator;

public final class Utils {
    public static final String MOD_ID = "expandedstorage";
    public static final Text ALT_USE = new TranslatableText("tooltip.expandedstorage.alt_use",
            new KeybindText("key.sneak").formatted(Formatting.GOLD),
            new KeybindText("key.use").formatted(Formatting.GOLD));

    public static final int WOOD_STACK_COUNT = 27;

    public static final Tier WOOD_TIER = new Tier(Utils.id("wood"), WOOD_STACK_COUNT, UnaryOperator.identity(), UnaryOperator.identity());

    // Item Cooldown
    public static final int QUARTER_SECOND = 5;

    private Utils() {

    }

    public static Identifier id(String path) {
        return new Identifier(Utils.MOD_ID, path);
    }

    public static MutableText translation(String key, Object... params) {
        return new TranslatableText(key, params);
    }
}
