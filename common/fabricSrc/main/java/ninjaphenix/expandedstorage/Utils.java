/*
 * Copyright 2021 NinjaPhenix
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ninjaphenix.expandedstorage;

import net.minecraft.text.KeybindText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public final class Utils {
    public static final String MOD_ID = "expandedstorage";
    public static final Text ALT_USE = new TranslatableText("tooltip.expandedstorage.alt_use",
            new KeybindText("key.sneak").formatted(Formatting.GOLD),
            new KeybindText("key.use").formatted(Formatting.GOLD));
    public static final int WOOD_STACK_COUNT = 27;
    public static final Identifier WOOD_TIER_ID = Utils.id("wood");
    public static final int QUARTER_SECOND = 5;

    private Utils() {
        throw new IllegalStateException("Should not instantiate this helper class.");
    }

    public static Identifier id(String path) {
        return new Identifier(Utils.MOD_ID, path);
    }

    public static MutableText translation(String key, Object... params) {
        return new TranslatableText(key, params);
    }
}
