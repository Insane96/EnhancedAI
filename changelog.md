# 2.4.5.0
## Gameplay
* Snow Golem Healed by Snowballs -> Healed by Snowballs (no longer limited to Snow Golems)
* Shielding
  * The chance to block now works like vanilla shields: only damage coming from the front can be blocked, but projectiles and explosions can be blocked too (piercing arrows can't)
  * Fixed Shielding replacing the off-hand item (e.g. miners losing their pickaxe)
  * Fixed hurt shields not applying wielder things (e.g. enchantments)
* Skeletons
  * Fixed skeletons randomly strolling away while shooting. Skeleton shooting goal now has the same priority as vanilla, so skeletons stop shooting when fleeing the sun or wolves
  * Fixed skeletons not being able to shoot with modded bows
* Targeting fixes
  * Fixed Better Hurt By Target ignoring other target goals priority (e.g. lower priority target goals could override the revenge target, wolves left their owner's target when hurt)
    * Creepers and endermen now actually switch target when hurt
  * Fixed Better Hurt By Target making mobs switch to an attacker up to 5 seconds after being hit, even if the hit was ignored (e.g. the current target was closer)
  * Fixed 'Prevent infighting' working the other way around (with config set to 0.9, only 10% would not fight back)
  * Fixed all the animals losing the HurtByTargetGoal
  * Fixed Better Nearby Targeting losing vanilla target goals custom logic, now Polar Bears, Witches, Foxes and Bees have their own target goals and custom target goals from other mods are no longer replaced
    * Polar bears attacked players even without cubs nearby
    * Witches no longer alternated between healing raiders and attacking players
    * Foxes attacked any entity that hurt something, instead of only who hurt their trusted players
    * Bees kept targeting after stinging
  * Fixed mobs possibly forgetting a new target too early if they lost the previous one while not sensing it
* Teleport to Target fixes
  * Fixed mobs never teleporting unless the destination was inside a block
  * Fixed mobs possibly teleporting the other mob inside walls
  * Fixed the chance being re-applied every time the mob is reloaded
  * Fixed the mob and the target mob not showing back if the goal stopped
  * Fixed mobs left floating if unloaded while hidden
  * Fixed the goal possibly stopping while hidden, without teleporting
* Riding fixes
  * Fixed the chance to ride mobs being re-rolled every time the mob is reloaded (over time most mobs would become riders)
  * Fixed mobs changing the mob to ride while going to ride one
* Fixed Pick Up and Throw chance being re-applied every time the mob is reloaded
* Actual Blindness fixes
  * Fixed possible crash when visibility is checked without a looking entity (e.g. Ender Dragon when an End Crystal is destroyed)
  * Fixed blindness range multiplier being applied twice (with default config the follow range was multiplied by 0.0225 instead of 0.15)
  * Fixed mobs not losing the follow range penalty when blindness expires
* Leaders fixes
  * Fixed reinforcements chance scaling with the damage after armor and enchantments instead of before (leaders spawned far less reinforcements)
  * Fixed 'Charge per spawn' being inverted (increasing instead of decreasing)
  * Fixed 'Charge per spawn' being applied only once
* Warden fixes
  * Fixed Darkness Range also slowing down the anger update rate (anger decreased 2.5x slower with default config)
  * Fixed crash with wardens already present when the feature is enabled
  * Fixed wardens already present when the feature is enabled never using the sonic boom
* Witch fixes
  * Fixed Witches throwing bad potions at players only (e.g. they would buff Iron Golems)
  * Dark Art is now performed only once per witch, even if interrupted (previously an interrupted ritual was never performed again, but a reloaded witch could perform it again)
  * Fixed witches keeping the villager spawn egg in hand if the Dark Art was interrupted
  * Fixed possible leftovers Dark Art tags or invulnerable villagers
  * Fixed Dark Art particles not showing
* Drowned fixes
  * Fixed not being able to dismount dead drowneds
  * Fixed (again) not dismounting drowneds on Goal stop
  * Fixed drowneds dealing no damage forever if unloaded while drowning a target
  * Fixed Sun Resistant time possibly going negative while in water
* Fisher Mobs fixes
  * Fixed fisher mobs reeling in twice as fast as configured
  * Fixed fisher mobs not stopping when starting the goal in fishing range
* Item Disruption fixes
  * Fixed removing the first item found in the inventory instead of the selected slot
  * Fixed not stopping the player from using the disrupted item
* Fixed Patrol making mobs stuck forever (not attacking or moving) if the patrol position was unreachable or too far away
* Fixed Parkour and Spider Destuck considering mobs stuck after 1 second instead of 2. Creepers breaching now also wait 2 seconds (was 1.5)
* Fixed Teleport Anti-Cheese teleporting the target even when the mob was already in melee range
* Fixed Vehicle Anti-Cheese preventing mobs from dismounting vehicles
* Fixed forced explosion creepers stopping swelling if the target was lost
* Fixed breaching creepers barely working with Mobs > Targeting disabled, as they forgot the target 3 seconds after losing sight of it (now 15 seconds)
* Fixed Angry Creepers data getting re-applied every creeper load
* Fixed Creeper Launch timing having no randomness
* Fixed pearler mobs losing the PearlUseGoal when reloaded
* Fixed non-miner mobs getting pickaxe on reload
* Fixed dupe with mobs hooking other mobs inventory
* Fixed mobs that can bite biting each other endlessly
* Fixed animals scared of players not fleeing on first spawn
* Fixed Slime Size spawning size 1 slimes twice as often
* Fixed min monster despawn distance being applied to all mobs categories and not monsters only
* Fixed beta strafe left/right consistency
* Other fixes I might have forgotten to write

## Technical (config, data, data packs and modpacks)
* Healed by Snowballs moved to the Mobs module, the feature config will be reset
  * Fixed `enhancedai:snow_golem/healed_by_snowballs` (now `enhancedai:mobs/healed_by_snowballs`) not checked (was hardcoded to snow golems)
* Vehicle Anti-Cheese feature name fixed in config, the feature config will be reset
* Moved 'Player Only' and 'Target Chance' from Better Hurt By Target to Better Nearby Targeting config, as they only affect nearby targeting. These config options will be reset
* Mobs in `enhancedai:mobs/targeting/allow_target_switch` now have the Hurt By Target goal at the highest priority
* Fixed MPR `enhancedai:data` condition never matching enums and lists with `string_value`
  * Invalid `string_value` now throws an error when loading the data pack
* Mobs blacklisted from Spawning (or without the feature data) now behave like vanilla
  * Fixed crash with wardens without the Darkness Range data and wardens without the Sonic Boom Range data never using the sonic boom
  * Fixed Thirsty Witches without data always drinking speed potions and never healing
  * Fixed shulkers without data bullets not applying levitation
  * Fixed Snow Golems Damaging Snowballs ignoring the Spawning blacklist
* Fixed Warden Listen Range using the config value instead of the Warden's Data
* Fixed Leaders 'Charge per spawn' read from the config and not the Mob's Data
* Fixed ghasts possibly shooting fireballs endlessly if the fireballs shot data is lowered while shooting
* Fixed mobs that can climb walls no longer being able to climb ladders
* `enhancedai:bite` is now in the `enhancedai:doesnt_trigger_bite` damage type tag
* Fixed animals scared of players ignoring flee distance changes
* Melee Attacking fixes
  * Fixed mobs outside the `enhancedai:mobs/melee_attacking` entity type tag still using the attack speed based off attribute
  * Fixed mobs with 0 attack speed never attacking anymore
  * Fixed melee attack speed multiplier being possible set to 0 in config
* Fixed Slimes outside the `enhancedai:slime/jump_rate` entity type tag jumping with no delay
* Fixed possible crash with apprentice witches if both good and bad potion lists are empty
* Potion lists now log a clearer warning when a potion has duration or amplifier (e.g. `minecraft:healing,600,0`), as potions don't accept them
* Fixed pillagers never moving towards the target with a shooting cooldown of 0
* Fixed miner mobs block entity drops (e.g. containers contents) when tile entities are not blacklisted
* Fixed Panic on Fire not removing the goal when disabled
* Silverfish 'Ticks after hurt to wake up friends' min is now 1, as 0 disabled waking up friends
* Enhanced performance of Teleport to Target, Pick Up and Throw and Riding by adding the Goal only to the mobs that can use it
* Mob data and goals are no longer applied client side, as they're only used by the server
* The vanilla `/debugpath` command is now registered only in the development environment

# 4.2.4.0
* Fixed 'Blow up on death' not working correctly
  * Issue #250
* MPR Data Pack
  * Chance to equip mobs with armor (both the chance to attempt a full set and the chance per armor piece) now scales with depth, up to +20% at the bottom of the world, instead of a flat bonus below light level 12
  * Full moon bonus to that same chance raised from +10% to +20%, to match the new depth bonus cap
  * Same depth scaling (up to +20%) and full moon bonus (+20%) now also apply to the chance of equipment (weapons, tools, armor) getting enchanted
  * Iron and Diamond armor are now more likely to be picked over weaker materials when underground or during a full moon
* Enderman can no longer teleport targets farther than their follow range
* Fixed fisher mobs no longer attacking and ignoring line of sight
* Fixed Better Drowning Swimming Up not swimming up

# 4.2.3.0
* Pathfinding
  * Mobs can now freely pathfind over rails even when not already walking on one (by default, only mobs already standing on a rail can walk over other rails)
* Miner mobs can now use both main and off-hand to mine (requires changing the `offhand` data key, defaults to true)
* Fixed Pearler AI not applied if not equipping pearls
* Fixed Fisher AI stopping if losing line of sight of the target

# 4.2.2.5
* Lists now work with `enhancedai:change_data` property (still need to use `string_value`)

# 4.2.2.4
* Fixed enums not working with `enhancedai:change_data` property for MPR (need to use `string_value`)

# 4.2.2.3
* Sprinting mobs now sprint closer to the player (16 -> 10 blocks)
  * Also creepers can now sprint
* Hopefully fixed disabling 'Fix Walking Over Open Trapdoors' not working

# 4.2.2.2
* MPR Data Pack Integration with ISOs Daggers
* Tentative fix for animals getting stuck no longer moving (but still attacking)
  * This hopefully also fixes the issue with modded mobs ... even tho they shouldn't be affected

# 4.2.2.1
* Fixed config options descriptions mentioning outdated tags

# 4.2.2.0
* Added 'Patrol'
  * Iron Golems will now return close to their spawn point when getting too far away
* Added 'Better Leap at Target Goal'
  * Increases the jump distance when the mob's faster
* MPR Data Pack
  * Wolves no longer have reduced attack speed and increased attack damage
* Animals no longer use the Panic on Fire feature

# 4.2.1.3
* Fixed compatibility with latest MPR

# 4.2.1.2
* Removed all the LOW priority on events
  * Other mods can just bump up event priority
* Llamas, Pandas and Polar Bears no longer break vehicles
* Fixed compatibility with latest MPR

# 4.2.1.1
* Fixed some events having LOWEST priority, making MPR condition fail on reading the data keys
* Updated MPR to latest version

# 4.2.1.0
* Leaders
  * Leaders now spawn with a banner on their head
  * Lowered chance
* Now only 50% of mobs can be alerted when hurting a mob
* Hopefully fixed getting stuck on drowned's back when dying on them

# 4.2.0.2
* Fixed Spider slowdown not working
* Falling shockwave is now disabled by default
* Fixed Not Tempted Animals not using the new EAIData system

# 4.2.0.1
* Fixed Wolves and Cats still attacking animals when tamed
* Crash fix with Biting Mobs and Falling Shockwave

# 4.2.0.0
* Added back Biting Mobs
  * I totally forgot about this feature
* Added back Falling Shockwave
  * I totally forgot about this feature
* MPR Data Pack
  * Slowed down spiders
* Fixed Random Stroll not making use of the entity type tag
* Reverted back 'Fixed mobs pathing getting stuck sometimes if the path was the same as before'
  * Was making mobs getting stuck while normally walking
* Fixed some missing mobs to use some features 
  * Bogged couldn't attack villagers
  * Witches didn't count zombie villagers, zombified piglins, bogged and wither skeletons as allied
  * Zombie villagers couldn't be leaders
  * Bogged couldn't use better nearby targeting

# 4.1.2.0
* Targeting
  * Mobs now have 50% chance to forget the target if they can't sense it (sight, xray or glowing) after 15s
* Leader mobs
  * Reinforcements now spawn even if non-entities damage them
  * Added loot table enhancedai:leader_mob that will drop on leaders death. Empty by default
  * Lowered armor
* Fixed mobs pathing getting stuck sometimes if the path was the same as before

# 4.1.1.2
* Lowered xray range
* Fixed crash with latest InsaneLib

# 4.1.1.1
* MPR Data Pack
  * Zombies now have more chance to spawn as miners only in caves

# 4.1.1.0
* Added Enderman module
  * Added Look Anger Cone
    * Makes it easier to anger endermen by looking at them
* Fixed wrong quartz tag for Break anger

# 4.1.0.2
* MPR Data Pack
  * Mobs that spawn in caves now have increased max health and xray follow range (up to +100% at bedrock level)
  * Mobs that spawn outside (when not in full moon) will have lower follow range, xray follow range and movement speed
* Decreased default xray follow range
* Fixed fishing hook not rendering correctly

# 4.1.0.1
* Lowered max follow range override and increased min xray range
* Added Shields+'s copper shield to chances to block
* Skeletons can no longer sprint
* With MPR data pack, zombies and skeletons have less chance to spawn with equipment and to be enchanted
* Fixed wolves having twice as much health

# 4.1.0.0
The mod is no longer server side only
* Added back Fisher Mobs and Web Throwers
* Added back Angry Creepers sound effects
* Added back Custom Targeting
  * Skeleton and Zombie horses are now hostile towards the player
* Skeleton horses can no longer be neutral and scared by players (from Animals scared attack feature), but have been added to Custom Targeting

# 4.0.1.0-beta
* Updated MPR Integration data pack, porting stuff from Insane Survival Overhaul
  * Bees are smaller, Vexes are bigger, Phantoms, Sea Creatures, Skeletons and Zombies are randomly scaled
  * Drowned deal 50% less damage with tridents
  * Vexes have much less health
  * Vindicators have less attack damage
  * Skeletons and Zombies have a better chance to spawn with armor and weapons
  * Small animals have less attack damage
  * Zombies have a higher chance to spawn as miner the deeper they spawn
* Zombified piglin can now mine
* Reduced leader mobs spawn chance
* Increased follow range override (32\~48 -> 32\~64)
* Fixed shulker armor crashing the game

# 4.0.0.4-beta
* Jump in place now mobs jump only if the target is at least the height of the mob higher (e.g. zombies will jump if the target is at least 1.95 blocks higher)
* Fixed mobs spawned by leaders attacking creative players
* Fixed more crashes with Miner Mobs missing attributes
  * I hate you Mojank

# 4.0.0.3-beta
* Moved Push Resistance feature to InsaneLib

# 4.0.0.2-beta
* Removed Biting mobs and Falling Shockwave features since they require client-sided mod

# 4.0.0.1-beta
* Enabled Bogged, Breeze and Armadillo to use some features
* Fixed crash with Miner mobs not having the block break speed attribute
* Fixed zombie villagers not affected by some features

## 4.0.0.0-beta
Ported to 1.21.1

* Jump in place now makes mobs jump only if the target is at least one block higher
* Updated MPR data pack with smaller spiders and disabled baby zombies