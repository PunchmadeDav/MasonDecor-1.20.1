package arathain.mason.client;

import arathain.mason.entity.GildedmouldEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GildedmouldEntityRenderer extends GeoEntityRenderer<GildedmouldEntity> {
    public GildedmouldEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new GildedmouldEntityModel());
        this.addRenderLayer(new GildedmouldEyesLayer(this));
    }

    @Override
    public Identifier getTexture(GildedmouldEntity entity) {
        return new Identifier("mason", "textures/entity/mould/gildedmould.png");
    }

    public RenderLayer getRenderType(GildedmouldEntity animatable, Identifier texture, VertexConsumerProvider bufferSource, float partialTick) {
        return RenderLayer.getEntityTranslucent(texture, true);
    }
}
