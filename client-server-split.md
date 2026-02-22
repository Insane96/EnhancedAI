# EnhancedAI – Server / Client-Extra Split Analysis (NeoForge 1.21.1 Port)

## General concept

| Mod | Who needs it | Purpose |
|-----|--------------|---------|
| **EnhancedAI** (server-side) | Server only (vanilla clients can connect) | All AI changes, mob behavior, pathfinding |
| **EnhancedAI Extra** (client-required) | Both server and client | Features that use custom entities visible to clients |

For vanilla clients to connect to the server-side mod, in NeoForge 1.21.1 `mods.toml` must have `displayTest = "IGNORE_SERVER_VERSION"`. This means the mod **cannot register custom entity types that are tracked by clients** (the client would not know how to handle them).

---

## Features that stay in the Server-Side mod

All the following features modify exclusively server-side logic (AI goals, attributes, mixins to server classes) and require no code on the client.

### Mobs (general)
- **AirSteal** – Mobs steal air from entities underwater
- **VehicleAntiCheese** – Mobs break vehicles (boat, minecart) to reach their target
- **AvoidExplosions** – Mobs run away from exploding Creepers/TNT
- **BitingMobs** – Mobs bite when hit with non-weapon items
- **ActualBlindness** – Blindness reduces mobs' follow range
- **BreakAnger** – Mobs get angry when a nearby block is broken
- **Climbing** – Mobs can climb ladders and climbable blocks
- **FallingShockwave** – Shockwave when a mob falls at least 3 blocks
- **FireImmuneTicks** – Fire immunity mechanics for mobs
- **FleeTarget** – Mobs flee from their target
- **ItemDisruption** – Mobs interfere with items held by the player
- **Jump** – Mobs can jump in place
- **Leaders** – Group leader mechanics
- **MeleeAttacking** – Enhanced melee attacking
- **MinerMobs** – Mobs mine blocks to reach their target
- **Panic** – Mobs panic when on fire
- **Parkour** – Mobs can parkour
- **Pathfinding** – Improved pathfinding (ladders, doors, etc.)
- **PearlerMobs** – Mobs can use Ender Pearls to teleport
- **PickUpAndThrow** – Mobs pick up and throw entities
- **PushResistance** – Push resistance mechanics
- **RandomStroll** – Improved random strolling
- **Riding** – Mobs can ride other mobs
- **Shielding** – Enhanced shield mechanics
- **Spawning** – Spawn restrictions and modifications
- **Sprint** – Mobs sprint toward their target
- **Swimmers** – Improved swimming mechanics
- **TeleportAntiCheese** – Anti-cheese for endermen/teleporting mobs
- **TeleportToTarget** – Mobs teleport to their target
- **MPRDataPack** – Mobs Properties Randomness datapack integration

### Animals
- **AnimalScaredAttack** – Animals fight back or flee
- **AnimalsPanic** – Animals panic when one of them is attacked
- **NotTemptedAnimals** – Animals are not attracted by food

### Blaze
- **BlazeAttack** – Faster/more fireballs for Blazes

### Bugs (Silverfish)
- **SilverfishMergeWithStone** – Silverfish hide inside blocks
- **SilverfishWakeUpFriends** – Silverfish wake up friends

### Creeper
- **DisableFallingSwelling** – Creepers no longer ignite when falling
- **CreeperLaunch** – Creepers launch into the air when exploding
- **CreeperSwell** – Various changes to Creeper ignition
- **TNTLike** – Creepers ignite when taking explosion damage

### Drowned
- **BetterDrownedSwimUp** – Improved swim-up goal with leap capability
- **DrownedAttackDuringDay** – Drowned attack during daytime
- **DrowningTargets** – Mobs drag their target underwater
- **SunResistantDrowned** – Drowned are temporarily immune to sunlight

### Ghast
- **GhastShooting** – Improved Ghast shooting

### Illager
- **RavagerFeature** – Block tag for blocks breakable by Ravagers
- **PillagerShoot** – Improved Pillager shooting

### Shulker
- **ShulkerArmor** – Enhanced Shulker armor
- **ShulkerAttack** – Enhanced Shulker attack
- **ShulkerBullets** – Shulker bullet customization

