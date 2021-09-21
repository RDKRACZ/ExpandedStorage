package ninjaphenix.expandedstorage.mixin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModels;
import ninjaphenix.expandedstorage.client.ChestBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

// todo: hopefully remove this before 1.18
@Mixin(EntityModels.class)
public abstract class ModelLayersMixin {
    @Inject(method = "getModels()Ljava/util/Map;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/TexturedModelData;of(Lnet/minecraft/client/model/ModelData;II)Lnet/minecraft/client/model/TexturedModelData;", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void addChestModelLayers(CallbackInfoReturnable<Map<EntityModelLayer, ModelPart>> cir,
                                            ImmutableMap.Builder<EntityModelLayer, TexturedModelData> builder) {
        builder.put(ChestBlockEntityRenderer.SINGLE_LAYER, ChestBlockEntityRenderer.createSingleBodyLayer());
        builder.put(ChestBlockEntityRenderer.LEFT_LAYER, ChestBlockEntityRenderer.createLeftBodyLayer());
        builder.put(ChestBlockEntityRenderer.RIGHT_LAYER, ChestBlockEntityRenderer.createRightBodyLayer());
        builder.put(ChestBlockEntityRenderer.TOP_LAYER, ChestBlockEntityRenderer.createTopBodyLayer());
        builder.put(ChestBlockEntityRenderer.BOTTOM_LAYER, ChestBlockEntityRenderer.createBottomBodyLayer());
        builder.put(ChestBlockEntityRenderer.FRONT_LAYER, ChestBlockEntityRenderer.createFrontBodyLayer());
        builder.put(ChestBlockEntityRenderer.BACK_LAYER, ChestBlockEntityRenderer.createBackBodyLayer());
    }
}
