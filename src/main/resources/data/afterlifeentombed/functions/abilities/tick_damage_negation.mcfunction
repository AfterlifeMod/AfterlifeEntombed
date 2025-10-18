# Increment timer
scoreboard players add @s negation_timer 1

# After 10 seconds (200 ticks), allow damage release
execute as @s[scores={negation_timer=200..}] run tellraw @s {"text":"§cDamage storage ready! Attack to release!","color":"dark_red"}
