package com.dracolich777.afterlifeentombed.boons;

import com.dracolich777.afterlifeentombed.items.GodType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;

/**
 * Represents the different types of boons and curses that can be granted by the gods.
 * Each god has 10 possible blessings and 10 possible curses.
 */
public enum BoonType {
    // ===== HORUS - God of War, Sky, and Protection =====
    // Blessings (10 boons from instructions)
    HORUS_PROJECTILE_IMMUNITY("Projectile Immunity", GodType.HORUS, true, BoonDuration.PERMANENT, Items.SHIELD,
        "Immune to projectile damage and levitation effect"),
    HORUS_ARMOR_BREAKER("Armor Breaker", GodType.HORUS, true, BoonDuration.PERMANENT, Items.IRON_CHESTPLATE,
        "Attacks reduce target's armor value temporarily for 5 seconds"),
    HORUS_FORTIFYING_STRIKES("Fortifying Strikes", GodType.HORUS, true, BoonDuration.PERMANENT, Items.GOLDEN_CHESTPLATE,
        "Taking damage increases your armor value temporarily for 5 seconds"),
    HORUS_EAGLES_EYE("Eagle's Eye", GodType.HORUS, true, BoonDuration.PERMANENT, Items.SPYGLASS,
        "Can see invisible entities"),
    HORUS_SKY_PROTECTION("Sky Protection", GodType.HORUS, true, BoonDuration.PERMANENT, Items.ELYTRA,
        "Take no damage from attacks that come from above you"),
    HORUS_FEATHER_FALL("Feather Fall", GodType.HORUS, true, BoonDuration.PERMANENT, Items.FEATHER,
        "Take no fall damage"),
    HORUS_EXTENDED_REACH("Extended Reach", GodType.HORUS, true, BoonDuration.PERMANENT, Items.FISHING_ROD,
        "Entity reach increased by 20 blocks"),
    HORUS_SNIPER("Sniper", GodType.HORUS, true, BoonDuration.PERMANENT, Items.BOW,
        "Entities 15+ blocks away take double damage from your attacks"),
    HORUS_DIVINE_FAVOR("Divine Favor", GodType.HORUS, true, BoonDuration.PERMANENT, Items.NETHER_STAR,
        "Immune to blindness, darkness, slowness, and nausea"),
    HORUS_HOMING_PROJECTILES("Homing Projectiles", GodType.HORUS, true, BoonDuration.PERMANENT, Items.ARROW,
        "Your projectiles home towards targets"),
    
    // Curses (7 curses from instructions)
    HORUS_PROJECTILE_WEAKNESS("Projectile Weakness", GodType.HORUS, false, BoonDuration.PERMANENT, Items.SPECTRAL_ARROW,
        "Take double damage from projectile attacks"),
    HORUS_ARMOR_DECAY("Armor Decay", GodType.HORUS, false, BoonDuration.PERMANENT, Items.LEATHER_CHESTPLATE,
        "Attacks reduce your own armor value temporarily for 5 seconds"),
    HORUS_DAMAGE_VULNERABILITY("Damage Vulnerability", GodType.HORUS, false, BoonDuration.PERMANENT, Items.DAMAGED_ANVIL,
        "Taking damage reduces your armor value temporarily for 5 seconds"),
    HORUS_BLURRED_VISION("Blurred Vision", GodType.HORUS, false, BoonDuration.PERMANENT, Items.FERMENTED_SPIDER_EYE,
        "Taking damage gives you blindness for 5 seconds"),
    HORUS_HEAVY_BURDEN("Heavy Burden", GodType.HORUS, false, BoonDuration.PERMANENT, Items.ANVIL,
        "Take extra fall damage"),
    HORUS_SHORT_REACH("Short Reach", GodType.HORUS, false, BoonDuration.PERMANENT, Items.STICK,
        "Entity reach reduced by 3 blocks"),
    HORUS_SELF_HARM("Self Harm", GodType.HORUS, false, BoonDuration.PERMANENT, Items.WOODEN_SWORD,
        "When you attack something, you take damage equal to half the damage you dealt"),

