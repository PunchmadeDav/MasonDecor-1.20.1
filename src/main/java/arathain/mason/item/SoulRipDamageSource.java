package arathain.mason.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Holder;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class SoulRipDamageSource extends DamageSource {
    private final Entity source;

    public static DamageSource playerRip(PlayerEntity attacker) {
        Holder<DamageType> damageType = attacker.getWorld().getRegistryManager()
                .get(RegistryKeys.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.MAGIC);
        return new SoulRipDamageSource(damageType, attacker);
    }

    public SoulRipDamageSource(Holder<DamageType> type, Entity source) {
        super(type);
        this.source = source;
    }

    @Override
    public Entity getSource() {
        return this.source;
    }

    @Override
    public boolean isScaledWithDifficulty() {
        return this.source instanceof LivingEntity && !(this.source instanceof PlayerEntity);
    }

    @Override
    @Nullable
    public Vec3d getPosition() {
        return this.source.getPos();
    }

    @Override
    public String toString() {
        return "SoulRipDamageSource (" + this.source + ")";
    }
}
