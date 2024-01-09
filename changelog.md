# Changelog

* Change all blacklists to whitelists

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