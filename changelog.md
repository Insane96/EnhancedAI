# Changelog

## Upcoming
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
* Fixed XRay mobs targeting players from far far away

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
* Added Prevent Infighting. Mobs no longer attack eachother
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