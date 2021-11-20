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
package ninjaphenix.expandedstorage.client;

import net.minecraft.util.Identifier;
import ninjaphenix.expandedstorage.block.misc.CursedChestType;

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

    public Identifier getTexture(CursedChestType type) {
        if (type == CursedChestType.TOP) {
            return top;
        } else if (type == CursedChestType.BOTTOM) {
            return bottom;
        } else if (type == CursedChestType.FRONT) {
            return front;
        } else if (type == CursedChestType.BACK) {
            return back;
        } else if (type == CursedChestType.LEFT) {
            return left;
        } else if (type == CursedChestType.RIGHT) {
            return right;
        } else if (type == CursedChestType.SINGLE) {
            return single;
        }
        throw new IllegalArgumentException("TextureCollection#getTexture received an unknown CursedChestType.");
    }
}
