# EnhancedAI: secondo controllo bug

Branch analizzato: `1.21.1` (commit `d0aae68`). Data: 2026-10-11.

Non ho modificato il codice. Non ho potuto compilarlo, perché l'ambiente non raggiunge maven.neoforged.net.
I punti marcati *(verifica)* dipendono da come è scritto il codice vanilla 1.21.1 e li ho controllati solo a memoria: vanno verificati sulle sorgenti locali (`C:\Users\delvi\.gradle\mc-sources\1.21.1-neoforge\`).

---

## Gravità alta

### 2. La sostituzione dei target goal fa perdere la logica delle sottoclassi vanilla *(verifica)*
**File:** `module/mobs/targeting/Targeting.java` (`processTargetGoal`, `createTargetGoal`)

Ogni `NearestAttackableTargetGoal` dei mob nel tag `better_nearby_targeting` viene sostituito da un `EAINearestAttackableTarget` generico. Si conservano solo `targetConditions`, cioè il selettore. Le condizioni scritte negli override di `canUse` si perdono.

- **Orsi polari:** `PolarBearAttackPlayersGoal` attacca i giocatori solo se c'è un cucciolo nel raggio di 8 blocchi (e mai se l'orso stesso è un cucciolo). Con il goal generico, tutti gli orsi adulti attaccano qualsiasi giocatore nel raggio, tranne il 5–15% che diventa neutrale.
- **Streghe nei raid:** `NearestHealableRaiderTargetGoal` (cooldown e 50% di probabilità) e `NearestAttackableWitchTargetGoal` (`setCanAttack`, alternato da `Witch.aiStep`) perdono il meccanismo di alternanza. La strega non si alterna più correttamente tra curare i raider e attaccare i giocatori. `Witch.aiStep` continua a chiamare `setCanAttack` sul goal originale, che però è stato rimosso.

Gli altri mob del tag (lupi, api, iron golem, volpi, snow golem) passano la condizione tramite il predicato, quindi il comportamento si conserva.

**Correzione suggerita:** aggiungere a `isGoalBlacklistedFromReplace` almeno `PolarBear.PolarBearAttackPlayersGoal`, `NearestHealableRaiderTargetGoal` e `NearestAttackableWitchTargetGoal`. Ancora meglio: sostituire solo le classi esattamente `NearestAttackableTargetGoal` e lasciare stare le sottoclassi, a parte quelle gestite esplicitamente (Spider, Vindicator Johnny, Shulker).

---

## Gravità media

### 3. Shielding sovrascrive la mano secondaria
**File:** `module/mobs/Shielding.java`

```java
if (mob.getRandom().nextDouble() < chanceToEquip)
    mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));   // nessun controllo isEmpty()
```

Miner, Pearler e Fisher mettono il loro oggetto nella mano secondaria, ma solo se è vuota. Shielding non controlla e lo sostituisce, con un esito che dipende dall'ordine degli handler di `EntityJoinLevelEvent`. Il caso peggiore è un miner che perde il piccone: con `CORRECT_TOOL_FOR_REQUIRED`, il valore predefinito, non riesce più a scavare la pietra.

**Correzione suggerita:** `&& mob.getOffhandItem().isEmpty()`.

### 4. ShulkerBullets: levitazione di durata 0
**File:** `module/shulker/ShulkerBullets.java`

```java
else return new MobEffectInstance(MobEffects.LEVITATION, LEVITATION_DURATION.get(mob), LEVITATION_AMPLIFIER.get(mob));
```

Quando il proprietario del proiettile è un `Mob` senza il dato, durata e amplificatore valgono 0 e il colpo non fa levitare. Succede:
- agli shulker esclusi da Spawning;
- agli shulker già esistenti quando la feature viene attivata a runtime;
- a eventuali mob di altre mod che sparano shulker bullet.

**Correzione suggerita:** se `!LEVITATION_DURATION.has(mob)`, usare i valori di config come nel ramo del proprietario che non è un mob.

### 5. CustomTargeting: tiro casuale ripetuto e bersaglio scelto all'istante
**File:** `module/mobs/customtargeting/CustomHostileData.java`, `CustomTargeting.java`

- **Tiro ripetuto a ogni caricamento.** `tryApply` ritira `chance` a ogni `EntityJoinLevelEvent`, e i goal non vengono salvati. Un cavallo del tag `hostile_horses` può essere ostile in una sessione e tranquillo in quella successiva. Andrebbe salvato l'esito, per esempio con un `EAIData`.
- **Bersaglio scelto all'istante.** Per i mob fuori dal tag `better_nearby_targeting`, `Targeting.TARGET_CHANCE` non viene mai applicato e vale 0. Il goal cerca un bersaglio a ogni valutazione invece che con probabilità 1 su 7.
- **Esclusioni ignorate.** Non viene controllato `Spawning.isUnaffectedByFeatures`.

### 6. TeleportToTarget: invisibilità persa se il chunk viene scaricato
**File:** `module/mobs/teleporttotarget/TeleportToTargetGoal.java`

È lo stesso schema già corretto in Dark Art. Se il chunk viene scaricato, il server si spegne o crasha dopo un autosave mentre i mob sono nascosti (`teleportTick > 0`), `stop()` non viene chiamato.
- `Invisible` e `NoGravity` sono salvati nell'NBT, quindi al caricamento i mob restano invisibili e senza gravità per sempre.
- Il modificatore di velocità invece è transient, quindi sparisce da solo.

**Correzione suggerita:** segnare i mob nascosti con un dato persistente e, al join, se il dato c'è, richiamare `show()` e rimuoverlo.

### 7. PickUpAndThrow: il mob trasportato resta in groppa
**File:** `module/mobs/pickandthrow/PickUpAndThrowGoal.java`

Quando scatta il timeout (`unreachableTime > adjustedTickDelay(120)`) mentre il mob ne sta trasportando un altro, `stop()` non lo fa scendere. Il mob trasportato resta in groppa finché il goal non riparte, cioè dopo il cooldown predefinito di 600 tick.