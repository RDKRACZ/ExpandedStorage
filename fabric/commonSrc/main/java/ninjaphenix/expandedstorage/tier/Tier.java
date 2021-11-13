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
package ninjaphenix.expandedstorage.tier;

import net.minecraft.block.AbstractBlock;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.function.UnaryOperator;

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