### Skeleton
- **SkeletonFleeTarget** – Skeletons keep their distance from the target
- **SkeletonShoot** – Improved Skeleton shooting
- **WitherSkeletons** – Wither Skeleton modifications

### Slime / Magma Cube
- **MagmaCubeSurfSpeed** – Magma Cube surface speed
- **SlimeAttackFix** – Fix Slimes dealing damage every tick
- **SlimeJumpDelay** – Slime jump delay mechanics
- **SlimeSize** – Enhanced Slime size mechanics

### Snow Golem
- **SnowballsInvulnerabilityFrames** – Snowball invulnerability frames
- **SnowGolemsDamagingSnowballs** – Snow Golems throw damaging snowballs
- **SnowGolemsFreezingSnowballs** – Snow Golems throw freezing snowballs
- **SnowGolemsHealedBySnowballs** – Snow Golems healed by snowballs

### Spider
- **DestuckWallGoal** – Spider unstuck from walls goal
- **StuckFix** – General spider stuck fixes

### Villager
- **VillagerAlertProtectors** – Villagers alert Golems
- **VillagerAttacking** – Enhanced Villager attacking

### Warden
- **WardenDarknessRange** – Darkness effect range
- **WardenListenRange** – Listen range
- **WardenSonicBoomRange** – Sonic Boom range

### Witch
- **AlliedMonsters** – Witches target monsters allied with the player
- **DarkArt** – Dark art mechanics for Witches
- **ThirstyWitches** – Witches drink water potions
- **WitchPotionThrowing** – Enhanced Witch potion throwing

---

## In-depth analysis: can FisherMobs and ThrowingWeb be made server-side?

### ThrowingWeb – `FallingBlockEntity` as an alternative

**Answer: Yes, technically feasible.**

`FallingBlockEntity` is a vanilla entity type, already registered and rendered correctly by the vanilla client without any mod. It would render as a flying cobweb block, which makes visual sense.

**What `ThrownWebEntity` needs to do:**
1. Fly toward the target with a ballistic trajectory (initial velocity + gravity)
2. Hit an entity → damage, effects, optional cobweb placement
3. Hit a block → optional cobweb placement

**What `FallingBlockEntity` can cover:**

| Behavior | ThrownWebEntity | FallingBlockEntity |
|---|---|---|
| Ballistic trajectory | `shoot()` + `ThrowableItemProjectile.tick()` | `setDeltaMovement()` + gravity per tick → **identical** |
| Block collision | `onHitBlock()` | Already handled natively by `move()` |
| Cobweb placement on block hit | Manual in `onHitBlock()` | Native: vanilla places the block on landing (with `dropItem = false`) |
| Entity collision | `onHitEntity()` | **Missing** → needs a mixin to `tick()` for manual AABB check |
| Owner + damage | Class fields | Persistent NBT (`persistentData`) or a server-side map |
| Identifying "our" blocks | Different entity type | Custom NBT tag (e.g. `enhancedai:owner_uuid`) |

**Required mixin on `FallingBlockEntity.tick()`:**
```java
// pseudo-logic to add via mixin
if (this.getPersistentData().contains("enhancedai:owner_uuid")) {
    AABB expanded = this.getBoundingBox().inflate(0.1);
    List<Entity> hit = level.getEntities(this, expanded, e -> e != owner && e.isAlive());
    if (!hit.isEmpty()) {
        // apply damage + effects + cobweb
        this.discard();
    }
}
```

**Downsides:**
- Renders as a flying cobweb block, not an item projectile (visually acceptable)
- Mixin on `FallingBlockEntity` is more prone to conflicts with other mods
- Physics differ slightly: `FallingBlockEntity` applies drag (`0.98`) but lacks the `normalize + speed` of `shoot()` → trajectory needs recalibration in `WebThrowGoal`

**Conclusion**: Feasible to keep ThrowingWeb in the server-side mod without any client code.

---

### FisherMobs – Mixin on vanilla `FishingHook` to accept `LivingEntity`

**Answer: Functional server-side, but rendering remains a problem.**

**Core issue with vanilla `FishingHook`:**
Vanilla `FishingHook` has a `private final Player player` field referenced throughout `tick()` and `retrieve()`. Making it work with a mob owner via mixin would require:
1. Adding a `@Unique Entity livingOwner` field via mixin
2. `@Redirect` on every access to `this.player` inside `tick()` — non-trivial given the number of references
3. Bypassing the constructor that requires a `Player`

