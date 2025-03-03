package arathain.mason.init;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModParticles {
    public static final DefaultParticleType SOUL_SWEEP_ATTACK = FabricParticleTypes.simple();

    public ModParticles() {
    }

    public static void registerParticles() {
        Registry.register(Registries.PARTICLE_TYPE, new Identifier("mason", "soul_sweep"), SOUL_SWEEP_ATTACK);
    }
}
