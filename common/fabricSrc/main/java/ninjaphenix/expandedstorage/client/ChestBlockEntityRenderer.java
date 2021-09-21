package ninjaphenix.expandedstorage.client;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.block.BlockState;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import ninjaphenix.expandedstorage.block.ChestBlock;
import ninjaphenix.expandedstorage.block.misc.ChestBlockEntity;
import ninjaphenix.expandedstorage.internal_api.ChestApi;
import ninjaphenix.expandedstorage.internal_api.Utils;
import ninjaphenix.expandedstorage.internal_api.block.AbstractChestBlock;
import ninjaphenix.expandedstorage.internal_api.block.misc.CursedChestType;
import ninjaphenix.expandedstorage.internal_api.block.misc.FaceRotation;
import ninjaphenix.expandedstorage.internal_api.block.misc.Property;
import ninjaphenix.expandedstorage.internal_api.block.misc.PropertyRetriever;

public final class ChestBlockEntityRenderer implements BlockEntityRenderer<ChestBlockEntity> {
    // todo: hopefully we can remove this mess once *hopefully* this is all json, 1.18
    public static final EntityModelLayer SINGLE_LAYER = new EntityModelLayer(Utils.resloc("single_chest"), "main");
    public static final EntityModelLayer LEFT_LAYER = new EntityModelLayer(Utils.resloc("left_chest"), "main");
    public static final EntityModelLayer RIGHT_LAYER = new EntityModelLayer(Utils.resloc("right_chest"), "main");
    public static final EntityModelLayer TOP_LAYER = new EntityModelLayer(Utils.resloc("top_chest"), "main");
    public static final EntityModelLayer BOTTOM_LAYER = new EntityModelLayer(Utils.resloc("bottom_chest"), "main");
    public static final EntityModelLayer FRONT_LAYER = new EntityModelLayer(Utils.resloc("front_chest"), "main");
    public static final EntityModelLayer BACK_LAYER = new EntityModelLayer(Utils.resloc("back_chest"), "main");
    private static final BlockState DEFAULT_STATE = Registry.BLOCK.get(Utils.resloc("wood_chest")).getDefaultState();

    private static final Property<ChestBlockEntity, Float2FloatFunction> LID_OPENNESS_FUNCTION_GETTER = new Property<>() {
        @Override
        public Float2FloatFunction get(ChestBlockEntity first, ChestBlockEntity second) {
            return (delta) -> Math.max(first.getLidOpenness(delta), second.getLidOpenness(delta));
        }

        @Override
        public Float2FloatFunction get(ChestBlockEntity single) {
            return single::getLidOpenness;
        }
    };

    private static final Property<ChestBlockEntity, Int2IntFunction> BRIGHTNESS_PROPERTY = new Property<>() {
        @Override
        public Int2IntFunction get(ChestBlockEntity first, ChestBlockEntity second) {
            return i -> {
                //noinspection ConstantConditions
                int firstLightColor = WorldRenderer.getLightmapCoordinates(first.getWorld(), first.getPos());
                int firstBlockLight = LightmapTextureManager.getBlockLightCoordinates(firstLightColor);
                int firstSkyLight = LightmapTextureManager.getSkyLightCoordinates(firstLightColor);
                //noinspection ConstantConditions
                int secondLightColor = WorldRenderer.getLightmapCoordinates(second.getWorld(), second.getPos());
                int secondBlockLight = LightmapTextureManager.getBlockLightCoordinates(secondLightColor);
                int secondSkyLight = LightmapTextureManager.getSkyLightCoordinates(secondLightColor);
                return LightmapTextureManager.pack(Math.max(firstBlockLight, secondBlockLight), Math.max(firstSkyLight, secondSkyLight));
            };
        }

        @Override
        public Int2IntFunction get(ChestBlockEntity single) {
            return i -> i;
        }
    };