    // ===== RA - God of the Sun and Light =====
    // Blessings (9 boons from instructions)
    RA_FIRE_IMMUNITY("Fire Immunity", GodType.RA, true, BoonDuration.PERMANENT, Items.FIRE_CHARGE,
        "Immune to fire, can walk on lava, cannot be set on fire"),
    RA_HOLY_FIRE("Holy Fire", GodType.RA, true, BoonDuration.PERMANENT, Items.BLAZE_ROD,
        "Attacks give holy fire effect to target, pushing them out of water"),
    RA_LIGHT_BEARER("Light Bearer", GodType.RA, true, BoonDuration.PERMANENT, Items.GLOWSTONE,
        "Emit ambient light of level 15"),
    RA_SOLAR_FLIGHT("Solar Flight", GodType.RA, true, BoonDuration.PERMANENT, Items.ELYTRA,
        "Creative flight during daytime"),
    RA_SOLAR_EFFICIENCY("Solar Efficiency", GodType.RA, true, BoonDuration.PERMANENT, Items.GOLDEN_PICKAXE,
        "Instant build and instant mine during daytime"),
    RA_ETERNAL_EQUIPMENT("Eternal Equipment", GodType.RA, true, BoonDuration.PERMANENT, Items.DIAMOND_CHESTPLATE,
        "Tools, weapons, and armor don't lose durability during daytime"),
    RA_FIRE_AURA("Fire Aura", GodType.RA, true, BoonDuration.PERMANENT, Items.FIRE_CHARGE,
        "Entities that attack you are set on fire for 10 seconds"),
    RA_SOLAR_PRESENCE("Solar Presence", GodType.RA, true, BoonDuration.PERMANENT, Items.TORCH,
        "Entities within 5 block radius are set on fire for 3 seconds"),
    RA_BURNING_RETRIBUTION("Burning Retribution", GodType.RA, true, BoonDuration.PERMANENT, Items.LAVA_BUCKET,
        "Entities attacking you are set on fire"),
    
    // Curses (9 curses from instructions)
    RA_SUNS_SCORN("Sun's Scorn", GodType.RA, false, BoonDuration.PERMANENT, Items.MAGMA_CREAM,
        "Take damage over time while in sunlight"),
    RA_DESERT_THIRST("Desert Thirst", GodType.RA, false, BoonDuration.PERMANENT, Items.GLASS_BOTTLE,
        "Water sources evaporate around you in a 5 block radius"),
    RA_SOLAR_PARALYSIS("Solar Paralysis", GodType.RA, false, BoonDuration.PERMANENT, Items.COBWEB,
        "Cannot jump, sprint, crouch, swim, or fly with elytra during daytime"),
    RA_SELF_IMMOLATION("Self-Immolation", GodType.RA, false, BoonDuration.PERMANENT, Items.FLINT_AND_STEEL,
        "Set on fire when hitting entities, eating golden foods, or drinking potions"),
    RA_LAVA_MAGNETISM("Lava Magnetism", GodType.RA, false, BoonDuration.PERMANENT, Items.LAVA_BUCKET,
        "Lava sources pull you towards them"),
    RA_FIRE_VULNERABILITY("Fire Vulnerability", GodType.RA, false, BoonDuration.PERMANENT, Items.SOUL_CAMPFIRE,
        "Take extra damage from fire and lava"),
    RA_SCORCHING_ARMOR("Scorching Armor", GodType.RA, false, BoonDuration.PERMANENT, Items.LEATHER_CHESTPLATE,
        "Fire damage deals double damage to your armor's durability"),
    RA_ETERNAL_FLAME("Eternal Flame", GodType.RA, false, BoonDuration.PERMANENT, Items.CAMPFIRE,
        "If extinguished by water, re-ignite when leaving water"),
    RA_EXHAUSTED("Exhausted", GodType.RA, false, BoonDuration.TEMPORARY, Items.DEAD_BUSH,
        "Mining Fatigue III for 5 minutes"),

    // ===== THOTH - God of Knowledge and Wisdom =====
    // Blessings
    THOTH_SCHOLARS_GIFT("Scholar's Gift", GodType.THOTH, true, BoonDuration.ONE_USE, Items.EXPERIENCE_BOTTLE,
        "Instantly gain 10 levels"),
    THOTH_QUICK_LEARNER("Quick Learner", GodType.THOTH, true, BoonDuration.TEMPORARY, Items.BOOK,
        "Gain 2x XP for 30 minutes"),
    THOTH_ENCHANTED_MIND("Enchanted Mind", GodType.THOTH, true, BoonDuration.ONE_USE, Items.ENCHANTING_TABLE,
        "Next enchantment costs 50% less levels"),
    THOTH_LIBRARY_RECALL("Library Recall", GodType.THOTH, true, BoonDuration.ONE_USE, Items.ENDER_PEARL,
        "Teleport to the nearest village"),
    THOTH_WISDOM_KEEPER("Wisdom Keeper", GodType.THOTH, true, BoonDuration.PERMANENT, Items.BOOKSHELF,
        "Enchantments are always slightly better"),
    THOTH_SCRIBES_FORTUNE("Scribe's Fortune", GodType.THOTH, true, BoonDuration.TEMPORARY, Items.WRITABLE_BOOK,
        "Fortune III for 10 minutes"),
    THOTH_MENTAL_CLARITY("Mental Clarity", GodType.THOTH, true, BoonDuration.TEMPORARY, Items.GLASS_BOTTLE,
        "All cooldowns recharge 25% faster for 15 minutes"),
    THOTH_ANCIENT_KNOWLEDGE("Ancient Knowledge", GodType.THOTH, true, BoonDuration.ONE_USE, Items.LECTERN,
        "Reveal the location of the nearest structure"),
    THOTH_PRESERVED_WISDOM("Preserved Wisdom", GodType.THOTH, true, BoonDuration.ONE_USE, Items.ENCHANTED_BOOK,
        "Keep all XP on next death"),
    THOTH_INFINITE_PAGES("Infinite Pages", GodType.THOTH, true, BoonDuration.PERMANENT, Items.KNOWLEDGE_BOOK,
        "Gain +1 maximum enchantment level on tools"),
    
