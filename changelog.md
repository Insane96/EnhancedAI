# Changelog

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
  * Drinkable and throwable potions list in the config now accepts vanilla potions****

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