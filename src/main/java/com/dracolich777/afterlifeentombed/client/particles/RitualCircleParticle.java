// package com.dracolich777.afterlifeentombed.client.particles;

// import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
// import mod.chloeprime.aaaparticles.api.common.AAALevel;
// import net.minecraft.resources.ResourceLocation;
// import net.minecraft.client.particle.TextureSheetParticle;
// import net.minecraft.client.particle.SpriteSet;
// import net.minecraft.client.particle.ParticleRenderType;
// import net.minecraft.client.particle.ParticleProvider;
// import net.minecraft.client.particle.Particle;
// import net.minecraft.client.multiplayer.ClientLevel;
// import net.minecraft.core.particles.SimpleParticleType;
// import net.minecraftforge.api.distmarker.OnlyIn;
// import net.minecraftforge.api.distmarker.Dist;

// @OnlyIn(Dist.CLIENT)
// public class RitualCircleParticle extends TextureSheetParticle {

//     private static final ParticleEmitterInfo PARTICLES = new ParticleEmitterInfo(
//         new ResourceLocation("afterlifeentombed", "ritual_circle_disolve")
//     );

//     public static RitualCircleParticleProvider provider(SpriteSet spriteSet) {
//         return new RitualCircleParticleProvider(spriteSet);
//     }

//     public static class RitualCircleParticleProvider implements ParticleProvider<SimpleParticleType> {
//         private final SpriteSet spriteSet;

//         public RitualCircleParticleProvider(SpriteSet spriteSet) {
//             this.spriteSet = spriteSet;
//         }

//         @Override
//         public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
//             return new RitualCircleParticle(worldIn, x, y, z, this.spriteSet);
//         }
//     }

//     private final SpriteSet spriteSet;

//     protected RitualCircleParticle(ClientLevel world, double x, double y, double z, SpriteSet spriteSet) {
//         super(world, x, y, z);
//         this.spriteSet = spriteSet;
//         this.setSize(1.0f, 1.0f);
//         this.lifetime = 1; // Only needs to trigger once
//         this.gravity = 0f;
//         this.hasPhysics = false;
//         this.pickSprite(spriteSet);
//     }

//     @Override
//     public ParticleRenderType getRenderType() {
//         return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
//     }

//     @Override
//     public void tick() {
//         super.tick();

//         float yaw = 0f;
//         float pitch = 0f;
//         float roll = 0f;

//         AAALevel.addParticle(
//             this.level,
//             PARTICLES
//                 .position(this.x, this.y, this.z)
//                 .rotation(yaw, pitch, roll)
//         );

//         this.remove(); // remove after spawning the emitter
//     }
// }