Technically doable but very fragile and breaks with every vanilla update.

**Rendering problem:**
The vanilla `FishingHookRenderer` does:
```java
public void render(FishingHook hook, ...) {
    Player player = hook.getPlayerOwner(); // returns null if owner is not a Player
    if (player == null) return; // ← nothing renders if owner is a mob
    // draws the line from the player's arm...
}
```
Even if the server side were fixed, vanilla clients would see the hook **with no line** connecting it to the mob.

**Options to make the line visible:**
- Mixin `FishingHookRenderer` on the client → still requires client code (Extra mod)
- Use the custom `FishingHook` + own `FishingHookRenderer` (current approach)

**Conclusion**: Mixin-ing vanilla `FishingHook` is more complex than the custom solution, and the fishing line still requires client code. FisherMobs stays in the Extra mod.

---

## Features that go in the Extra mod (client-required)

These features use **custom entities tracked by clients** and therefore require registration and rendering on the client.

### FisherMobs – Mobs that fish players
- **Entity**: `FishingHook` (extends `Projectile`)
  - `clientTrackingRange(4)` → synced to clients
  - Client must know the type to avoid crashes
- **Renderer**: `FishingHookRenderer` (`@OnlyIn(Dist.CLIENT)`)
  - Renders the hook + fishing line toward the mob
- **Goal**: `FishingTargetGoal`, server-side `FishingHook` logic
- **Registration**: `EAIEntities.FISHING_HOOK`

**Reason**: A vanilla client connecting to a server with this feature receives spawn packets for an unknown entity type → potential disconnect or errors. The renderer requires client code.

### ThrowingWeb – Mobs that throw cobwebs
- **Entity**: `ThrownWebEntity` (extends `ThrowableItemProjectile`)
  - `setTrackingRange(4)` → synced to clients
- **Renderer**: `ThrownItemRenderer` (vanilla), registered in `ClientSetup`
  - Even though the renderer is vanilla, the **registration** happens on the client in `ClientSetup`
- **Registration**: `EAIEntities.THROWN_WEB`

**Reason**: Same unknown entity type issue as FisherMobs. The renderer registration is in `ClientSetup`.

### CreeperRendererMixin (future)
- Currently **fully commented out** (no effect)
- If re-enabled: mixin to `CreeperRenderer` (client-only class) → goes in the Extra mod
- Original purpose: modify the Creeper's visual scale during swelling

---

## Files to move to the Extra mod

```
src/main/java/insane96mcp/enhancedai/
├── setup/
│   └── ClientSetup.java                          → EXTRA
├── mixin/
│   └── CreeperRendererMixin.java                 → EXTRA
└── modules/mobs/
    ├── fisher/
    │   ├── FishingHook.java                      → EXTRA
    │   ├── FishingHookRenderer.java              → EXTRA
    │   ├── FishingTargetGoal.java                → EXTRA
    │   └── FishingHookAI logic...                → EXTRA
    └── webber/
        └── ThrownWebEntity.java (registration)   → EXTRA
            (server logic stays in server mod, renderer/registration in Extra)
```

> **Note**: `ThrownWebEntity` could be split: the behavior logic (placing cobweb on hit) stays in the server mod, but the **entity type registration** must be common. Alternatively, the Extra mod depends on the server mod and re-registers the renderer.

---

## Mixin configs

The `mixins.enhancedai.json` file already separates:
- `"mixins"` → server-side (all ~31 current mixins)
- `"client"` → `CreeperRendererMixin` (currently commented out)

For the Extra mod, a new `mixins.enhancedai_extra.json` is created for client-only mixins.

---

## Summary

| | Server-Side Mod | Extra Mod |
|---|---|---|
| **Client requirement** | None (vanilla clients ok) | Must be installed on the client too |
| **Feature count** | ~70 | ~2 (+CreeperRenderer if re-enabled) |
| **Custom entities** | None | FishingHook, ThrownWebEntity |
| **Renderers** | None | FishingHookRenderer, ThrownItemRenderer reg. |
| **Client mixins** | None | CreeperRendererMixin |
| **Dependencies** | InsaneLib, (MPR optional) | InsaneLib + EnhancedAI server mod |
