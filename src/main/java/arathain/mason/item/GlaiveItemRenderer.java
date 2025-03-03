package arathain.mason.item;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleSynchronousResourceReloader;

public class GlaiveItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer, SimpleSynchronousResourceReloader {
    private final Identifier id;
    private final Identifier glaiveId;
    private ItemRenderer itemRenderer;
    private BakedModel inventoryGlaiveModel;
    public BakedModel worldGlaiveModel;

    public GlaiveItemRenderer(Identifier tridentId) {
        this.id = new Identifier(tridentId.getNamespace(), tridentId.getPath() + "_renderer");
        this.glaiveId = tridentId;
    }

public Identifier getQuiltId() {
        return this.id;
    }

    public void reload(ResourceManager manager) {
        MinecraftClient mc = MinecraftClient.getInstance();
        this.itemRenderer = mc.getItemRenderer();
        this.inventoryGlaiveModel = mc.getBakedModelManager().getModel(new ModelIdentifier(this.glaiveId.getNamespace(), this.glaiveId.getPath() + "_gui", "inventory"));
        this.worldGlaiveModel = mc.getBakedModelManager().getModel(new ModelIdentifier(this.glaiveId.getNamespace(), this.glaiveId.getPath() + "_handheld", "inventory"));
    }

    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (mode == ModelTransformationMode.GUI) {
            matrices.pop();
            matrices.push();
            this.itemRenderer.renderItem(stack, mode, false, matrices, vertexConsumers, light, overlay, this.inventoryGlaiveModel);
        } else {
            matrices.pop();
            matrices.push();
            boolean leftHanded;
            switch (mode) {
                case FIRST_PERSON_LEFT_HAND:
                case THIRD_PERSON_LEFT_HAND:
                    leftHanded = true;
                    break;
                default:
                    leftHanded = false;
            }
            this.itemRenderer.renderItem(stack, mode == ModelTransformationMode.FIXED ? ModelTransformationMode.FIXED : mode, leftHanded, matrices, vertexConsumers, light, overlay, this.worldGlaiveModel);
        }
    }
}
