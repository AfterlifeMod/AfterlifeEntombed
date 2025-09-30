# Enhanced Mirage Effect Documentation

## Overview
The Enhanced Mirage Effect creates realistic heat haze distortion and phantom structures that appear at the edges of vision without placing actual blocks.

## Features

### 1. Heat Haze Distortion
- **Swimming heat effects**: Visual distortion that creates a "swimming" effect on the horizon
- **Multi-layered distortion**: Multiple wave frequencies create complex, realistic heat shimmer
- **Horizon-specific**: Distortion appears primarily on the horizon for maximum realism
- **Distance-based**: Effects intensity varies with distance from the player

### 2. Phantom Structures
- **Edge-of-vision appearance**: Structures appear only at the periphery of the player's vision
- **No block placement**: These are purely visual effects that don't affect the world
- **Multiple structure types**:
  - **Desert Oasis**: Palm trees, water, and grass patches
  - **Ancient Ruins**: Crumbling sandstone pillars and structures
  - **Distant City**: Building silhouettes that appear on the horizon

### 3. Visual Effects
- **Ethereal glow**: Phantom structures have a blue-white ethereal glow
- **Transparency**: Phantom structures are more translucent than regular mirages
- **Pulsing wisps**: Concentric rings of energy around phantom structures
- **Fade on direct look**: Both effects fade when looked at directly

## Usage

### Applying the Effect
```java
// Apply mirage effect to a player
player.addEffect(new MobEffectInstance(ModEffects.MIRAGE.get(), duration, amplifier));
```

### Effect Amplifiers
- **Amplifier 0**: Works in any biome, shows precious ores and phantom structures
- **Amplifier 1**: Desert-only mode, shows desert-specific blocks and structures

### Configuration
The effect can be customized by modifying these constants in `ClientMirageHandler`:
- `HAZE_INTENSITY`: Controls the intensity of heat distortion
- `HAZE_SPEED`: Controls animation speed of the heat effects
- `HORIZON_DISTORTION_DISTANCE`: Distance at which horizon distortion appears
- `MAX_DISTORTION_HEIGHT`: Maximum height of distortion effects

## Technical Implementation

### Network Communication
The effect uses a custom network packet system to send mirage data from server to client:
- `MirageDataPacket`: Handles sending mirage block and structure data
- Support for phantom structure metadata

### Client-Side Rendering
- Custom `MirageBlock` class tracks individual mirage elements
- Separate rendering for phantom structures vs. regular mirage blocks
- Advanced alpha blending for translucent effects

### Structure Generation
- Template-based phantom structure system
- Configurable structure types with relative positioning
- Ground-finding algorithm for proper placement

## Adding New Phantom Structures

To add new phantom structures, modify the `initializePhantomStructures()` method in `MirageEffect.java`:

```java
// Example: Add a new phantom structure
List<BlockPos> positions = new ArrayList<>();
List<BlockState> blocks = new ArrayList<>();

// Define your structure layout
positions.add(new BlockPos(0, 0, 0));
blocks.add(Blocks.STONE.defaultBlockState());

PHANTOM_STRUCTURES.add(new PhantomStructure("my_structure", positions, blocks, width, height, depth));
```

## Performance Considerations
- Phantom structures are only rendered when the player has the mirage effect
- Structures fade when looked at directly to maintain the illusion
- Limited number of concurrent mirage blocks to prevent performance issues
- Distance-based culling for optimal rendering performance