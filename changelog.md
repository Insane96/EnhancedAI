# Changelog

## 1.8.5
* Fixed server hanging if skeletons distance from target was 0

## 1.8.4
* Digger Zombies will no longer mine if the `LivingDestroyBlockEvent` is canceled

## 1.8.3
* Backported 1.20.2 attack reach change [https://minecraft.wiki/w/Java_Edition_1.20.2#Mobs]
  * This is automatically applied if the Attacking feature is enabled
  * Melee Attacks Attribute Based is now enabled by default as it works much better thanks to the backport

## 1.8.2
* Fixed some target goal selectors not being copied to mod's target goal
  * This might fix some mobs attacking for no reason

## 1.8.1
* Fixed level's `RandomSource` causing crashes
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