    // Curses
    THOTH_FORGETFULNESS("Forgetfulness", GodType.THOTH, false, BoonDuration.TEMPORARY, Items.PAPER,
        "Lose 2 levels every minute for 5 minutes"),
    THOTH_CONFUSED_MIND("Confused Mind", GodType.THOTH, false, BoonDuration.TEMPORARY, Items.POISONOUS_POTATO,
        "XP gain reduced by 75% for 20 minutes"),
    THOTH_COSTLY_KNOWLEDGE("Costly Knowledge", GodType.THOTH, false, BoonDuration.ONE_USE, Items.DAMAGED_ANVIL,
        "Next enchantment costs 2x levels"),
    THOTH_LOST_PAGES("Lost Pages", GodType.THOTH, false, BoonDuration.ONE_USE, Items.WRITTEN_BOOK,
        "Lose 5 levels immediately"),
    THOTH_MENTAL_FOG("Mental Fog", GodType.THOTH, false, BoonDuration.TEMPORARY, Items.GRAY_WOOL,
        "All cooldowns take 50% longer for 10 minutes"),
    THOTH_CURSED_ENCHANTMENT("Cursed Enchantment", GodType.THOTH, false, BoonDuration.ONE_USE, Items.SOUL_SAND,
        "Next enchanted item gains a curse"),
    THOTH_MEMORY_DRAIN("Memory Drain", GodType.THOTH, false, BoonDuration.TEMPORARY, Items.COBWEB,
        "Cannot gain XP for 15 minutes"),
    THOTH_BROKEN_QUILL("Broken Quill", GodType.THOTH, false, BoonDuration.TEMPORARY, Items.STICK,
        "Cannot use enchanting table for 30 minutes"),
    THOTH_SCATTERED_THOUGHTS("Scattered Thoughts", GodType.THOTH, false, BoonDuration.ONE_USE, Items.DROPPER,
        "Random inventory items will be dropped on next hit"),
    THOTH_IGNORANCE("Ignorance", GodType.THOTH, false, BoonDuration.PERMANENT, Items.BARRIER,
        "Permanent slight reduction to XP gain"),

    // ===== SHU - God of Air and Wind =====
    // Blessings
    SHU_WIND_WALKER("Wind Walker", GodType.SHU, true, BoonDuration.TEMPORARY, Items.ELYTRA,
        "Slow Falling for 10 minutes"),
    SHU_GALE_FORCE("Gale Force", GodType.SHU, true, BoonDuration.TEMPORARY, Items.FEATHER,
        "Speed III for 5 minutes"),
    SHU_UPDRAFT("Updraft", GodType.SHU, true, BoonDuration.ONE_USE, Items.PHANTOM_MEMBRANE,
        "Next jump launches you very high"),
    SHU_FEATHER_FALL("Feather Fall", GodType.SHU, true, BoonDuration.PERMANENT, Items.FEATHER,
        "Permanent fall damage reduction"),
    SHU_SWIFT_BREEZE("Swift Breeze", GodType.SHU, true, BoonDuration.TEMPORARY, Items.SUGAR,
        "Jump Boost II for 15 minutes"),
    SHU_CLOUD_STEP("Cloud Step", GodType.SHU, true, BoonDuration.TEMPORARY, Items.WHITE_WOOL,
        "Walk on air for 3 seconds (one time)"),
    SHU_CYCLONE("Cyclone", GodType.SHU, true, BoonDuration.ONE_USE, Items.TRIDENT,
        "Launch all nearby entities away"),
    SHU_BREATH_OF_LIFE("Breath of Life", GodType.SHU, true, BoonDuration.TEMPORARY, Items.GLASS_BOTTLE,
        "Water breathing for 20 minutes"),
    SHU_EAGLES_GIFT("Eagle's Gift", GodType.SHU, true, BoonDuration.ONE_USE, Items.ELYTRA,
        "Instantly repair your elytra"),
    SHU_SKYBOUND("Skybound", GodType.SHU, true, BoonDuration.PERMANENT, Items.FIREWORK_ROCKET,
        "Permanent slight increase to jump height"),
    
