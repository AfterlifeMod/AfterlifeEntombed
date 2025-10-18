# Check if player is on fire
execute as @s[nbt={Fire:1s}] run function afterlifeentombed:abilities/desert_walker_flight
execute as @s[nbt=!{Fire:1s}] run function afterlifeentombed:abilities/desert_walker_teleport
