# Changelog

## Upcoming
* Added Fire Immune Ticks
  * Set how many ticks a mob will be immune to fire before it starts to burn
  * By default it's now the same as the player (20 ticks)
* Fisher Mobs
  * Fishing range is now configurable
    * And has been increased from 24 to 32
  * Pull strength is now capped
* Parkour is now disabled by default
* Lowered default shulker armor
* Fixed shulkers attacking each other and in peaceful
  * Had to create custom Targeting AIs for them
* list_goals command now shows running goals in green
* Web Throwers feature is no longer limited to player targets

## Alpha 3.0.3
* Halved the default time Drowned are sun-resistant
* Fixed mobs with ranged attack AI trying to parkour
* Fixed player blocking hits when wielding a shield
* Fixed shield being given to mobs outside the entity type tag

## Alpha 3.0.2
* Finished porting witches
* Decided to not port custom attack and flee for now, will see in the future

## Alpha 3.0.1
This version has no Witches (except for dark arts and fleeing) as they are WIP
* Attribute renamed: `enhancedai:generic.xray_follow_range` -> `enhancedai:xray_follow_range`
* Many features have been split into multiple features
  * This makes it easier to disable single features
  * E.g. Creeper Swell has been split into Creeper Swell, Creeper Launch, Tnt Like Creepers and Disable Falling Swelling features.
* Some features have been implemented through MPR
  * E.g. Iron golem resistance and wolves buffs
* Cleaned up entity type tags to be split by module
* You now have more control over which mobs can ride other mobs
* Fixed command not accepting strings correctly (you have to surround them with quotes)
* Fixed anti-cheese probably not working

### Not yet Ported
* Witches: everything except dark art requires a full rewrite to make it less aggressive in changing vanilla code
* Custom Flee: requires a bit of thought to make it work with the new AI data
* Custom Targeting: requires a bit of thought to make it work with the new AI data

## Alpha 3.0.0
You are able to change AI at runtime without having to reload the world thanks to the new tech: AI Data, which lets you change parameters per mob, without having to reload the world.  
AI Data is defined as a resource location (`<namespace>:<id>`), by default the namespace is obviously `enhancedai`.  
Due how the vanilla AI works, the goals already present in the mob can't be changed with this system without a huge amount of work. E.g. For the "Animals not temped" feature they already have the Tempt AI, so it's impossible to add it back if it's removed.  
Check the command for more infos.  
The tags have been moved to their folders (e.g. `enhancedai:can_be_miner` -> `enhancedai:miner_mobs/can_mine`)

* Added enhancedai command to set the AI Data
  * `/enhancedai set <target> <data> <value>`  
    E.g. `/enhancedai set @e[type=creeper] enhancedai:creeper_swell/tnt_like true` will enable tnt like for all the creepers currently loaded. You can also get the value with `get` on an entity  
    A MobsPropertiesRandomness property will come to make use of the AI Data id instead of NBT
* Added a new Anti-Cheese feature (from the old "Endermen Get Over Here" feature, which is now gone)
  * Endermen will now teleport the target close to them if they can't reach or see them for a few seconds
  * Item disruption has been disabled by default
* Added back the Shielding feature
  * Due to not being able to make mobs use the shield to block (since they have no animation for it), mobs with a shield now have a chance to block damage.
  * Each shield (e.g. from Shields+) can be configured to have a different chance to block
* Added Sprint in Movement feature
  * Some mobs can now sprint towards the target when close enough
* Added Random Stroll Chance multiplier in Movement feature
  * Mobs now have a higher chance to randomly walk around when not targeting
* Leap feature is now the Parkour feature and it's enabled by default
  * Mobs can now leap up to 3 blocks forwards on the same Y level as the target
* Added sun-resistant Drowned
  * Drowned have 30 seconds where they don't burn in sunlight. During this time, they will not seek water
* Added a de-stuck goal for spiders. When targeting someone and stuck on a wall for more than 2 seconds they will throw themselves at the target
* Miner mobs now use the `forge:block_reach` attribute
* Melee mobs default attack speed multiplier is now 0.25 (so 1 attack/sec instead of 2)
* Removed Warden Interactions
  * Fleeing is done through Custom Flee feature
