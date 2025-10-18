# Apply invisibility and phase effects
effect give @s minecraft:invisibility 999999 0 true
tag @s add one_with_chaos_active
scoreboard players set @s one_chaos_time 0
tellraw @s {"text":"§5One with Chaos activated!","color":"dark_purple"}
