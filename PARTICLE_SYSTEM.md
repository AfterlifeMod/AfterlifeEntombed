# Afterlife: Entombed - Streamlined Particle System

## Overview
The particle system has been completely streamlined to be programmatic instead of manual, with support for in-game commands to spawn particles with position and scale parameters.

## Components

### 1. ParticleManager (`util/ParticleManager.java`)
- **Central registry** for all AAA particles in the mod
- **Automatic registration** of particles with logical names
- **Helper methods** for spawning particles with various parameters
- **Error handling** and validation

#### Available Particles:
- `seth_dissolve` - Seth's crown dissolve effect
- `seth_appear` - Seth's crown appear effect  
- `ra_ring` - Ra's ring following effect

#### Key Methods:
```java
// Spawn with default scale (1.0, 1.0, 1.0)
ParticleManager.spawnParticle(level, "seth_dissolve", x, y, z)

// Spawn with custom uniform scale
ParticleManager.spawnParticle(level, "ra_ring", x, y, z, 2.0f, 2.0f, 2.0f)

// Spawn with full customization (scale + rotation)
ParticleManager.spawnParticle(level, "seth_appear", x, y, z, 1.5f, 1.5f, 1.5f, 45.0f, 0.0f, 90.0f)
```

### 2. Enhanced ParticleEffectPacket (`network/ParticleEffectPacket.java`)
- **Backward compatible** with existing constructors
- **New support** for scale parameters (scaleX, scaleY, scaleZ)
- **Automatic handling** of both old and new packet formats

#### Constructors:
```java
// Legacy constructor (scale defaults to 1.0)
new ParticleEffectPacket(x, y, z, "seth_dissolve")

// New constructor with scale support
new ParticleEffectPacket(x, y, z, "ra_ring", 2.0f, 1.0f, 2.0f)
```

### 3. Particle Command (`commands/ParticleCommand.java`)
- **In-game command** for spawning particles
- **Auto-completion** for particle names
- **Flexible syntax** with optional parameters
- **Permission level 2** required (OP)

#### Command Syntax:
```
/afterlife_particle <particle_name> [position] [scale] [scaleY] [scaleZ]
```

#### Usage Examples:
```
# Spawn at player position with default scale
/afterlife_particle seth_dissolve

# Spawn at specific position with default scale
/afterlife_particle ra_ring ~ ~1 ~

# Spawn with uniform scale
/afterlife_particle seth_appear ~ ~2 ~ 2.0

# Spawn with individual X, Y, Z scales
/afterlife_particle ra_ring ~ ~1 ~ 1.5 2.0 1.5
```

### 4. Command Registration (`events/CommandEvents.java`)
- **Automatic registration** of the particle command
- **Event-based system** using Forge's RegisterCommandsEvent

## Benefits

### For Developers:
1. **Easy to add new particles** - just register them in ParticleManager
2. **Consistent API** - all particle spawning goes through the same system
3. **Built-in error handling** - invalid particles are caught and logged
4. **Flexible scaling** - per-axis scale control for all particles

### For Users:
1. **In-game particle testing** - no need to trigger specific game events
2. **Creative possibilities** - spawn particles anywhere with custom scales  
3. **Auto-completion** - command suggests available particle names
4. **Position flexibility** - use coordinates or relative positioning

### For Maintainability:
1. **Centralized management** - all particles in one place
2. **No more hardcoded checks** - the old if/else chain is gone
3. **Scalable system** - easy to add more particles without code changes
4. **Backward compatible** - existing particle usage continues to work

## Migration from Old System

The old manual system with hardcoded particle checks has been replaced, but existing code continues to work:

**Old way (still works):**
```java
ParticleEffectPacket packet = new ParticleEffectPacket(x, y, z, "seth_dissolve");
```

**New way (recommended):**
```java
ParticleManager.spawnParticle(level, "seth_dissolve", x, y, z, scaleX, scaleY, scaleZ);
```

## Adding New Particles

To add a new particle:

1. **Register in ParticleManager:**
```java
registerParticle("new_particle_name", "resource_file_name");
```

2. **That's it!** The particle is now available:
   - In the command system (auto-completion included)
   - Through the ParticleManager API
   - Via network packets

No need to modify packet handling, command logic, or add hardcoded checks.