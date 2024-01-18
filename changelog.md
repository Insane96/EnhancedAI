# Changelog

## 1.6.11
* Fixed Digger Zombies crashing the game

## 1.6.10
* Digger zombies will no longer dig if the `LivingDestroyBlockEvent` is canceled

## 1.6.9
* Fixed crash if Tinkers Construct is not installed and the feature is

## 1.6.8
* Backported 1.20.2 attack reach change [https://minecraft.wiki/w/Java_Edition_1.20.2#Mobs]
  * This is automatically applied if the Attacking feature is enabled
  * Melee Attacks Attribute Based is now enabled by default as it works much better thanks to the backport 

## 1.6.7
* Skeletons are less painful to fight now (and more configurable)

## 1.6.6
* Skeletons can now use Tinkers' bows
  * Basic support, some modifiers might not work due to requiring Crit Arrows which are only shot by players
  * Bows only, no crossbows

## 1.6.5
* Witches summoned by Dark Art Witches can no longer be Dark Art Witches
* Fixed Dark art Witches failing to spawn a villager, causing a crash

## 1.6.4
* Decreased running speed from explosions
* Increased Blaze Time Between Fireballs (~~3\~6~~ -> 4~10 ticks)
* Witch Potion Throwing
  * Halved Weakness potion duration (~~1:30~~ -> 0:45) 
* Fixed Creeper Cena exploding 0.1 seconds too early

## 1.6.3
* Added Villager Module
  * Villager Attacking. Makes villagers fight back
* Added experimental Get Over Here ability to Enderman. Disabled by default as I'm not happy how it is.
* Fixed zombies stopping mining if the player was less than 4 blocks from them
* Slightly increased Pearl Zombies Cooldown (~~4~~ -> 5 seconds)
* Fisher zombies now retrieve hooks faster (~~2~~ -> 1.5 seconds)

## 1.6.2
* Creeper cena explosion size can now be configured
* Fixed Tnt like creepers not obeying the config option

## 1.6.1
* Creeper cena explosion no longer generates fire by default
* Digger Zombies will now less likely stop mining
* Avoid explosion run speed can now be configured. Also reduced the running speed
* Dark art witches no longer trigger if the target is a raider
* Dark Art Witches are no longer immune when performing the ritual, instead, they have a strong resistance (80% damage reduction)
* Dark Art Witches' spawned villagers are now killed if the Witch dies
* Fixed Witches throwing good potions at any non-player entity

## Beta 1.6.0
* XRay follow range is now a separate attribute and can be overridden separately from vanilla follow range
* Most mobs data (e.g. if a zombie is a miner or the inaccuracy of a skeleton) are now loaded from NBT in `ForgeData` (if not present will use default values from config).  
  You can find all the Tags here: https://github.com/Insane96/EnhancedAI/wiki/Entity-Tags
* Fixed Fisher zombies fishing only if they couldn't see the target
* Melee Attacks Attribute Based is now disabled by default
* Fixed crash when the owner of fishing hook died / transformed

## 1.5.1
* Added Melee Attacks Attribute Based. Makes mob attack distance based off `forge:attack_range` attribute. Defaults to vanilla attack range, can be changed by modifying the `forge:attack_range` attribute.
* Fixed witches having wrong config for potion effects, causing duration and amplifier being swapped. You need to regen the config or fix the potion effects by swapping the last two numbers.
* Fixed Zombies spawning with Fishing rod client side.
* Fixed Fisher zombies fishing even if they couldn't see the target

## Beta 1.5.0
* Added Ghast Shoot feature 
  * Ghasts can now shoot up to 3 fireballs.
  * Ghasts can now shoot slightly faster or slightly slower.
  * Ghasts can keep shooting even if can no longer see the target, will also shoot 4x faster when can't see the target to breach
* Added Fishing Zombies
  * Zombies have 7% chance to spawn with a fishing rod and will be able to reel in players
* Added Drowned Swimming feature
  * Makes drowned swim speed based off `forge:swim_speed` attribute instead of `minecraft:generic.movement_speed`  
    Basically are no longer reaaaally slow
* Added an option to make mobs find a better path to the target
* Moved Swim speed multiplier to Movement feature
* Pearler Zombies' inaccuracy can now be configured
* Allow Climbing and Target Ladders are now disabled by default
* Entities with higher follow range than the override are no longer overridden (e.g. Ghasts)
* Creepers now start exploding when nearer to the target (explosion radius ~~x1.5~~ -> x1.35 blocks)
* Cleaned up Creeper Module and AI, should perform slightly better
* Hiding no longer let mobs lose the target
* Fixed a bug where the vanilla targeting AI was not removed causing mobs to be confused at times

## 1.4.6
* Increased Dark Art Witch chance
* Dark art witches no longer start the ritual if the target can't see them
* Fixed Diggers begin able to insta-break indestructible blocks

## 1.4.5
* Requires InsaneLib 1.5.1
* Creepers should now stop exploding if the target dies

## Beta 1.4.4
* Fixed possible crash with Digger AI
* Fixed digger not dropping blocks broken

## Beta 1.4.3
* Requires InsaneLib 1.5.0
* Split skeleton shoot and skeleton flee in two separate features
* Thrown webs cooldown is now between 40 and 60 ticks
* Added entity Blacklist to Animals Attacking Feature
* Invisible witches now try to run in a random direction.
* Fixed disabling InstaTarget having no effect 
* Config is now alphabetically ordered (with Base Module on top) (need to regen config for this to take effect)

## 1.4.2
* Witches
  * Added Dark Art Witches Feature. When approaching a Dark Art Witch, she'll use a Villager spawn egg and call a lightning bolt on it.
  * Invisibility potions are now a Throwing feature
  * Witches no longer drink strong healing potions by default. They'll only drink it if are below certain health
  * Reduced flee speed
  * Reduced Slow Falling duration when witches use it
* Enemy animals (such as Hoglins) no longer gain the Animal attacking AI and bonus knockback

## 1.4.1
* Witch
  * Added Thirsty Witches feature. Witches drink more potions and milk if they have a negative effect.
  * Potion Throwing
    * Witches are now able to use a slow falling potion on themselves if falling for more than 8 blocks
    * Slowed down witches throw speed and reduced back-to-back attack chance
* Added Entity Blacklist to Avoid Explosion Feature
* Miner Zombies 
  * No longer keep mining if they can reach the target
  * Added a max distance from target to mine config option. Disabled by default

## 1.4.0
* Added Witch Module
  * Added Witch Potion Throwing Feature. Witches throw potions farther, faster and more potion types. Also, no longer chase player if they can't see him. 15% chance for potions to be lingering instead of splash. 25% chance to throw a potion back to back.
  * Added Witch Flee Target Feature. Witches run away from players when near them. Have 50% chance to be able to throw potions while fleeing
  * Witches no longer stop when drinking potions, instead they now move slower
* Added Movement features
  * Mobs will now try to target climbable blocks and use them to reach the target
* Increased digger chance (~~5%~~ -> 7%)
* Halved animal knockback
* Fixed a potential crash when mods don't register the knockback attribute for animals
* Fixed zombies spawning with Ender Pearls and wooden pickaxes client-side

## 1.3.3
* Increased XRay Chance (~~15%~~ -> 20%)
* Increased animal attack damage (3 -> 4) and increased knockback
* Fixed disabling Miner Zombies Wooden pick not disabling it

## 1.3.2
* Added max Y coordinate at which zombies can mine, sea level by default
* Skeletons now shot slower when not in hard difficulty
* Reduced Follow range override (~~64~~ -> 48)
* Reduced Skeleton Spammer chance (~~10%~~ -> 7%)
* Set drop chance of mining zombies wooden pickaxe to 0
* Fixed Mining Zombies having pickaxe in wrong hand
* Fixed another potential crash with mining zombies

## 1.3.1
* Animals movement speed when attacking is now configurable
* Digger Zombies now get equipped with a Wooden Pickaxe
* Fixed skeletons not using the correct arrow (e.g. strays no longer shot slowness arrows)
* Fixed potential crash with Digging zombies

## Beta 1.3.0
* Added Animal Module
  * Added Animal Attacking Feature. Make animals fight back and no longer flee when attacked
* Added Swim Speed Multiplier to Targeting
* Added Blaze Module
  * Added Blaze Attack Feature. Blazes can fire multiple fireballs per shot, more fireballs, with less cooldown and with better aim
* Added Anti-Cheese Feature, monsters can no longer be boated or minecarted
* Skeleton
  * Added 10% chance for a skeleton to be a spammer. Shots an arrow every 0.5 seconds dealing 1/4 damage and begin twice as inaccurate
  * Flee Distance changed (12 -> 16 blocks; 7 -> 8 blocks faster flee) and now configurable
* Zombie
  * Zombie Pearlers no longer need to be on ground to pearl
  * Improved Digger Zombies, they now keep mining multiple blocks before stopping, and they should get stuck less
* Reduced XRay Chance
* Increased mobs despawning distance
* Fixed spiders throwing webs 4x slower

## 1.2.4
* Port to 1.18.2, requires InsaneLib 1.4.6
* Spider Webs are no longer affected by mob griefing, and can also now replace simple blocks like grass
* Fixed skeletons running faster from the player when farther instead of near
* Fixed spiders having wrong config options for Web Throwing
* Fixed spiders having wrong fall damage reduction (was 10% instead of 90%)

## 1.2.3
* Monsters will now be able to despawn when farther than any player by 48 blocks (vanilla is 32) and will instantly despawn when farther away than 80 blocks (vanilla is 128) (configurable in "Base Feature")
* Breaching creepers now breach more often
* Launching creepers that go on cooldown now have higher cooldown each time they fail
* Zombie Pearlers now Pearl from min 6 blocks instead of 12
* Fixed missing Spider Misc Feature

## 1.2.2
* Now requires InsaneLib 1.4.1
* Added Spider Misc Feature
  * Spiders now take -90% fall damage
* Spider Cobweb Throwing
  * Cobwebs placed will now disappear after 5 seconds (configurable)
  * Cobweb damage is now configurable and scales with difficulty
  * Cobweb cooldown is now configurable (also sped up, 3 -> 2.5 seconds before shoting)
  * Min and max attack distance is now configurable
  * Slowness 
    * Is now configurable
    * Is no longer applied from thrown webs if the player parrys the projectile
    * Increased Slowness applied to hit entities (5 seconds of one level of slowness stacking up to V -> 6 seconds of 2 levels of slowness stacking up to VI)
* Launching + Breaching creepers will now launch and try to breach walls, exploding on contact with walls
* Slightly adjusted throwing web and skeleton arrows shooting angle
* Added ability tags to all mobs. This means that exiting and re-entering the world (or chunks) will no longer change the mob's abilities (e.g. Creepers with breaching ability will have the ForgeData.enhancedai:breach tag set to 1b)
* skeletons running from target's speed is now configurable, also fixed having faster speed when far away instead of when near
* Fixed skeletons not taking into account config inaccuracy
* Fixed breaching only working when creepers were in water
* Fixed spiders throwing webs even they could not see the player
* Fixed skeletons not strafing at all

## Alpha 1.2.1
* Fixed server startup crash

## Alpha 1.2.0
* Added Spider Web Throw. Spiders with this ability will throw Cobwebs at the target, stackingly slowing him down or, if missed, places a cobweb at the block hit
* Added Zombie Pearlers. Zombies with and Ender Pearl in the hand or off-hand will throw it to get closer to the player
* Launching creepers now cancel the explosion if they didn't reach the target. If that's the case, the launching ability will go on a 6 seconds cooldown
* Launching creepers will no longer launch if they're less than 12 blocks away from the target
* Skeletons now have 50% chance to not be able to shoot the player when avoiding them
* Skeletons now have 50% chance to gain back the strafing ability
* Skeletons no longer shot the player when farther than their attack distance

## 1.1.2
* Fixed crash with Moisth server
* Fixed mod badly crashing the game at startup

## 1.1.1
* Added Entity Blacklist for skeletons shoot ai and zombies digger. Skeleton Shoot AI blacklist has Quark's fallen by default
* Arrow inaccuracy for skeletons now defaults to 2 (from 0)
* Skeletons no longer always shoot from 64 blocks (64 -> 24 to 48 blocks)
* Skeletons avoiding target chance has been reduced (every skeleton -> 50% of skeletons)
* Reduced even further skeleton avoid speed from target

## 1.1.0
* Requires InsaneLib 1.2.1
* Added Tool only and proper tool only options to zombie digger
* Added Digging Speed Multiplier for digger zombies
* Added "Blacklist Tile Entities" for Digger Zombies. Will prevent Zombies from breaking tile entities
* Added Block Black/Whitelist to zombie diggers
* Added Entity Black/Whitelist for the targeting feature
* Zombies no longer start mining as soon as their path is finished
* Creeper Cena explosion radius has been increased back to 6 instead of 5
* Increased Creeper Breaching (5% -> 7.5%) and Creeper Cena (1% -> 2%) chance 
* Fixed break animation staying after a zombie broke a block
* Fixed mod not requiring InsaneLib thus crashing instead of giving "missing dependency" message

## 1.0.4
* Fixed server crash on skeleton spawn
* Zombies now consume less bandwidth when mining

## Beta 1.0.3
* Zombies can now mine. 5% chance to spawn with the miner AI with which they can break blocks to reach the player. If they have the right tools in the *Off Hand* then they'll mine faster.
* Fixed Mobs running away from explosion seemingly losing AI if the target died
* Creepers will no longer try to breach when in water
* Creepers now stop the explosion when breaching if the target moves away.
* Creepers now stop the explosion when launching if the target dies
* Reduced avoid speed of explosions

## Alpha 1.0.2
* Creepers now Swell from the correct distance
* Increased Breach range (10 -> 12 blocks from the player)
* Reduced speed at which skeletons avoid players
* Mobs now try to avoid TnTs explosions

## Alpha 1.0.1
* 2% chance for creepers to spawn with the Launch ability. When in range (based off their explosion size) they will ignite and throw themselves to the player
* Fixed skeletons getting crazy when swapping weapons
* Fixed a few more AI craziness
* Fixed Cena Creepers not emitting the cena fuse sound when ignited not by the AI
* Reduced xray chance (25% -> 20%)
* Fixed Follow Range not begin applied to AI

## Alpha 1.0.0
First release