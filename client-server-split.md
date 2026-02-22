# EnhancedAI – Analisi Split Server / Client-Extra (Port a NeoForge 1.21.1)

## Concetto generale

| Mod | Chi deve averla | Scopo |
|-----|----------------|-------|
| **EnhancedAI** (server-side) | Solo il server (i client vanilla possono connettersi) | Tutte le modifiche all'AI, comportamento mob, pathfinding |
| **EnhancedAI Extra** (client-required) | Sia server che client | Feature che usano entità custom visibili ai client |

Per far sì che un client vanilla possa connettersi alla server-side mod, in NeoForge 1.21.1 il `mods.toml` deve avere `displayTest = "IGNORE_SERVER_VERSION"`. Questo implica che la mod **non può registrare entity type custom che vengono tracciati dai client** (il client non saprebbe come gestirli).

---

## Features che rimangono nella mod Server-Side

Tutte le seguenti feature modificano esclusivamente logica server-side (AI goals, attributi, mixin a classi server) e non richiedono alcun codice sul client.

### Mobs (generali)
- **AirSteal** – I mob rubano l'aria alle entità sott'acqua
- **VehicleAntiCheese** – I mob rompono veicoli (barca, minecart) per raggiungere il target
- **AvoidExplosions** – I mob scappano da Creeper/TNT che esplodono
- **BitingMobs** – I mob mordono se colpiti con oggetti non-weapon
- **ActualBlindness** – La cecità riduce il follow range dei mob
- **BreakAnger** – I mob si arrabbiano quando si rompe un blocco vicino
- **Climbing** – I mob scalano scale e blocchi arrampicabili
- **FallingShockwave** – Shockwave quando un mob cade per almeno 3 blocchi
- **FireImmuneTicks** – Gestione dell'immunità al fuoco
- **FleeTarget** – I mob fuggono dal target
- **ItemDisruption** – I mob interferiscono con gli oggetti in mano al giocatore
- **Jump** – I mob possono saltare sul posto
- **Leaders** – Meccanica del leader del gruppo
- **MeleeAttacking** – Attacco corpo a corpo migliorato
- **MinerMobs** – I mob minano blocchi per raggiungere il target
- **Panic** – I mob entrano in panico quando in fiamme
- **Parkour** – I mob fanno parkour
- **Pathfinding** – Pathfinding migliorato (scale, porte, etc.)
- **PearlerMobs** – I mob usano Ender Pearl per teletrasportarsi
- **PickUpAndThrow** – I mob raccolgono e lanciano entità
- **PushResistance** – Resistenza alla spinta
- **RandomStroll** – Vagabondaggio casuale migliorato
- **Riding** – I mob cavalcano altri mob
- **Shielding** – Meccanica scudo migliorata
- **Spawning** – Restrizioni e modifiche allo spawn
- **Sprint** – I mob scattano verso il target
- **Swimmers** – Nuoto migliorato
- **TeleportAntiCheese** – Anti-cheese per enderman/mob che si teletrasportano
- **TeleportToTarget** – I mob si teletrasportano sul target
- **MPRDataPack** – Integrazione con Mobs Properties Randomness

### Animali
- **AnimalScaredAttack** – Gli animali contrattaccano o fuggono
- **AnimalsPanic** – Gli animali entrano in panico quando uno viene attaccato
- **NotTemptedAnimals** – Gli animali non sono attirati dal cibo

### Blaze
- **BlazeAttack** – Attacco del Blaze più rapido / più palle di fuoco

### Bugs (Silverfish)
- **SilverfishMergeWithStone** – I Silverfish si nascondono nei blocchi
- **SilverfishWakeUpFriends** – I Silverfish risvegliano gli amici

### Creeper
- **DisableFallingSwelling** – I Creeper non si innescano cadendo
- **CreeperLaunch** – I Creeper si lanciano in aria durante l'esplosione
- **CreeperSwell** – Modifiche varie all'innesco dei Creeper
- **TNTLike** – I Creeper si innescano con danni da esplosione

### Drowned
- **BetterDrownedSwimUp** – Nuoto verso l'alto migliorato con salto
- **DrownedAttackDuringDay** – I Drowned attaccano anche di giorno
- **DrowningTargets** – I mob trascinano il target sott'acqua
- **SunResistantDrowned** – I Drowned resistono temporaneamente alla luce solare

### Ghast
- **GhastShooting** – Sparo del Ghast migliorato

### Illager
- **RavagerFeature** – Tag blocchi rompibili dal Ravager
- **PillagerShoot** – Tiro del Pillager migliorato

### Shulker
- **ShulkerArmor** – Armatura dello Shulker migliorata
- **ShulkerAttack** – Attacco dello Shulker migliorato
- **ShulkerBullets** – Personalizzazione dei proiettili Shulker

