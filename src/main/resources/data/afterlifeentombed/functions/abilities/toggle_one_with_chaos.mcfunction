# Check if player has One with Chaos active
execute as @s store result score @s one_chaos_active run data get entity @s {Tags:["one_with_chaos_active"]}

# If not active, activate it
execute as @s[scores={one_chaos_active=0}] run function afterlifeentombed:abilities/start_one_with_chaos

# If active, deactivate it
execute as @s[scores={one_chaos_active=1}] run function afterlifeentombed:abilities/stop_one_with_chaos
