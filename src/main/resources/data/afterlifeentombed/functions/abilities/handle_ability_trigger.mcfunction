# Handle ability activation from Origins key presses
# The score indicates which ability was pressed (1-4)

# Ability 1: One with Chaos
execute as @s[scores={god_avatar_ability=1}] run function afterlifeentombed:abilities/java/activate_one_with_chaos

# Ability 2: Damage Negation  
execute as @s[scores={god_avatar_ability=2}] run function afterlifeentombed:abilities/java/activate_damage_negation

# Ability 3: Desert Walker
execute as @s[scores={god_avatar_ability=3}] run function afterlifeentombed:abilities/java/activate_desert_walker

# Ability 4: Chaos Incarnate
execute as @s[scores={god_avatar_ability=4}] run function afterlifeentombed:abilities/java/activate_chaos_incarnate

# Reset the trigger
scoreboard players set @s god_avatar_ability 0
scoreboard players enable @s god_avatar_ability