    // Curses
    SHU_HEAVY_AIR("Heavy Air", GodType.SHU, false, BoonDuration.TEMPORARY, Items.LEAD,
        "Jump Boost -2 for 10 minutes"),
    SHU_GROUNDED("Grounded", GodType.SHU, false, BoonDuration.TEMPORARY, Items.CHAIN,
        "Cannot fly with elytra for 15 minutes"),
    SHU_SUFFOCATING("Suffocating", GodType.SHU, false, BoonDuration.TEMPORARY, Items.COBWEB,
        "Constant damage as if drowning for 2 minutes"),
    SHU_CRUSHING_WEIGHT("Crushing Weight", GodType.SHU, false, BoonDuration.TEMPORARY, Items.IRON_BLOCK,
        "Take increased fall damage for 20 minutes"),
    SHU_WIND_BURN("Wind Burn", GodType.SHU, false, BoonDuration.TEMPORARY, Items.CACTUS,
        "Take damage when moving fast for 10 minutes"),
    SHU_STILL_AIR("Still Air", GodType.SHU, false, BoonDuration.TEMPORARY, Items.BARRIER,
        "Slowness III for 5 minutes"),
    SHU_VERTIGO("Vertigo", GodType.SHU, false, BoonDuration.TEMPORARY, Items.FERMENTED_SPIDER_EYE,
        "Nausea II when airborne for 15 minutes"),
    SHU_DOWNDRAFT("Downdraft", GodType.SHU, false, BoonDuration.ONE_USE, Items.ANVIL,
        "Next jump slams you down instead"),
    SHU_BREATHLESS("Breathless", GodType.SHU, false, BoonDuration.TEMPORARY, Items.PUFFERFISH,
        "Reduced oxygen underwater for 30 minutes"),
    SHU_EARTHBOUND("Earthbound", GodType.SHU, false, BoonDuration.PERMANENT, Items.BEDROCK,
        "Permanent slight reduction to movement speed"),

    // ===== GEB - God of Earth =====
    // Blessings
    GEB_STONE_SKIN("Stone Skin", GodType.GEB, true, BoonDuration.TEMPORARY, Items.STONE,
        "Resistance II for 15 minutes"),
    GEB_EARTHS_EMBRACE("Earth's Embrace", GodType.GEB, true, BoonDuration.TEMPORARY, Items.DIRT,
        "Regeneration I for 10 minutes"),
    GEB_MINERS_LUCK("Miner's Luck", GodType.GEB, true, BoonDuration.TEMPORARY, Items.DIAMOND_PICKAXE,
        "Fortune II for 20 minutes"),
    GEB_UNSHAKEABLE("Unshakeable", GodType.GEB, true, BoonDuration.TEMPORARY, Items.OBSIDIAN,
        "Knockback resistance for 10 minutes"),
    GEB_CRYSTAL_CLARITY("Crystal Clarity", GodType.GEB, true, BoonDuration.ONE_USE, Items.DIAMOND,
        "Next mined ore drops 3x items"),
    GEB_MOUNTAIN_STRENGTH("Mountain Strength", GodType.GEB, true, BoonDuration.TEMPORARY, Items.IRON_INGOT,
        "Strength II for 10 minutes"),
    GEB_FERTILE_GROUND("Fertile Ground", GodType.GEB, true, BoonDuration.ONE_USE, Items.BONE_MEAL,
        "Next 32 bone meals work at 2x speed"),
    GEB_GEOMANCERS_WARD("Geomancer's Ward", GodType.GEB, true, BoonDuration.TEMPORARY, Items.BEDROCK,
        "Take no damage from falling for 30 minutes"),
    GEB_EARTHEN_BLESSING("Earthen Blessing", GodType.GEB, true, BoonDuration.ONE_USE, Items.EMERALD,
        "Find rare ore in the next 64 blocks mined"),
    GEB_ETERNAL_STONE("Eternal Stone", GodType.GEB, true, BoonDuration.PERMANENT, Items.REINFORCED_DEEPSLATE,
        "Permanent slight mining speed bonus"),
    