* Pillagers now try to reposition to not hit allies (or when they hit an ally)
* Merged Pillager and Witch flee features into a single "Target Flee" feature
* Web throwing feature is no longer limited to spiders
* Shulker bullets now take duration and amplifier from owners so each shulker can have its own
* Snow golems can now strafe when attacking (disabled by default)
* Break anger now accepts a new field `requires_line_of_sight` to determine if the mob requires line of sight to get angry
* Fixed warden sonic boom range being disabled
  * Now configurable as fixed value instead of multiplier, obviously compatible with AI data
* Fixed temporary cobwebs breaking any block in the cobweb's position
* Fixed blindness multiplier not making mobs forget the target
* Fixed disabling "Melee Attacking" feature making melee attacks have no cooldown
* Fixed fisher mobs canceling the fishing rod cast if they can't see the target
* Fixed dark art witches dropping the equipped spawn egg

## 2.7.1
* Villagers no longer use target changes
* Cats no longer use animal changes
  * You weren't able to tame them due to always fleeing

## 2.7.0
**Config options might have been renamed, thus reset. Backup your config file.**

* Snow Golems' snowballs damage and freeze are now configurable
* Mobs now can rarely attack each-other again
* Increased shulker bullets levitation duration and amplifier
* Endermen can now switch target
* Fixed Iron Golem's "fire tick faster" not working as intended
* Fixed Ravager feature not working

## 2.6.13
* Drowned leaps now have a fixed force instead of being based on the target's distance

## 2.6.12
* Fixed nearest attackable target not using the correct follow range if changed after the goal has been applied

## 2.6.11
* Fixed drowned swimming like crazy

## 2.6.10
* Drowned can now attack again in daytime if can't reach water
    * Mojang says it works as intended, but it's stupid they just stand still doing nothing and probably die
* Drowned now have a much easier time getting out of water
    * They basically jump out of the water
* Mobs can now break many more modded vehicles (Quark and Abnormals veichles)
* Forget Target Chance is now disabled by default
    * Breaks xray follow range
* Hopefully fixed mobs standing still when hurt by target instead of approaching them again

## 2.6.9
* Added a new item tag for what items count as a fishing rod for Fisher Mobs
    * `enhancedai:fisher_rods`
    * Already contains Tide's fishing rods

## 2.6.8
* Monsters can now forget their target
* Fishing min and max distance can now be changed

## 2.6.7
* Fixed a huge bug that made animals fleeing very performance heavy

## 2.6.6
* Bosses can no longer be teleported by the Over Here feature
    * Added a new Entity Type Tag `enhancedai:get_over_here_teleport_blacklist`

## 2.6.5
* Fixed compat with latest InsaneLib

## 2.6.4
* Lowered Blaze Attack stats
* Update for latest InsaneLib

## 2.6.3
* Jumping mobs now take into account jump boost
* Animals baby born (so usually from breeding) can no longer be hostile
* Fixed Fisher mobs no longer attacking when target was close enough

## 2.6.2
* Fixed possible crash with Hurt By Target

## 2.6.1
* Fixed some wonkyness with the new fisher mobs
* Fixed Universal Anger breaking Hurt By Target
    * I have no idea what is this check, so I've removed it
* Breaching creepers should blow up less randomly

## 2.6.0
* Mini rework Fisher Mobs
    * They no longer approach the target if farther than 4 blocks, they'll just reel it in
* Endermen disrupting items no longer throws them away, but simply under the player with a pickup delay of 1.5s
* Fixed a bug that made jumping mobs reset the mob's attack cooldown
* Fixed Angry Creeper.Forced Explosion being duplicated

## 2.5.8
* Warden
    * Added Listen range multiplier
        * Increases range at which vibrations reach Wardens
    * Added Step Up
        * Makes warden step up blocks like horses without needing to jump
    * Temporarily disabled "Increase sonic boom range" as was crashing the game

## 2.5.7
* Animals now have a small chance to be hostile
    * `enhancedai:can_fight_back` entity type tag has been renamed to `enhancedai:can_be_neutral`
    * New `enhancedai:can_be_hostile` entity type tag
* Added `enhancedai:doesnt_trigger_bite` damage type tag
    * Damage types in this tag will not trigger biting mobs
* Halved the chance for silverfish to call other silverfish

## 2.5.6
* Requires InsaneLib 1.18.3 and Forge 47.4.0
* Wolves are no longer affected by biting mobs
* Caverns and Chasms Mimes now have most of the AI changes applied

