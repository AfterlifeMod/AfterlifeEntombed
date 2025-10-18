# Call the Java code via a custom command
# This triggers the network packet to activate the ability on the server
execute as @s run tellraw @s[tag=debug] {"text":"Activating One with Chaos (Java)","color":"gray"}
# The Java event handler will pick this up and activate the ability