### Skeleton
- **SkeletonFleeTarget** – Gli Skeleton mantengono distanza dal target
- **SkeletonShoot** – Tiro dello Skeleton migliorato
- **WitherSkeletons** – Modifiche allo Wither Skeleton

### Slime / Magma Cube
- **MagmaCubeSurfSpeed** – Velocità in superficie delle Magma Cube
- **SlimeAttackFix** – Fix danni ogni tick degli Slime
- **SlimeJumpDelay** – Ritardo nei salti degli Slime
- **SlimeSize** – Dimensioni degli Slime migliorate

### Snow Golem
- **SnowballsInvulnerabilityFrames** – Frame di invulnerabilità palle di neve
- **SnowGolemsDamagingSnowballs** – Palle di neve che danneggiano
- **SnowGolemsFreezingSnowballs** – Palle di neve che congelano
- **SnowGolemsHealedBySnowballs** – Snow Golem curati dalle palle di neve

### Spider
- **DestuckWallGoal** – Spider che si destucca dalle pareti
- **StuckFix** – Fix generali per spider bloccati

### Villager
- **VillagerAlertProtectors** – I Villager allertano i Golem
- **VillagerAttacking** – Attacco dei Villager migliorato

### Warden
- **WardenDarknessRange** – Raggio dell'effetto Darkness
- **WardenListenRange** – Raggio di ascolto
- **WardenSonicBoomRange** – Raggio del Sonic Boom

### Witch
- **AlliedMonsters** – Le Witch prendono di mira i mostri alleati del player
- **DarkArt** – Meccaniche "dark art" per la Witch
- **ThirstyWitches** – Le Witch bevono pozioni d'acqua
- **WitchPotionThrowing** – Lancio pozioni della Witch migliorato

---

## Analisi approfondita: possibile rendere server-side FisherMobs e ThrowingWeb?

### ThrowingWeb – `FallingBlockEntity` come alternativa

**Risposta: Sì, tecnicamente fattibile.**

`FallingBlockEntity` è un entity type vanilla, già registrato e renderizzato correttamente dal client vanilla senza nessuna mod. Renderebbe come un blocco di cobweb in volo, il che ha senso visivamente.

**Cosa deve fare `ThrownWebEntity`:**
1. Volare verso il target con traiettoria balistica (velocità iniziale + gravità)
2. Colpire entità → danni, effetti, cobweb opzionale
3. Colpire blocchi → cobweb opzionale

**`FallingBlockEntity` può fare tutto questo:**

| Comportamento | ThrownWebEntity | FallingBlockEntity |
|---|---|---|
| Traiettoria balistica | `shoot()` + `ThrowableItemProjectile.tick()` | `setDeltaMovement()` + gravità tick-by-tick → **identico** |
| Collisione con blocchi | `onHitBlock()` | Già gestito nativamente dal `move()` |
| Posizionamento cobweb su blocco | Manuale in `onHitBlock()` | Già nativo: vanilla piazza il blocco al landing (con `dropItem = false`) |
| Collisione con entità | `onHitEntity()` | **Mancante** → serve mixin a `tick()` per AABB check manuale |
| Owner + damage | Campi della classe | NBT persistente (`persistentData`) o mappa server-side |
| Identificazione "nostra" | Tipo entità diverso | NBT tag custom (es. `enhancedai:owner_uuid`) |

**Mixin necessario su `FallingBlockEntity.tick()`:**
```java
// pseudo-logica da aggiungere via mixin
if (this.getPersistentData().contains("enhancedai:owner_uuid")) {
    AABB expanded = this.getBoundingBox().inflate(0.1);
    List<Entity> hit = level.getEntities(this, expanded, e -> e != owner && e.isAlive());
    if (!hit.isEmpty()) {
        // applica danni + effetti + cobweb
        this.discard();
    }
}
```

**Svantaggi:**
- Il rendering è un blocco di cobweb che vola, non un item projectile (ma è visivamente accettabile)
- Il mixin a `FallingBlockEntity` è più soggetto a conflitti con altre mod
- La fisica differisce leggermente: `FallingBlockEntity` applica drag (`0.98`) ma non ha il `normalize + speed` di `shoot()` → la traiettoria va ricalibrata nel `WebThrowGoal`

**Conclusione**: Fattibile per avere ThrowingWeb nella server-side mod senza client.

---

### FisherMobs – Mixin su vanilla `FishingHook` per accettare `LivingEntity`

**Risposta: Fattibile per la funzionalità, ma il rendering rimane un problema.**

**Problema principale del vanilla `FishingHook`:**
Il vanilla `FishingHook` ha un campo `private final Player player` usato ovunque in `tick()` e `retrieve()`. Per far funzionare il hook con un mob owner tramite mixin servirebbe:
1. `@Unique Entity livingOwner` aggiunto via mixin
2. `@Redirect` su tutti gli accessi a `this.player` dentro `tick()` → non banale perché sono decine di reference
3. Bypassare il costruttore che richiede `Player`

