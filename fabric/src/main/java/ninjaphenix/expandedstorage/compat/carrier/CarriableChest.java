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
