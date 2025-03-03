package arathain.mason.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

@Environment(EnvType.CLIENT)
public class SoulSweepAttackParticle$Factory implements ParticleFactory<DefaultParticleType> {
    private final SpriteProvider spriteProvider;

    public SoulSweepAttackParticle$Factory(SpriteProvider spriteProvider) {
        this.spriteProvider = spriteProvider;
    }

    @Override
    public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        return new SoulSweepAttackParticle(clientWorld, x, y, z, velocityX, this.spriteProvider);
    }
}
