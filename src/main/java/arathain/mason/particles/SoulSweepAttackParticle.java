package arathain.mason.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.world.ClientWorld;

@Environment(EnvType.CLIENT)
public class SoulSweepAttackParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;

    SoulSweepAttackParticle(ClientWorld world, double x, double y, double z, double scale, SpriteProvider spriteProvider) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.spriteProvider = spriteProvider;
        this.maxAge = 7; // Particle lifetime
        this.scale = 1.0F - (float)scale * 0.5F;
        this.setSprite(spriteProvider);
    }

    @Override
    public int getBrightness(float tint) {
        return 15728880; // Max brightness (full glow effect)
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (this.age++ >= this.maxAge) {
            this.markDead();
        } else {
            this.setSprite(this.spriteProvider);
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }
}
