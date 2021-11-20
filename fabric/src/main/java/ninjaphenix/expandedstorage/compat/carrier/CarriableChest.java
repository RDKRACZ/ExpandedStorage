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
package ninjaphenix.expandedstorage.compat.carrier;

import me.steven.carrier.api.CarrierComponent;
import net.minecraft.block.Block;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

public final class CarriableChest extends CarriableOldChest {
    public CarriableChest(Identifier id, Block parent) {
        super(id, parent);
    }

    @Override
    protected void preRenderBlock(PlayerEntity player, CarrierComponent component, MatrixStack stack, VertexConsumerProvider consumer, float delta, int light) {
        stack.translate(0.5D, 0.5D, 0.5D);
        stack.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(180.0F));
        stack.translate(-0.5D, -0.5D, -0.5D);
    }
}