    // Curses
    GEB_BRITTLE_BONES("Brittle Bones", GodType.GEB, false, BoonDuration.TEMPORARY, Items.BONE,
        "Take 50% more fall damage for 15 minutes"),
    GEB_BURIED("Buried", GodType.GEB, false, BoonDuration.TEMPORARY, Items.GRAVEL,
        "Suffocation damage when underground for 10 minutes"),
    GEB_CRUMBLING("Crumbling", GodType.GEB, false, BoonDuration.TEMPORARY, Items.SAND,
        "Weakness II for 10 minutes"),
    GEB_PETRIFIED("Petrified", GodType.GEB, false, BoonDuration.TEMPORARY, Items.COBBLESTONE,
        "Slowness II and Mining Fatigue II for 5 minutes"),
    GEB_EARTHEN_CURSE("Earthen Curse", GodType.GEB, false, BoonDuration.TEMPORARY, Items.COARSE_DIRT,
        "Cannot jump for 8 minutes"),
    GEB_CORRODED_TOOLS("Corroded Tools", GodType.GEB, false, BoonDuration.TEMPORARY, Items.FLINT,
        "Tools lose durability 3x faster for 20 minutes"),
    GEB_BARREN_SOIL("Barren Soil", GodType.GEB, false, BoonDuration.TEMPORARY, Items.DEAD_BUSH,
        "Crops wither near you for 30 minutes"),
    GEB_CAVE_IN("Cave In", GodType.GEB, false, BoonDuration.ONE_USE, Items.GRAVEL,
        "Next block mined causes gravel to fall on you"),
    GEB_FAULT_LINE("Fault Line", GodType.GEB, false, BoonDuration.TEMPORARY, Items.TNT,
        "Random explosions near you for 5 minutes"),
    GEB_UNSTABLE_GROUND("Unstable Ground", GodType.GEB, false, BoonDuration.PERMANENT, Items.SOUL_SAND,
        "Permanent slight reduction to mining speed"),

    // ===== ISIS - God of Magic and Healing =====
    // Blessings
    ISIS_HEALING_TOUCH("Healing Touch", GodType.ISIS, true, BoonDuration.TEMPORARY, Items.GLISTERING_MELON_SLICE,
        "Regeneration III for 5 minutes"),
    ISIS_MAGIC_AFFINITY("Magic Affinity", GodType.ISIS, true, BoonDuration.TEMPORARY, Items.ENCHANTED_BOOK,
        "All enchantments boosted for 10 minutes"),
    ISIS_LIFE_BLOOM("Life Bloom", GodType.ISIS, true, BoonDuration.ONE_USE, Items.SWEET_BERRIES,
        "Instantly heal 10 hearts"),
    ISIS_PROTECTIVE_MAGIC("Protective Magic", GodType.ISIS, true, BoonDuration.TEMPORARY, Items.SHIELD,
        "Absorption IV for 10 minutes"),
    ISIS_MANA_SURGE("Mana Surge", GodType.ISIS, true, BoonDuration.TEMPORARY, Items.LAPIS_LAZULI,
        "Ability cooldowns reduced by 30% for 10 minutes"),
    ISIS_MYSTIC_RESTORATION("Mystic Restoration", GodType.ISIS, true, BoonDuration.ONE_USE, Items.CHORUS_FRUIT,
        "Restore all hunger and saturation"),
    ISIS_ARCANE_SHIELD("Arcane Shield", GodType.ISIS, true, BoonDuration.TEMPORARY, Items.NETHERITE_CHESTPLATE,
        "Resistance III for 8 minutes"),
    ISIS_BLESSED_RECOVERY("Blessed Recovery", GodType.ISIS, true, BoonDuration.TEMPORARY, Items.GOLDEN_APPLE,
        "Health regeneration doubled for 15 minutes"),
    ISIS_SPELL_WEAVER("Spell Weaver", GodType.ISIS, true, BoonDuration.ONE_USE, Items.BLAZE_POWDER,
        "Next potion effect lasts 3x duration"),
    ISIS_ETERNAL_GRACE("Eternal Grace", GodType.ISIS, true, BoonDuration.PERMANENT, Items.ENCHANTED_GOLDEN_APPLE,
        "Permanent slight increase to healing"),
    
