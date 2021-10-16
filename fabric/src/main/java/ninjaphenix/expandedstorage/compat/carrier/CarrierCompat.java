/**
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
package ninjaphenix.expandedstorage.compat.carrier;

import me.steven.carrier.api.CarriableRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import ninjaphenix.expandedstorage.Utils;

public class CarrierCompat {
    public static void initialize() {
        CarrierCompat.registerChest(Utils.id("wood_chest"));
        CarrierCompat.registerChest(Utils.id("pumpkin_chest"));
        CarrierCompat.registerChest(Utils.id("christmas_chest"));
        CarrierCompat.registerChest(Utils.id("iron_chest"));
        CarrierCompat.registerChest(Utils.id("gold_chest"));
        CarrierCompat.registerChest(Utils.id("diamond_chest"));
        CarrierCompat.registerChest(Utils.id("obsidian_chest"));
        CarrierCompat.registerChest(Utils.id("netherite_chest"));

        CarrierCompat.registerOldChest(Utils.id("old_wood_chest"));
        CarrierCompat.registerOldChest(Utils.id("old_iron_chest"));
        CarrierCompat.registerOldChest(Utils.id("old_gold_chest"));
        CarrierCompat.registerOldChest(Utils.id("old_diamond_chest"));
        CarrierCompat.registerOldChest(Utils.id("old_obsidian_chest"));
        CarrierCompat.registerOldChest(Utils.id("old_netherite_chest"));
    }

    private static void registerChest(Identifier id) {
        CarriableRegistry.INSTANCE.register(id, new CarriableChest(id, Registry.BLOCK.get(id)));
    }

    private static void registerOldChest(Identifier id) {
        CarriableRegistry.INSTANCE.register(id, new CarriableOldChest(id, Registry.BLOCK.get(id)));
    }
}
