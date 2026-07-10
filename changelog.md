## Upcoming
* Fixed Spider slowdown not working

## 4.2.0.1
* Fixed Wolves and Cats still attacking animals when tamed
* Crash fix with Biting Mobs and Falling Shockwave

## 4.2.0.0
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

## 4.1.2.0
* Targeting
  * Mobs now have 50% chance to forget the target if they can't sense it (sight, xray or glowing) after 15s
* Leader mobs
  * Reinforcements now spawn even if non-entities damage them
  * Added loot table enhancedai:leader_mob that will drop on leaders death. Empty by default
  * Lowered armor
* Fixed mobs pathing getting stuck sometimes if the path was the same as before

## 4.1.1.2
* Lowered xray range
* Fixed crash with latest InsaneLib

## 4.1.1.1
* MPR Data Pack
  * Zombies now have more chance to spawn as miners only in caves

## 4.1.1.0
* Added Enderman module
  * Added Look Anger Cone
    * Makes it easier to anger endermen by looking at them
* Fixed wrong quartz tag for Break anger

## 4.1.0.2
* MPR Data Pack
  * Mobs that spawn in caves now have increased max health and xray follow range (up to +100% at bedrock level)
  * Mobs that spawn outside (when not in full moon) will have lower follow range, xray follow range and movement speed
* Decreased default xray follow range
* Fixed fishing hook not rendering correctly

## 4.1.0.1
* Lowered max follow range override and increased min xray range
* Added Shields+'s copper shield to chances to block
* Skeletons can no longer sprint
* With MPR data pack, zombies and skeletons have less chance to spawn with equipment and to be enchanted
* Fixed wolves having twice as much health

## 4.1.0.0
The mod is no longer server side only
* Added back Fisher Mobs and Web Throwers
* Added back Angry Creepers sound effects
* Added back Custom Targeting
  * Skeleton and Zombie horses are now hostile towards the player
* Skeleton horses can no longer be neutral and scared by players (from Animals scared attack feature), but have been added to Custom Targeting

## 4.0.1.0-beta
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

## 4.0.0.4-beta
* Jump in place now mobs jump only if the target is at least the height of the mob higher (e.g. zombies will jump if the target is at least 1.95 blocks higher)
* Fixed mobs spawned by leaders attacking creative players
* Fixed more crashes with Miner Mobs missing attributes
  * I hate you Mojank

## 4.0.0.3-beta
* Moved Push Resistance feature to InsaneLib

## 4.0.0.2-beta
* Removed Biting mobs and Falling Shockwave features since they require client-sided mod

## 4.0.0.1-beta
* Enabled Bogged, Breeze and Armadillo to use some features
* Fixed crash with Miner mobs not having the block break speed attribute
* Fixed zombie villagers not affected by some features

### 4.0.0.0-beta
Ported to 1.21.1

* Jump in place now makes mobs jump only if the target is at least one block higher
* Updated MPR data pack with smaller spiders and disabled baby zombies