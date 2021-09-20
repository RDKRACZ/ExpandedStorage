package ninjaphenix.expandedstorage.mixin.chest;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import ninjaphenix.expandedstorage.chest.client.ChestBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

// todo: hopefully remove this before 1.18
@Mixin(LayerDefinitions.class)
public abstract class ModelLayersMixin {
    @Inject(method = "createRoots()Ljava/util/Map;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/builders/LayerDefinition;create(Lnet/minecraft/client/model/geom/builders/MeshDefinition;II)Lnet/minecraft/client/model/geom/builders/LayerDefinition;", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void addChestModelLayers(CallbackInfoReturnable<Map<ModelLayerLocation, ModelPart>> cir,
                                            ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> builder) {
        builder.put(ChestBlockEntityRenderer.SINGLE_LAYER, ChestBlockEntityRenderer.createSingleBodyLayer());
        builder.put(ChestBlockEntityRenderer.LEFT_LAYER, ChestBlockEntityRenderer.createLeftBodyLayer());
        builder.put(ChestBlockEntityRenderer.RIGHT_LAYER, ChestBlockEntityRenderer.createRightBodyLayer());
        builder.put(ChestBlockEntityRenderer.TOP_LAYER, ChestBlockEntityRenderer.createTopBodyLayer());
        builder.put(ChestBlockEntityRenderer.BOTTOM_LAYER, ChestBlockEntityRenderer.createBottomBodyLayer());
        builder.put(ChestBlockEntityRenderer.FRONT_LAYER, ChestBlockEntityRenderer.createFrontBodyLayer());
        builder.put(ChestBlockEntityRenderer.BACK_LAYER, ChestBlockEntityRenderer.createBackBodyLayer());
    }
}
