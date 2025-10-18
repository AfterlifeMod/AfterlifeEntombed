# Check if holding a different god's stone for god switching
execute as @s store result score @s holding_godstone run function afterlifeentombed:abilities/check_godstone

# If holding different godstone, switch gods
execute as @s[scores={holding_godstone=1..}] run function afterlifeentombed:abilities/switch_god

# Otherwise, activate Chaos Incarnate
execute as @s[scores={holding_godstone=0}] run function afterlifeentombed:abilities/start_chaos_incarnate
