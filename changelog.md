# Changelog

## Upcoming
* Now requires InsaneLib 1.4.0
* Spider Cobweb Throwing
  * Cobweb damage is now configurable and scales with difficulty
  * Cobwebs placed will now disappear after 5 seconds (configurable)
* Slowness is no longer applied from thrown webs if the player parrys the projectile
* Fixed spiders throwing webs even they could not see the player
* Fixed skeletons running from target having faster speed when far away instead of when near. Also made those speeds configurable.
* Fixed skeletons not strafing at all

## Alpha 1.2.1
* Fixed server startup crash

## Alpha 1.2.0
* Added Spider Web Throw. Spiders with this ability will throw Cobwebs at the target, stackingly slowing him down or, if missed, places a cobweb at the block hit
* Added Zombie Pearlers. Zombies with and Ender Pearl in the hand or off hand will throw it to get closer to the player
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