    // Curses
    ISIS_ANTI_MAGIC("Anti-Magic", GodType.ISIS, false, BoonDuration.TEMPORARY, Items.MILK_BUCKET,
        "Cannot benefit from positive potions for 15 minutes"),
    ISIS_FESTERING_WOUND("Festering Wound", GodType.ISIS, false, BoonDuration.TEMPORARY, Items.ROTTEN_FLESH,
        "Cannot regenerate health for 10 minutes"),
    ISIS_MAGIC_BACKLASH("Magic Backlash", GodType.ISIS, false, BoonDuration.TEMPORARY, Items.FERMENTED_SPIDER_EYE,
        "All potions have reversed effects for 5 minutes"),
    ISIS_DRAINING_CURSE("Draining Curse", GodType.ISIS, false, BoonDuration.TEMPORARY, Items.WITHER_ROSE,
        "Wither II for 3 minutes"),
    ISIS_BROKEN_SHIELD("Broken Shield", GodType.ISIS, false, BoonDuration.TEMPORARY, Items.DAMAGED_ANVIL,
        "Armor provides 50% less protection for 10 minutes"),
    ISIS_SPELL_LOCKED("Spell Locked", GodType.ISIS, false, BoonDuration.TEMPORARY, Items.BARRIER,
        "Cannot use god abilities for 5 minutes"),
    ISIS_WEAKENING_HEX("Weakening Hex", GodType.ISIS, false, BoonDuration.TEMPORARY, Items.SPIDER_EYE,
        "Weakness III for 8 minutes"),
    ISIS_MANA_BURN("Mana Burn", GodType.ISIS, false, BoonDuration.TEMPORARY, Items.BLAZE_POWDER,
        "All cooldowns take 2x longer for 10 minutes"),
    ISIS_CORRUPTED_HEALING("Corrupted Healing", GodType.ISIS, false, BoonDuration.ONE_USE, Items.POISONOUS_POTATO,
        "Next healing source damages instead"),
    ISIS_ABANDONED_GRACE("Abandoned by Grace", GodType.ISIS, false, BoonDuration.PERMANENT, Items.WITHER_SKELETON_SKULL,
        "Permanent slight reduction to healing received"),

    // ===== SETH - God of Chaos and Storms =====
    // Blessings (13 boons from instructions)
    SETH_AFFLICTION_IMMUNITY("Affliction Immunity", GodType.SETH, true, BoonDuration.PERMANENT, Items.MILK_BUCKET,
        "Immune to wither, poison, blindness, weakness, slowness, hunger, nausea, and mining fatigue"),
    SETH_WITHER_STRIKES("Wither Strikes", GodType.SETH, true, BoonDuration.PERMANENT, Items.WITHER_ROSE,
        "Attacks give wither effect to target, preventing them from eating food"),
    SETH_DARKNESS_AURA("Darkness Aura", GodType.SETH, true, BoonDuration.PERMANENT, Items.COAL_BLOCK,
        "Emit darkness/suppress light sources in a 5 block radius"),
    SETH_NIGHT_FLIGHT("Night Flight", GodType.SETH, true, BoonDuration.PERMANENT, Items.ELYTRA,
        "Creative flight in darkness (light level 7 or below) or during night"),
    SETH_SHADOW_EFFICIENCY("Shadow Efficiency", GodType.SETH, true, BoonDuration.PERMANENT, Items.NETHERITE_PICKAXE,
        "Instant build and instant mine in darkness or during night"),
    SETH_ETERNAL_SHADOW_EQUIPMENT("Eternal Shadow Equipment", GodType.SETH, true, BoonDuration.PERMANENT, Items.NETHERITE_CHESTPLATE,
        "Tools, weapons, and armor don't lose durability in darkness or during night"),
    SETH_CHAOS_WARD("Chaos Ward", GodType.SETH, true, BoonDuration.PERMANENT, Items.OBSIDIAN,
        "Immune to explosions"),
    SETH_UNPREDICTABLE("Unpredictable", GodType.SETH, true, BoonDuration.PERMANENT, Items.ENDER_PEARL,
        "50% chance not to take damage from any given attack"),
    SETH_DEATH_DEFIANCE("Death Defiance", GodType.SETH, true, BoonDuration.PERMANENT, Items.TOTEM_OF_UNDYING,
        "25% chance not to die when killed, instead left with half a heart"),
    SETH_WITHER_AURA("Wither Aura", GodType.SETH, true, BoonDuration.PERMANENT, Items.WITHER_SKELETON_SKULL,
        "Entities that attack you are given wither for 10 seconds"),
    SETH_DECAY_PRESENCE("Decay Presence", GodType.SETH, true, BoonDuration.PERMANENT, Items.SOUL_SOIL,
        "Entities within 5 block radius are given wither for 3 seconds"),
    SETH_SHADOW_ASSASSIN("Shadow Assassin", GodType.SETH, true, BoonDuration.PERMANENT, Items.NETHERITE_SWORD,
        "While invisible, deal double damage with attacks"),
    SETH_WILD_MAGIC("Wild Magic", GodType.SETH, true, BoonDuration.PERMANENT, Items.RESPAWN_ANCHOR,
        "When hit, attacker takes damage equal to half the damage they dealt"),
    
