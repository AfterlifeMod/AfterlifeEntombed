# Increment timer
scoreboard players add @s chaos_timer 1

# After 1 minute (1200 ticks), end Chaos Incarnate
execute as @s[scores={chaos_timer=1200..}] run function afterlifeentombed:abilities/end_chaos_incarnate