## 2.5.5
* Fisher mobs
    * Now only hooks main or off-hand items
    * Items reeling now works on any entity not only players
    * Cooldown configurable and doubled if reels in someone
* Witches no longer throw invisibility potions if aren't on ground
* Added Caverns and Chasms Deepers to many entity type tags
* Fixed Animals attacking if they had an owner

## 2.5.4
* Item Disruption now has a 10 seconds cooldown
* Reduced lag caused by the Animal Attack AI with many baby animals
* Environmental ducks now can flee and fight back
* Web Throwers slowness no longer counts as a Beacon effect
* Caverns and Chasms Deepers and Savage and Ravage Creepie are now affected by the Swelling Creeper feature
* Added a new entity type tag `enhancedai:can_creeper_launch` which defines which creepers can launch (by default only vanilla creepers)
* Creepers can no longer attack villagers and iron golems

## 2.5.3
* Fixed mobs attack speed being doubled
    * Mojang's melee attack goal uses a method that halves the attack speed cooldown ... for no reason
* Mobs max attack speed is now 2 instead of 1.25
* Miner zombies no longer reset the attack goal if within attack range
* Decreased skeleton's shooting cooldown
    * Also, hard mode no longer makes skeletons shoot faster

## 2.5.2
* Witches, Skeletons and Pillagers now actually flee from the target instead of running away from any Player
    * This should increase the performance of the features since they don't have to get the nearest entity anymore
* Wolves can now heal like Horses: 1 in 900 chance each tick
* When Webber spiders now hit the target, the web cooldown is now increased by 5 seconds
* Tamable animals no longer flee from players

## 2.5.1
* Requires InsaneLib 1.15.0
* Mobs no longer jump if the target is more than 6 blocks away
* Pillagers, Skeletons and Piglin have been removed from the `allow_jumping` entity type tag (they can no longer jump)

## 2.5.0
* Added Jump feature
    * If the mob is below the target by a few blocks, he will jump trying to hit him
* Added a new explosion sound for Angry Creepers. It's now the default
    * The fuse sound is vanilla but the explosion sound is the old minecraft beta one
* Configurable Animal flee speed
* Added a config option for Better Hurt by target to prefer players to other entities
* Performance improvement for fisher mobs

## 2.4.10
* Removed `use_follow_range_changes` entity type tag
    * Replaced with `change_follow_range` that applies only the follow range override
    * Also with `apply_xray` that applies only the xray range override
* Added "Block Blacklist as Whitelist" for Miner Mobs

## 2.4.9
* Fixed Beta creepers ignoring movement speed changes
* Fixed mobs no longer pathfinding to target if no longer seen
* Fixed mobs trying to ride entities not on ground
* Added russian lang

## 2.4.8
* Added a config option to allow targeting changes to work on non-players target (e.g. Zombies targeting Villagers)
* Breaching creepers can now breach from 24 blocks instead of the radius times 5
    * Added config option
    * Fixed Breaching creepers having a hard cap of 14 blocks range
* Fisher mobs
    * Fishing in player's inventory now has a cooldown (after fishing in the players' inventory the fisher needs to hook 4 times before being able to fish players' inventory again)
    * No longer need to be on ground to hook
* Reduced max slime sizes spawn
* Fixed Launching Creepers launching to the target without cooldown when can't see the target

## 2.4.7
* Fixed mobs staying at close distance but not reaching for attacking
    * The previous version fix increased the chance for mobs to stay at close distance but not moving to get close to attack
* Fixed mobs neglecting to use the melee attack goal if too close to the entity
    * Not sure if this does anything, but I missed changing this when porting the melee attack range from 1.20.2