    // Curses (9 curses from instructions)
    SETH_DARKNESS_BANE("Darkness Bane", GodType.SETH, false, BoonDuration.PERMANENT, Items.SOUL_TORCH,
        "Take damage over time in darkness (light level 7 or below)"),
    SETH_STARVATION("Starvation", GodType.SETH, false, BoonDuration.PERMANENT, Items.ROTTEN_FLESH,
        "Cannot eat food during night time or below light level 7"),
    SETH_SHADOW_PARALYSIS("Shadow Paralysis", GodType.SETH, false, BoonDuration.PERMANENT, Items.CHAIN,
        "Cannot jump, sprint, crouch, swim, or fly with elytra in darkness or night"),
    SETH_SELF_DECAY("Self-Decay", GodType.SETH, false, BoonDuration.PERMANENT, Items.WITHER_ROSE,
        "Given wither when hitting entities, eating food, or drinking potions"),
    SETH_EXPLOSION_MAGNETISM("Explosion Magnetism", GodType.SETH, false, BoonDuration.PERMANENT, Items.TNT,
        "Explosions pull you towards them"),
    SETH_WITHER_VULNERABILITY("Wither Vulnerability", GodType.SETH, false, BoonDuration.PERMANENT, Items.WITHER_SKELETON_SKULL,
        "Receive the wither effect for twice as long as normal"),
    SETH_CORRODING_ARMOR("Corroding Armor", GodType.SETH, false, BoonDuration.PERMANENT, Items.DAMAGED_ANVIL,
        "Armor takes durability damage while withering"),
    SETH_ETERNAL_AFFLICTION("Eternal Affliction", GodType.SETH, false, BoonDuration.PERMANENT, Items.MILK_BUCKET,
        "Cannot clear negative effects with milk"),
    SETH_PANDEMONIUM("Pandemonium", GodType.SETH, false, BoonDuration.PERMANENT, Items.BEDROCK,
        "Permanent random chance for minor negative effects"),

    // ===== ANUBIS - God of Death and the Afterlife =====
    // Blessings
    ANUBIS_DEATH_DEFIANCE("Death Defiance", GodType.ANUBIS, true, BoonDuration.ONE_USE, Items.TOTEM_OF_UNDYING,
        "Survive a fatal blow with 1 heart"),
    ANUBIS_SOUL_HARVEST("Soul Harvest", GodType.ANUBIS, true, BoonDuration.TEMPORARY, Items.SOUL_SAND,
        "Gain health from kills for 10 minutes"),
    ANUBIS_UNDEAD_COMMAND("Undead Command", GodType.ANUBIS, true, BoonDuration.TEMPORARY, Items.SKELETON_SKULL,
        "Undead creatures fight for you for 5 minutes"),
    ANUBIS_GRAVE_BLESSING("Grave Blessing", GodType.ANUBIS, true, BoonDuration.ONE_USE, Items.BONE,
        "Keep inventory on next death"),
    ANUBIS_LIFE_DRAIN("Life Drain", GodType.ANUBIS, true, BoonDuration.TEMPORARY, Items.GHAST_TEAR,
        "Attacks heal you for 30% damage for 10 minutes"),
    ANUBIS_SPIRIT_SHIELD("Spirit Shield", GodType.ANUBIS, true, BoonDuration.TEMPORARY, Items.SOUL_LANTERN,
        "Absorption V for 5 minutes"),
    ANUBIS_NECROTIC_POWER("Necrotic Power", GodType.ANUBIS, true, BoonDuration.TEMPORARY, Items.WITHER_SKELETON_SKULL,
        "Deal bonus damage to living entities for 10 minutes"),
    ANUBIS_RESURRECTION("Resurrection", GodType.ANUBIS, true, BoonDuration.ONE_USE, Items.NETHER_STAR,
        "Auto-revive at your bed on next death"),
    ANUBIS_SOULS_BLESSING("Soul's Blessing", GodType.ANUBIS, true, BoonDuration.TEMPORARY, Items.EXPERIENCE_BOTTLE,
        "Gain souls from kills (currency) for 20 minutes"),
    ANUBIS_ETERNAL_GUARDIAN("Eternal Guardian", GodType.ANUBIS, true, BoonDuration.PERMANENT, Items.BEACON,
        "Permanent slight damage bonus against undead"),
    
