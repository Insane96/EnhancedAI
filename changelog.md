# Changelog

## Upcoming
* Added Prevent Infighting. Mobs no longer attack eachother
* Villagers Attacking
  * No longer attack enemies (can be configured)
  * No longer attack players with high enough reputation (defaults to Iron Golem)
* Spiders Throwing Webs
  * Cave Spiders now also apply poison when hitting entities (configurable)
  * Added config option to place a cobweb on the entity hit by the thrown web
  * Added config option to disable slowness effect correctly
  * Reduced max slowness level (~~VI (6)~~ -> IV (4))
* Witch Potion Throwing
  * Halved Weakness potion duration (~~1:30~~ -> 0:45)
* Animal knockback can now be configured
* Drowned now go in Swimming pose when swimming, quite ugly as they don't support the swimming animation fully

## Beta 1.7.1
* Requires InsaneLib 1.7.1

## Beta 1.7.0
* Ported to 1.19.1+, requires InsaneLib 1.7.0
* Added Shielding Feature (mobs will rise shields when the target is near them), disabled by default
* Decreased run speed when running from explosions
* Increased Blaze Time Between Fireballs (~~3\~6~~ -> 4~10 ticks)