È fattibile ma molto fragile e si rompe ad ogni aggiornamento vanilla.

**Problema del rendering:**
Il vanilla `FishingHookRenderer` fa questo:
```java
public void render(FishingHook hook, ...) {
    Player player = hook.getPlayerOwner(); // ritorna null se non è un Player
    if (player == null) return; // ← nessun render se owner è un mob
    // disegna la linea dall'ARM del player...
}
```
Anche se fixassimo il lato server, il client vanilla vedrebbe il hook **senza la linea** che collega al mob.

**Per avere la linea visibile ci sono due opzioni:**
- Mixin a `FishingHookRenderer` sul client → richiede comunque la mod client (Extra)
- Usare la custom `FishingHook` + `FishingHookRenderer` propria (approccio attuale)

**Conclusione**: Usare vanilla `FishingHook` via mixin è più complesso della soluzione custom, e il rendering della linea richiede comunque codice client. FisherMobs rimane nell'Extra.

---

## Features che vanno nell'Extra (client-required)

Queste feature usano **entità custom tracciate dai client** e quindi richiedono registrazione e rendering sul client.

### FisherMobs – Mob che pescano i giocatori
- **Entity**: `FishingHook` (estende `Projectile`)
  - `clientTrackingRange(4)` → viene sincronizzata ai client
  - Il client deve conoscere il tipo per non crashare
- **Renderer**: `FishingHookRenderer` (`@OnlyIn(Dist.CLIENT)`)
  - Render del gancio + corda verso il mob
- **Goal**: `FishingTargetGoal`, `FishingHook` logic lato server
- **Registrazione**: `EAIEntities.FISHING_HOOK`

**Motivo**: Il client vanilla che si connette a un server con questa feature riceve pacchetti di spawn per un entity type sconosciuto → potenziale disconnessione o errori. Il renderer richiede codice client.

### ThrowingWeb – Mob che lanciano ragnatele
- **Entity**: `ThrownWebEntity` (estende `ThrowableItemProjectile`)
  - `setTrackingRange(4)` → sincronizzata ai client
- **Renderer**: `ThrownItemRenderer` (vanilla), registrato in `ClientSetup`
  - Anche se il renderer è vanilla, la **registrazione** avviene lato client in `ClientSetup`
- **Registrazione**: `EAIEntities.THROWN_WEB`

**Motivo**: Stesso problema di FisherMobs per il tipo entità sconosciuto. La registrazione del renderer è in `ClientSetup`.

### CreeperRendererMixin (futuro)
- Attualmente **completamente commentato** (nessun effetto)
- Se riabilitato: mixin a `CreeperRenderer` (classe client-only) → va nell'Extra
- Scopo originale: modificare la scala visiva del Creeper durante il gonfiamento

---

## Struttura file da spostare nell'Extra

```
src/main/java/insane96mcp/enhancedai/
├── setup/
│   └── ClientSetup.java                          → EXTRA
├── mixin/
│   └── CreeperRendererMixin.java                 → EXTRA
└── modules/mobs/
    ├── fisher/
    │   ├── FishingHook.java                      → EXTRA (o condiviso)
    │   ├── FishingHookRenderer.java              → EXTRA
    │   ├── FishingTargetGoal.java                → EXTRA
    │   └── FishingHookAI logic...                → EXTRA
    └── webber/
        └── ThrownWebEntity.java  (registrazione) → registrazione nel EXTRA
            (la logica server va nella server mod, renderer/reg client nell'extra)
```

> **Nota**: `ThrownWebEntity` può essere divisa: la logica di comportamento (piazzare cobweb al hit) rimane nella server mod, ma la **registrazione dell'entity type** deve essere comune. In alternativa, si usa un approccio dove l'Extra dipende dalla server mod e ri-registra il renderer.

---

## Mixin configs

Il file `mixins.enhancedai.json` già separa:
- `"mixins"` → server-side (tutti i ~31 mixin attuali)
- `"client"` → `CreeperRendererMixin` (attualmente commentato)

Nell'Extra, si crea un nuovo `mixins.enhancedai_extra.json` con i mixin client-only.

---

## Riepilogo

| | Server-Side Mod | Extra Mod |
|---|---|---|
| **Requisiti client** | Nessuno (vanilla ok) | Deve essere installata anche sul client |
| **N° feature** | ~70 | ~2 (+CreeperRenderer se riabilitato) |
| **Entità custom** | Nessuna | FishingHook, ThrownWebEntity |
| **Renderer** | Nessuno | FishingHookRenderer, ThrownItemRenderer reg. |
| **Mixin client** | Nessuno | CreeperRendererMixin |
| **Dipendenze** | InsaneLib, (MPR opzionale) | InsaneLib + EnhancedAI server mod |
