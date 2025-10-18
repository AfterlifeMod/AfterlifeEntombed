# Check if player is holding a godstone
execute as @s[nbt={SelectedItem:{id:"afterlifeentombed:godstone_of_ra"}}] run return 1
execute as @s[nbt={SelectedItem:{id:"afterlifeentombed:godstone_of_horus"}}] run return 2
execute as @s[nbt={SelectedItem:{id:"afterlifeentombed:godstone_of_thoth"}}] run return 3
execute as @s[nbt={SelectedItem:{id:"afterlifeentombed:godstone_of_shu"}}] run return 4
execute as @s[nbt={SelectedItem:{id:"afterlifeentombed:godstone_of_geb"}}] run return 5
execute as @s[nbt={SelectedItem:{id:"afterlifeentombed:godstone_of_isis"}}] run return 6
execute as @s[nbt={SelectedItem:{id:"afterlifeentombed:godstone_of_anubis"}}] run return 8
return 0