## 2.4.6
* [MC-198068](https://bugs.mojang.com/browse/MC-198068) again
    * Fixed attack rate resetting when mobs were hit. I've removed the `followingTargetEvenIfNotSeen` check in `canContinueToUse`

## 2.4.5
* Angry creepers now blow up after death like 0.30 creepers
    * Configurable to also make charged creepers only or any creeper to behave the same
* Pearler and Fisher Mobs can now use the item as long as they are not underwater (they couldn't use them if touching water)
* Pearler mobs no longer use the pearl if they can't see the target
* Pearler mobs cooldown to throw a pearl increased

## 2.4.4
* 'Stop mounting if too much suffocation' is now reset when dismounting
* Fixed Breaching Creeper Explosions ignoring block hardness, making the explosion break **any** block
* Fixed 'Stop mounting if too much suffocation' affecting players too

## 2.4.3
* Miner mobs no longer drop experience from blocks broken
* Fixed breaching creepers with ITR making huge holes when not breaching
* Fixed miner mobs LivingDestroyBlockEvent getting called too many times
    * It's now called only when the mob attempts to break blocks
* Fixed miner mobs dropping the block's item even if `Level#destroyBlock` returned false
* Fixed llamas attacking indefinitely (removed from `use_target_changes` tag)
* Fixed breaching creepers not working most of the time

## 2.4.2
* Lowered cooldown from skeleton shooting in Easy and Normal
* If Iguana Tweaks Reborn is installed, Breaching creepers now have bigger explosions
* Fixed mobs not dismounting correctly when taking suffocation damage

## 2.4.1
* Fixed some mobs losing the ability to attack, like phantoms and wolves
* Fixed Darkness range multiplier not being applied

## 2.4.0
* Added Warden
    * Increased sonic boom range and darkness range
* Added Beta Creepers
* Increased mounting chance
* Slightly slowed down miner mobs
* Fixed Angry creeper integration with ITR

## 2.3.1
* Potential fix for [MC-198068](https://bugs.mojang.com/browse/MC-198068)

## 2.3.0
* All features are now opt-in
    * Anti-Cheese `ignore_anti_cheese` tag is now `can_use_anti_cheese`
    * Avoid Explosions `no_run_from_explosion` tag is now `can_run_from_explosion`
    * Targeting `no_target_changes` and `no_follow_range_changes` tags are now `use_target_changes` and `use_follow_range_changes`
* `allow_target_change` has been renamed to `allow_target_switch`
* Baby animals now also get attack AI, but don't use it until grown up
* Snowman attack speed can now be configured
* Miner mobs no longer mine if mobGriefing is set to false

## 2.2.2
* Fixed pillagers Attack When Avoiding completely breaking Flee
* Fixed fishing hook not rendering for non-zombie entities
* The pillager attack goal will no longer reset the target
* Phantoms are no longer affected by targeting changes
* Fixed climbing mobs always playing ladder sound

## 2.2.1
* Creepers no longer alert their vehicle
* Pigs are no longer eligible to be ridden
    * Can't figure out why they stop moving
* Fixed mobs having a lower priority Hurt By Target Goal not switching target
* Fixed Custom Flee and Custom Hostile ignoring chance

## 2.2.0
* Added Silverfish feature
    * They now call more friends when hurt
* Added a custom flee config
    * A json config where you can specify which mobs should run from which
* Added Ravager Feature
    * Makes the ravager break any block in the `enhancedai:breakable_by_ravager` block tag
* Added `must_see` and `priority` to Custom hostile config
* Added a new sound for Angry Creepers 😏
* Fixed pillagers features having wrong priority making it not possible to use NBT to change the data

## 2.1.0
* Added Pillager shoot
    * Pillagers can now shoot from farther away
    * Inaccuracy and shooting cooldown is now configurable
* Added Pillager Flee
* Added a Custom Hostile config so any mob can target any mob
    * By default, skeletons, creepers and spiders attack villagers
* Fisher Mobs
    * Now have 40% chance to hook items in the player's inventory
    * Mobs now reel in the hook faster if it's on the ground
    * Reel in time is now configurable (and lower in Hard 😈)
    * Fixed a bug where when re-joining the world fishers would no longer fish
* Skeleton inaccuracy is now configurable per difficulty
    * Slightly decreased accuracy in hard
* Fixed Witches throwing potions from farther than their throw range
* Fixed miner mobs breaking blocks way faster than they should've
    * With this, Time to break multiplier has been set back to 1

## 2.0.5
* Added Dimension Blacklist for miner mobs
* Fixed "Better hurt by target" "alert others" not working
* Fixed Miner Mobs crashing the game
* Fixed server hanging if skeletons distance from target was 0

## 2.0.4
* Fixed "Better hurt by target" removing the "alert others" feature
* Hopefully fixed Dark Art Witches crashing with some other mod
* Fixed mobs no longer running from creepers
* Fixed launching creepers no longer launching vertical
* Fixed miner mobs not breaking blocks if too close to target but can't see it

## 2.0.3
* Mobs that run from explosions will now also dismount to run away
* Miner mobs are now set "aggressive" when mining (zombies and vindicators should rise their hands)
* Increased default riding chance
* Fixed vindicators not being able to open doors

## 2.0.2
* Miner mobs will no longer mine if the `LivingDestroyBlockEvent` is canceled
* Better targeting when hit
    * Mobs will now switch target if the entity that hit them is closer
    * Will also prefer targeting players over other entities (e.g. if a zombie is attacking a golem, will always switch to players)

## 2.0.1
* Item disruption
    * No longer only for endermen (but by default only for them)
    * Chance is now saved in the mob (and can be changed)
    * Now drops the item on left or right
* Misc Illagers removed, any mob can now open doors (by default only Illagers)
* Added a "bonus movement speed" config option to Movement feature
* Halved Snow golems snowballs damage
* Mobs can now mount pigs alongside spiders
* Many more tags and tags are now always listed in the feature description in the config
* Launch creepers are now less accurate on hard and more accurate on easy
* Launch creeper explosion radius override can now be disabled
* Fixed creeper starting exploding after up to 0.75 seconds

## 2.0.0
* Backported 1.20.2 mob attack reach change [(Minecraft Wiki)](https://minecraft.wiki/w/Java_Edition_1.20.2#Mobs)
    * This is automatically applied if the Attacking feature is enabled
    * Melee Attacks Attribute Based is now enabled by default as it works much better thanks to the backport
* Added Illagers module
    * Added Misc feature. Illagers can now open doors at any time
* Added Riding
    * Some mobs now search for a spider to ride
    * If a mob takes too much damage while riding will dismount
* Added Break trapping vehicles
    * Most mobs now break boats and minecarts if trapped in them
    * Disabled "Prevent boating and minecarting"
* Added Slimes module and feature
    * Slimes can now spawn bigger and jump more often
* Implemented Enderman Item Disruption
    * Whenever an enderman attacks a player, there's a chance for the player's held item(s) to fall
* Animals has been split into 3 features
    * Animals Scared Attack
        * Make animals fight back or be scared by players
        * Knockback is now based off their size
        * Added two entity type tags `enhancedai:can_fight_back` and `enhancedai:can_be_scared_by_players`. Only animals in these tags are affected by the feature
    * Animals Group Flee
        * Make animals flee/fight back when one is attacked
    * Not Tempted Animals
        * Makes animals not tempted by food
        * Added a new entity type tag `enhancedai:can_ignore_food_temptation`. Only animals in this tag can have the chance to not be tempted by food
* Added Shulker Armor
    * Higher armor of the shulker when it's closed, when it's peeking and when it's open (Vanilla is 20 armor when it's closed)
* Added Shulker Attack
    * Customizable shulker attack speed (now fire faster)
    * Shulkers now close when lower health
* Anti-Cheese
    * Added `enhancedai:ignore_anti_cheese` entity type tag
* Avoid Explosions
    * Added `enhancedai:no_run_from_explosion`
* Warden Interactions
    * Added `enhancedai:ignore_warden_interaction`
* Digger Zombies -> Miner Mobs
    * Miner's AI can now be applied to any mob in the entity type tag (`enhancedai:can_be_miner`) (by default, only zombies, like before)
    * Blocks can now be blacklisted with a block tag (`enhancedai:miner_block_blacklist`)
    * Added 'Always require proper tool'
    * Added `enhancedai:time_to_break_multiplier` nbt tag that can be used to set the multiplier on time to break blocks
    * Upgraded the Wooden Pickaxe to a Stone Pickaxe
* Fisher Zombies -> Fisher Mobs
    * Fisher's AI can now be applied to any mob in the entity type tag (`enhancedai:can_be_fisher`) (by default, only zombies, like before)
    * Fishers now fish from closer to the target
    * Fishers can now fish any entity and no longer players only
* Pearler Zombies -> Pearler Mobs
    * Pearler's AI can now be applied to any mob in the entity type tag (`enhancedai:can_be_pearler`) (by default, only zombies, like before)
    * Increased ender pearls in hand by 1
    * Increased throwing accuracy
    * Halved cooldown
* Biting Zombies -> Biting Mobs
    * Any mob can now bite as long as it's in the entity type tag (`enhancedai:can_bite`) (by default, only zombies, like before)
* Piglin Quartz -> Break Anger
    * Can now be fully configured with which blocks are broken and which mobs are angered
    * By default, it's the same as before: Zombified Piglins get angry if players break quartz
* Creeper Cena is now Angry Creeper
    * Cena sounds can be re-enabled in the config
    * Also fixed fuse animation not matching the fuse time
* "Allow Climbing" is much more reliable and mobs no longer get stuckat the top of a ladder, so it's now enabled by default
* Mobs are now affected by blindness (They see only 10% of range)
* Drowned should get stuck less
* Witches no longer heal themselves if above 70% health
* Mobs now flee from the warden instead of attaking it
* Changed "Base" module to "Mobs"
* Removed shielding
    * Barely worked and making it work basically requires rewriting mobs
* Fixed witch potion warning in logs
* Fixed reloading the world giving zombies ender pearls or fishing rods
* Fixed neutral mobs not working

## 1.15.4
* Fixed world freezing when a Wither Skeleton loaded

## 1.15.3
* Biting zombies now have a higher chance to bite in hard
* Fishing zombies now dismount entities
* Fixed modded creepers exploding even if they aren't supposed to
* Fixed launching creepers no longer launching

## 1.15.2
* Now requires InsaneLib 1.11.1

## 1.15.1
* Mobs maximum attack speed can now be set (and has been lowered (2 attacks/sec -> 1.25))
* Digger zombies: Block breaking sound is now the same as player breaking

## 1.15.0
* Added Attacking>Melee Attack Speed Based
    * Makes melee mobs attack rate based off attack speed (reduced by 60%/50%/40% on Easy/Normal/Hard compared to players)
* Added 'Wearden Interactions'
    * Mobs will attack the Warden
    * Can be set so mobs flee from the warden instead
* Digger Zombies now finally drop blocks they mine
    * Decreased mining speed and made them require a tool to mine (zombies with a fishing rod will no longer mine)
* Slightly lowered fisher zombie pull force

## 1.14.2
* Skeletons
    * Skeletons now shoot slower if the target is farther away
    * Increased shooting cooldown
* Fixed vanilla bug MC-198068 (mobs randomly stop targeting entities)
* Fixed vanilla bug where mobs switch target when hit even if the target is the same

## 1.14.1
* Fixed and nerfed Witches
    * No longer use Invisibility and Slow Falling if drinking a potion
    * Fixed vanilla potions dropping as "Uncraftable Potion"
    * Drinkable and throwable potions list in the config now accepts vanilla potions

## 1.14.0
* Added Shulker Bullets
    * Changed duration and amplifier of Levitation (10 seconds I -> 10/5/2.5 seconds II/IV/VIII in easy/normal/hard)
* Slowed down explosion avoid flee speed
* Added config options for cena forced explosion

## 1.13.2
* Nerfed Witches a little bit
* Fixed some target goal selectors not being copied to mod's target goal
    * This might fix some mobs attacking for no reason

## 1.13.1
* MC 1.20.1
* Fixed ranged targets attacking out of range

## 1.13.0
* Port to 1.20
* Dark art withches now stop the ritual if moved too far from the villager
* Snow golems no longer attack if out of range

## 1.12.2
* Launch creepers now have a smaller explosion
* Launch creepers have now reduced particles
* Fisher and Pearler zombies no longer use the item if in water
* Fisher zombies cooldown reduced
* Mobs no longer flee from TNTs

## 1.12.1
* Spawning feature (renamed from Base) can now be disabled
* Creeper cena now also breaks more blocks if SR is present
* Rebalanced some default values

## 1.12.0
* Added Biting Zombies feature
    * When a player attacks a mob with no weapon, has a chance to get bitten
* Added Wither Skeletons feature
    * Wither Skelly can spawn with Bow and shoot Withered arrows
* Added Wolves feature
    * Double Wolves HP and Damage
* Added Snow Golems feature
    * Snow golems attack like skeletons, are more precise and shoot faster
    * Snowballs damage and freeze entities hit
    * Snowballs heal snow golems
* Creepers no longer swell when falling

## 1.11.0
* Added Iron Golem feature
    * Iron Golems now have an innate 40% damage resistance and are kept less time on fire
* Added Zombified Piglins
    * Breaking quartz now alerts Zombified Piglins around
* Webbing spiders now gain a speed boost if they hit the target
* Heavily reduced neutral mobs chances (~~60%/25%/10%~~ -> 25%/10%/4%)
* Halved Animals knockback

## 1.10.4
* 60%/25%/10% of mobs can now spawn neutral
* Food no longer tempts 50% of animals

## 1.10.3
* Skeletons attack cooldown and charge time can now be configured
* Reduced witches Resistance when performing dark arts
* Fisher zombies now fish closer to targets
* Witches now throw Slowness I potions instead of II
* Added Apprentice Witches
    * Witches that will throw random potions, even wrong ones
* Nerfed Spammer skeletons

## 1.10.2
* 40% of animals now flee from players
* Animals no longer flee if they can attack back
* Digger zombies no longer drop items from blocks mined
* Fixed digger zombies overriding off hand items
* Fixed zombies stopping mining mid block
* Fixed digger zombies ignoring the 'Blacklist Tile Entities' config option

## 1.10.1
* Launching creepers inaccuracy can now be configured
* Skeleton Spammers now spam less, deal less damage and can be spawned with "enhancedai:spammer" tag

## 1.10.0
* Updated to 1.19.4

## 1.9.2
* Walking fuse creepers now slowdown when exploding
* Reduced animals base attack damage (4 -> 3)
* Animals now scare only animals of the same species
* Reduced follow range override (32~64 -> 24~48)
* Reduced xray distance (16~32 -> 12~24)
* Reduced Throwing Web Spiders range and slowness applied

## 1.9.1
* Only 20% of animals are now able to fight back, the others will just scatter
* Skeleton inaccuracy is now loaded from tag "enhancedai:inaccuracy"
* Spiders Web Throwers
    * Reduced thrown web damage to 3 from 5
    * Fixed stackSlowness = false not working correctly
* Fixed XRay mobs targeting players from far, far away

## 1.9.0
* The update in the number is just to keep 1.19.2 and 1.19.3 separated
* Requires InsaneLib 1.7.4
* Creeper Cena inflating animation now matches with the Fuse time

## 1.8.1
* Updated to 1.19.3
* Creeper
    * Creeper Cena now emits particles when players are close enough
    * Chance increased (~~2%~~ -> 3%) but reduced explosion power (~~6~~ -> 5)
    * Cena and Launching Creepers particles can be disabled
* Fixed level's `RandomSource` causing crashes
* Fixed Zombies Fishing Rods and Ender Pearls replacing what they had in the off-hand
* Dark Ark Witches
    * Witches summoned by Dark Art Witches can no longer be Dark Art Witches
    * Fixed Dark art Witches failing to spawn a villager, causing a crash

## 1.8.0
* Added Prevent Infighting. Mobs no longer attack each-other
* Villagers Attacking
    * No longer attack enemies (can be configured)
    * No longer attack players with high enough reputation (defaults to Iron Golem)
* Small rework to launching creepers
    * Launching creepers now emit smoke particles to let you know they can launch
    * Launching creepers accuracy (in both exploding timing and launching direction) is now lower at lower difficulties (in Hard they are slightly less accurate than before, in Easy they can now easily miss you)
* Spiders Throwing Webs
    * Cave Spiders now also apply poison when hitting entities (configurable)
    * Added config option to place a cobweb on the entity hit by the thrown web
    * Added config option to disable slowness effect correctly
    * Reduced max slowness level (~~VI (6)~~ -> IV (4))
* Witch Potion Throwing
    * Halved Weakness potion duration (~~1:30~~ -> 0:45)
* Drowned now go in Swimming pose when swimming, quite ugly as they don't support the swimming animation fully
* Animal knockback can now be configured
* Added italian (by me) and portuguese translations (thanks to https://github.com/FITFC)

## Beta 1.7.1
* Requires InsaneLib 1.7.1

## Beta 1.7.0
* Ported to 1.19.1+, requires InsaneLib 1.7.0
* Added Shielding Feature (mobs will rise shields when the target is near them), disabled by default
* Decreased run speed when running from explosions
* Increased Blaze Time Between Fireballs (~~3\~6~~ -> 4~10 ticks)