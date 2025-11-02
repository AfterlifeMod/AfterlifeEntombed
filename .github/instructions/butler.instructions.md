---
applyTo: '**'
---
Provide project context and coding guidelines that AI should follow when generating code, answering questions, or reviewing changes.

# Responses
- should be in the voice of a british butler
- should be formal, polite, and professional
- should not include emojis

# Terminal usage
- Use powershell commands as default
- always use ./gradlew for gradle commands
- never use .\gradlew.bat

# This Project
- Minecraft mod using Forge for 1.20.1
- When making new god avatars, you must make them functional in the exact same way as Seth and Ra.
- Each god should have [active1]: origins primary key, [active2]: origins secondary key, and [active3]: loadtoolbaractivator, and [active4]: savetoolbaractivator.
- Each gods ultimate should look exactly the same as the others. Creative flight, instant build, absorbtion 255, resistance 7, haste 7, regeneration 255, strength 255, speed 7, no cooldowns on other active abilities. this form should last for a minute, then go on a 5 minute cooldown. when it goes on cooldown, it should grant slow falling for a minute.

# List of boons and curses to be implemented, per god
 - Ra:
   - Boons:
     - Immune to fire, can walk on lava.
     - Attacks made by you give holy fire effect to target, and they are pushed out of water for the duration of the effect.
     - You emit ambient light of 15.
     - while it is day time, you have creative flight.
     - While it is daytime you have instant build and instant mine.
     - while it is day, your tools, weapons, and armor do not lose durability.
     - You cannot be set on fire.
     - Entities that attack you are set on fire for 10 seconds.
     - Entities within a 5 block radius of you are set on fire for 3 seconds.
   - Curses:
     - Take damage over tiem while in sunlight.(make this be a custom damage type that bypasses armor and enchantments, with a cool custom death message.)
     - Water sources evaporate around you in a 5 block radius.
     - While it is day time, you cannot jump, sprint, crouch, swim, or fly with elytra.
     - You are set on fire whenever you hit another entity.
     - You are set on fire when eating golden apples, when eating golden carrots, and when drinking potions.
     - Lava sources ever so slightly pull you towards them.
     - You take extra damage from fire and lava.
     - Takign fire damage deals double damage to your armors durability.
     - if you were on fire and then extinguished it with water, you get set on fire again as soon as you leave the water.
 - Seth:
    - Boons:
      - Immune to wither, poison, blindness, weakness, slowness, hunger, nausea, and mining fatigue.
      - Attacks made by you give wither effect to target, and they are unable to eat food for the duration of the effect.
      - you emit darkness/supress light sources in a 5 block radius around you.
      - While in darkness (light level 7 and below) or during night time, you have creative flight.
      - While in darkness (light level 7 and below) or during night time, you have instant build and instant mine.
      - While in darkness (light level 7 and below) or during night time, your tools, weapons, and armor do not lose durability.
      - you are immune to explosions.
      - you have a 50% chance not to take damage from a given attack.
      - you have a 25% chance to not die when killed, instead being left with half a heart.
      - Entities that attack you are given wither for 10 seconds.
      - Entities within a 5 block radius of you are given wither for 3 seconds.
      - while you are invisible, youo deal double damage with attacks.
      - When you get hit,  the attacker takes damage equal to half the damage they dealt to you.
    - Curses:
      - Take damage over time while in darkness (light level 7 and below).(make this be a custom damage type that bypasses armor and enchantments, with a cool custom death message.)
      - You cannot eat food as long as it is night time or below light level 7.
      - While in darkness (light level 7 and below) or during night time, you cannot jump, sprint, crouch, swim, or fly with elytra.
      - You are given wither whenever you hit another entity.
      - You are given wither when eating food, and when drinking potions.
      - Explosions ever so slightly pull you towards them.
      - You recieve the wither effect for twice as long as you should.
      - While withering, your armor also takes durability damage.
      - You cannot clear negative effects with milk.
    - Horus:
        - Boons:
          - You are immune to projectile damage, and the levitation effect.
          - Attacks made by you reduce the targets armor value temporarily for 5 seconds.
          - Whenever you take damage, increase your own armor value temporarily for 5 seconds.
          - You can see invisible entities.
          - You take no damage from attacks that come from above you.
          - You take no fall damage.
          - You have increased entity reach by 20 blocks.
          - when you hit an entity with an attack and that entity is at least 15 blocks away, they take double damage.
          - You are immune to blindness,darkness, slowness, and nausea.
          - Your projectiles home towards targets.
        - Curses:
          - You take double damage from projectile attacks.
          - Attacks made by you reduce your own armor value temporarily for 5 seconds.
          - Whenever you take damage, reduce your own armor value temporarily for 5 seconds.
          - whenever you take damage you get blindness for 5 seconds.
          - you take extra fall damage.
          - Your entity reach is reduced by 3 blocks.
          - When you attack something you take damage equal to half the damage you dealt.