    // Curses
    ANUBIS_DEATH_MARK("Death Mark", GodType.ANUBIS, false, BoonDuration.TEMPORARY, Items.WITHER_ROSE,
        "All mobs target you preferentially for 10 minutes"),
    ANUBIS_SOUL_DRAIN("Soul Drain", GodType.ANUBIS, false, BoonDuration.TEMPORARY, Items.SOUL_SOIL,
        "Lose 1 heart max health for 15 minutes"),
    ANUBIS_UNDEAD_ATTRACTION("Undead Attraction", GodType.ANUBIS, false, BoonDuration.TEMPORARY, Items.ZOMBIE_HEAD,
        "Undead spawn more frequently near you for 10 minutes"),
    ANUBIS_GRAVE_TOUCH("Grave Touch", GodType.ANUBIS, false, BoonDuration.TEMPORARY, Items.SKELETON_SKULL,
        "Wither I for 10 minutes"),
    ANUBIS_HAUNTED("Haunted", GodType.ANUBIS, false, BoonDuration.TEMPORARY, Items.PHANTOM_MEMBRANE,
        "Take damage from spirits periodically for 5 minutes"),
    ANUBIS_WEAKENED_SPIRIT("Weakened Spirit", GodType.ANUBIS, false, BoonDuration.TEMPORARY, Items.BONE_MEAL,
        "Deal 40% less damage for 10 minutes"),
    ANUBIS_DOOMED("Doomed", GodType.ANUBIS, false, BoonDuration.ONE_USE, Items.WITHER_SKELETON_SKULL,
        "Next death, drop all items and experience"),
    ANUBIS_NECROTIC_CURSE("Necrotic Curse", GodType.ANUBIS, false, BoonDuration.TEMPORARY, Items.ROTTEN_FLESH,
        "Food provides no nourishment for 20 minutes"),
    ANUBIS_SPECTRAL_CHAINS("Spectral Chains", GodType.ANUBIS, false, BoonDuration.TEMPORARY, Items.CHAIN,
        "Cannot respawn at bed for 30 minutes"),
    ANUBIS_FORSAKEN("Forsaken", GodType.ANUBIS, false, BoonDuration.PERMANENT, Items.BARRIER,
        "Permanent slight increase to damage taken from undead");

    private final String displayName;
    private final GodType god;
    private final boolean isBlessing; // true = blessing, false = curse
    private final BoonDuration duration;
    private final Item icon;
    private final String description;

    BoonType(String displayName, GodType god, boolean isBlessing, BoonDuration duration, Item icon, String description) {
        this.displayName = displayName;
        this.god = god;
        this.isBlessing = isBlessing;
        this.duration = duration;
        this.icon = icon;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public GodType getGod() {
        return god;
    }

    public boolean isBlessing() {
        return isBlessing;
    }

    public boolean isCurse() {
        return !isBlessing;
    }

    public BoonDuration getDuration() {
        return duration;
    }

    public Item getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get all boons for a specific god
     */
    public static BoonType[] getBoonsForGod(GodType god, boolean blessingsOnly) {
        return java.util.Arrays.stream(values())
            .filter(b -> b.god == god && (!blessingsOnly || b.isBlessing))
            .toArray(BoonType[]::new);
    }

    /**
     * Get random blessings for a god (for selection menu)
     */
    public static BoonType[] getRandomBlessings(GodType god, int count) {
        BoonType[] allBlessings = getBoonsForGod(god, true);
        if (allBlessings.length <= count) {
            return allBlessings;
        }

        java.util.List<BoonType> list = new java.util.ArrayList<>(java.util.Arrays.asList(allBlessings));
        java.util.Collections.shuffle(list);
        return list.subList(0, count).toArray(new BoonType[0]);
    }

    /**
     * Get random curses for a god (for selection menu)
     */
    public static BoonType[] getRandomCurses(GodType god, int count) {
        BoonType[] allCurses = java.util.Arrays.stream(values())
            .filter(b -> b.god == god && b.isCurse())
            .toArray(BoonType[]::new);

        if (allCurses.length <= count) {
            return allCurses;
        }

        java.util.List<BoonType> list = new java.util.ArrayList<>(java.util.Arrays.asList(allCurses));
        java.util.Collections.shuffle(list);
        return list.subList(0, count).toArray(new BoonType[0]);
    }

    public enum BoonDuration {
        PERMANENT,    // Lasts until removed or death
        TEMPORARY,    // Lasts for a specific duration
        ONE_USE       // Triggers once then disappears
    }
}
