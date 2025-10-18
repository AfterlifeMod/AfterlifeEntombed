# Track time used
scoreboard players add @s one_chaos_time 1

# Check if reached 2 minutes (2400 ticks)
execute as @s[scores={one_chaos_time=2400..}] run function afterlifeentombed:abilities/stop_one_with_chaos
