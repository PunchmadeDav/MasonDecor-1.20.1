package arathain.mason.client;

import arathain.mason.entity.GildedmouldEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class GildedmouldEntityModel extends DefaultedEntityGeoModel<GildedmouldEntity> {
    public GildedmouldEntityModel() {
        super(new Identifier("mason", "gildedmould"), true);
        this.withAltTexture(new Identifier("mason", "textures/entity/mould/gildedmould.png"));
        // If needed, set animation file using a suitable method
    }

    @Override
    public void setCustomAnimations(GildedmouldEntity animatable, long instanceId, AnimationState<GildedmouldEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        CoreGeoBone head = this.getAnimationProcessor().getBone("head");
        EntityModelData extraData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        if (head != null) {
            head.setRotX(extraData.headPitch() * ((float) Math.PI / 180F));
            head.setRotY(extraData.netHeadYaw() * ((float) Math.PI / 180F));
        }
    }
}