    private final ModelPart singleBottom, singleLid, singleLock;
    private final ModelPart leftBottom, leftLid, leftLock;
    private final ModelPart rightBottom, rightLid, rightLock;
    private final ModelPart topBottom, topLid, topLock;
    private final ModelPart bottomBottom;
    private final ModelPart frontBottom, frontLid, frontLock;
    private final ModelPart backBottom, backLid;

    public ChestBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        ModelPart single = context.getLayerModelPart(ChestBlockEntityRenderer.SINGLE_LAYER);
        singleBottom = single.getChild("bottom");
        singleLid = single.getChild("lid");
        singleLock = single.getChild("lock");
        ModelPart left = context.getLayerModelPart(ChestBlockEntityRenderer.LEFT_LAYER);
        leftBottom = left.getChild("bottom");
        leftLid = left.getChild("lid");
        leftLock = left.getChild("lock");
        ModelPart right = context.getLayerModelPart(ChestBlockEntityRenderer.RIGHT_LAYER);
        rightBottom = right.getChild("bottom");
        rightLid = right.getChild("lid");
        rightLock = right.getChild("lock");
        ModelPart top = context.getLayerModelPart(ChestBlockEntityRenderer.TOP_LAYER);
        topBottom = top.getChild("bottom");
        topLid = top.getChild("lid");
        topLock = top.getChild("lock");
        ModelPart bottom = context.getLayerModelPart(ChestBlockEntityRenderer.BOTTOM_LAYER);
        bottomBottom = bottom.getChild("bottom");
        ModelPart front = context.getLayerModelPart(ChestBlockEntityRenderer.FRONT_LAYER);
        frontBottom = front.getChild("bottom");
        frontLid = front.getChild("lid");
        frontLock = front.getChild("lock");
        ModelPart back = context.getLayerModelPart(ChestBlockEntityRenderer.BACK_LAYER);
        backBottom = back.getChild("bottom");
        backLid = back.getChild("lid");
    }

    public static TexturedModelData createSingleBodyLayer() {
        ModelData meshDefinition = new ModelData();
        ModelPartData partDefinition = meshDefinition.getRoot();
        partDefinition.addChild("bottom", ModelPartBuilder.create().uv(0, 19).cuboid(1, 0, 1, 14, 10, 14), ModelTransform.NONE);
        partDefinition.addChild("lid", ModelPartBuilder.create().uv(0, 0).cuboid(1, 0, 0, 14, 5, 14), ModelTransform.pivot(0, 9, 1));
        partDefinition.addChild("lock", ModelPartBuilder.create().uv(0, 0).cuboid(7, -1, 15, 2, 4, 1), ModelTransform.pivot(0, 8, 0));
        return TexturedModelData.of(meshDefinition, 64, 48);
    }

    public static TexturedModelData createLeftBodyLayer() {
        ModelData meshDefinition = new ModelData();
        ModelPartData partDefinition = meshDefinition.getRoot();
        partDefinition.addChild("bottom", ModelPartBuilder.create().uv(0, 19).cuboid(1, 0, 1, 15, 10, 14), ModelTransform.NONE);
        partDefinition.addChild("lid", ModelPartBuilder.create().uv(0, 0).cuboid(1, 0, 0, 15, 5, 14), ModelTransform.pivot(0, 9, 1));
        partDefinition.addChild("lock", ModelPartBuilder.create().uv(0, 0).cuboid(15, -1, 15, 1, 4, 1), ModelTransform.pivot(0, 8, 0));
        return TexturedModelData.of(meshDefinition, 64, 48);
    }

    public static TexturedModelData createRightBodyLayer() {
        ModelData meshDefinition = new ModelData();
        ModelPartData partDefinition = meshDefinition.getRoot();
        partDefinition.addChild("bottom", ModelPartBuilder.create().uv(0, 19).cuboid(0, 0, 1, 15, 10, 14), ModelTransform.NONE);
        partDefinition.addChild("lid", ModelPartBuilder.create().uv(0, 0).cuboid(0, 0, 0, 15, 5, 14), ModelTransform.pivot(0, 9, 1));
        partDefinition.addChild("lock", ModelPartBuilder.create().uv(0, 0).cuboid(0, -1, 15, 1, 4, 1), ModelTransform.pivot(0, 8, 0));
        return TexturedModelData.of(meshDefinition, 64, 48);
    }

    public static TexturedModelData createTopBodyLayer() {
        ModelData meshDefinition = new ModelData();
        ModelPartData partDefinition = meshDefinition.getRoot();
        partDefinition.addChild("bottom", ModelPartBuilder.create().uv(0, 19).cuboid(1, 0, 1, 14, 10, 14), ModelTransform.NONE);
        partDefinition.addChild("lid", ModelPartBuilder.create().uv(0, 0).cuboid(1, 0, 0, 14, 5, 14), ModelTransform.pivot(0, 9, 1));
        partDefinition.addChild("lock", ModelPartBuilder.create().uv(0, 0).cuboid(7, -1, 15, 2, 4, 1), ModelTransform.pivot(0, 8, 0));
        return TexturedModelData.of(meshDefinition, 64, 48);
    }

    public static TexturedModelData createBottomBodyLayer() {
        ModelData meshDefinition = new ModelData();
        ModelPartData partDefinition = meshDefinition.getRoot();
        partDefinition.addChild("bottom", ModelPartBuilder.create().uv(0, 0).cuboid(1, 0, 1, 14, 16, 14), ModelTransform.NONE);
        return TexturedModelData.of(meshDefinition, 64, 32);
    }

    public static TexturedModelData createFrontBodyLayer() {
        ModelData meshDefinition = new ModelData();
        ModelPartData partDefinition = meshDefinition.getRoot();
        partDefinition.addChild("bottom", ModelPartBuilder.create().uv(0, 20).cuboid(1, 0, 0, 14, 10, 15), ModelTransform.NONE);
        partDefinition.addChild("lid", ModelPartBuilder.create().uv(0, 0).cuboid(1, 0, 15, 14, 5, 15), ModelTransform.pivot(0, 9, -15));
        partDefinition.addChild("lock", ModelPartBuilder.create().uv(0, 0).cuboid(7, -1, 31, 2, 4, 1), ModelTransform.pivot(0, 8, -16));
        return TexturedModelData.of(meshDefinition, 64, 48);
    }

    public static TexturedModelData createBackBodyLayer() {
        ModelData meshDefinition = new ModelData();
        ModelPartData partDefinition = meshDefinition.getRoot();
        partDefinition.addChild("bottom", ModelPartBuilder.create().uv(0, 20).cuboid(1, 0, 1, 14, 10, 15), ModelTransform.NONE);
        partDefinition.addChild("lid", ModelPartBuilder.create().uv(0, 0).cuboid(1, 0, 0, 14, 5, 15), ModelTransform.pivot(0, 9, 1));
        return TexturedModelData.of(meshDefinition, 48, 48);
    }

    @Override
    public void render(ChestBlockEntity entity, float delta, MatrixStack stack, VertexConsumerProvider source, int light, int overlay) {
        Identifier blockId = entity.getBlockId();
        BlockState state = entity.hasWorld() ? entity.getCachedState() :
                ChestBlockEntityRenderer.DEFAULT_STATE.with(AbstractChestBlock.Y_ROTATION, FaceRotation.SOUTH);
        if (blockId == null || !(state.getBlock() instanceof ChestBlock block)) {
            return;
        }
        CursedChestType chestType = state.get(AbstractChestBlock.CURSED_CHEST_TYPE);
        stack.push();
        stack.translate(0.5D, 0.5D, 0.5D);
        stack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180 - state.get(AbstractChestBlock.Y_ROTATION).asRotationAngle()));
        stack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-state.get(AbstractChestBlock.PERP_ROTATION).asRotationAngle()));
        stack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(-state.get(AbstractChestBlock.FACE_ROTATION).asRotationAngle()));
        stack.translate(-0.5D, -0.5D, -0.5D);
        PropertyRetriever<ChestBlockEntity> retriever;
        if (entity.hasWorld()) {
            retriever = AbstractChestBlock.createPropertyRetriever(block, state, entity.getWorld(), entity.getPos(), true);
        } else {
            retriever = PropertyRetriever.createDirect(entity);
        }
        VertexConsumer consumer = new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, ChestApi.INSTANCE.getChestTexture(blockId, chestType)).getVertexConsumer(source, RenderLayer::getEntityCutout);
        float lidOpenness = ChestBlockEntityRenderer.getLidOpenness(retriever.get(ChestBlockEntityRenderer.LID_OPENNESS_FUNCTION_GETTER).get(delta));
        int brightness = retriever.get(ChestBlockEntityRenderer.BRIGHTNESS_PROPERTY).applyAsInt(light);
        if (chestType == CursedChestType.SINGLE) {
            ChestBlockEntityRenderer.renderBottom(stack, consumer, singleBottom, brightness, overlay);
            ChestBlockEntityRenderer.renderTop(stack, consumer, singleLid, brightness, overlay, lidOpenness);
            ChestBlockEntityRenderer.renderTop(stack, consumer, singleLock, brightness, overlay, lidOpenness);
        } else if (chestType == CursedChestType.TOP) {
            ChestBlockEntityRenderer.renderBottom(stack, consumer, topBottom, brightness, overlay);
            ChestBlockEntityRenderer.renderTop(stack, consumer, topLid, brightness, overlay, lidOpenness);
            ChestBlockEntityRenderer.renderTop(stack, consumer, topLock, brightness, overlay, lidOpenness);
        } else if (chestType == CursedChestType.BOTTOM) {
            ChestBlockEntityRenderer.renderBottom(stack, consumer, bottomBottom, brightness, overlay);
        } else if (chestType == CursedChestType.FRONT) {
            ChestBlockEntityRenderer.renderBottom(stack, consumer, frontBottom, brightness, overlay);
            ChestBlockEntityRenderer.renderTop(stack, consumer, frontLid, brightness, overlay, lidOpenness);
            ChestBlockEntityRenderer.renderTop(stack, consumer, frontLock, brightness, overlay, lidOpenness);
        } else if (chestType == CursedChestType.BACK) {
            ChestBlockEntityRenderer.renderBottom(stack, consumer, backBottom, brightness, overlay);
            ChestBlockEntityRenderer.renderTop(stack, consumer, backLid, brightness, overlay, lidOpenness);
        } else if (chestType == CursedChestType.LEFT) {
            ChestBlockEntityRenderer.renderBottom(stack, consumer, leftBottom, brightness, overlay);
            ChestBlockEntityRenderer.renderTop(stack, consumer, leftLid, brightness, overlay, lidOpenness);
            ChestBlockEntityRenderer.renderTop(stack, consumer, leftLock, brightness, overlay, lidOpenness);
        } else if (chestType == CursedChestType.RIGHT) {
            ChestBlockEntityRenderer.renderBottom(stack, consumer, rightBottom, brightness, overlay);
            ChestBlockEntityRenderer.renderTop(stack, consumer, rightLid, brightness, overlay, lidOpenness);
            ChestBlockEntityRenderer.renderTop(stack, consumer, rightLock, brightness, overlay, lidOpenness);
        }
        stack.pop();
    }

    private static float getLidOpenness(float delta) {
        delta = 1 - delta;
        delta = 1 - delta * delta * delta;
        return -delta * MathHelper.HALF_PI;
    }

    private static void renderBottom(MatrixStack stack, VertexConsumer consumer, ModelPart bottom, int brightness, int overlay) {
        bottom.render(stack, consumer, brightness, overlay);
    }

    private static void renderTop(MatrixStack stack, VertexConsumer consumer, ModelPart top, int brightness, int overlay, float openness) {
        top.pitch = openness;
        top.render(stack, consumer, brightness, overlay);
    }
}
