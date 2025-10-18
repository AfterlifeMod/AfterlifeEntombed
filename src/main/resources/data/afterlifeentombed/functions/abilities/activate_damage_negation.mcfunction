# Activate damage negation mode
scoreboard players set @s damage_negation_active 1
scoreboard players set @s stored_damage 0
scoreboard players set @s negation_timer 0
tellraw @s {"text":"§4Damage Negation active - storing damage for 10s","color":"dark_red"}
