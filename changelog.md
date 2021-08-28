# Changelog

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