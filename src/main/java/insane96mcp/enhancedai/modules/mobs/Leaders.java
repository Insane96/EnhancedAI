package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.enhancedai.setup.EAIAttributes;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@LoadFeature(module = EAIModules.Ids.MOBS, description = "Allows mobs to call reinforcements (like vanilla zombies can, but better) and have leaders that have a high chance to call reinforcements. Only entity types in the `enhancedai:mobs/leaders` tag can jump. PLEASE NOTE that this feature uses a custom attribute (enhancedai:spawn_reinforcements_chance) instead of the vanilla one. This feature has also an MPR condition `enhancedai:is_leader` to check if the mob is a leader.")
public class Leaders extends Feature {
    private static final ResourceLocation BONUS_STATS_ID = EnhancedAI.location("leader_bonus_stats");
    private static final ResourceLocation REINFORCEMENT_CALLED_CHARGE_ID = EnhancedAI.location("reinforcement_called_charge");

    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/leaders"));

    @Config(min = 0, max = 1, description = "Chance for a mob to become a leader. Leader mobs have a high chance to spawn reinforcements.")
    public static Double leaderChance = 0.05d;
    @Config(min = 0, description = "At this damage, the chance to spawn reinforcements is 100% of the attribute, otherwise is scaled. E.g. with this set to 6 and enhancedai:spawn_reinforcements_chance attribute set to 0.5 the chance to spawn reinforcements is 50% at 6 damage, 25% at 3 damage or 100% at 12 damage. Set to 0 to disable scaling with damage. This is damage before resistances (armor, etc).")
    public static Double spawnReinforcementsChanceDamageScaled = 6d;
    @Config(description = "How much is enhancedai:spawn_reinforcements_chance reduced by each time a reinforcement is spawned.")
    public static Double chargePerSpawn = 0.05d;
    @Config
    public static Double leaderSpawnReinforcementsChance = 1d;
    @Config
    public static Boolean reinforcesCanSpawnAsLeader = false;
    @Config(description = "If true, leader mobs will have 4x health and +15 armor.")
    public static Boolean bonusStats = true;
    @Config
    public static Boolean removeVanillaSpawnReinforcementsChance = true;

    public static EAIData<Boolean> LEADER;
    public static EAIData<Double> CHARGE_PER_SPAWN;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
        LEADER = EAIData.ofBool(this.createDataKey("leader"), (mob, leader) -> {
            if (leader) {
                MCUtils.applyModifier(mob, EAIAttributes.SPAWN_REINFORCEMENTS_CHANCE, BONUS_STATS_ID, leaderSpawnReinforcementsChance, AttributeModifier.Operation.ADD_VALUE);
            }
            else {
                mob.getAttribute(EAIAttributes.SPAWN_REINFORCEMENTS_CHANCE).removeModifier(BONUS_STATS_ID);
            }

            if (!bonusStats)
                return;

            if (leader) {
                MCUtils.applyModifier(mob, Attributes.MAX_HEALTH, BONUS_STATS_ID, 3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                MCUtils.applyModifier(mob, Attributes.ARMOR, BONUS_STATS_ID, 15, AttributeModifier.Operation.ADD_VALUE);
            }
            else {
                mob.getAttribute(Attributes.MAX_HEALTH).removeModifier(BONUS_STATS_ID);
                mob.getAttribute(Attributes.ARMOR).removeModifier(BONUS_STATS_ID);
            }
        });
        CHARGE_PER_SPAWN = EAIData.ofDouble(this.createDataKey("charge_per_spawn"));
	}

    public static void attribute(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
            if (event.has(entityType, EAIAttributes.SPAWN_REINFORCEMENTS_CHANCE))
                continue;

            event.add(entityType, EAIAttributes.SPAWN_REINFORCEMENTS_CHANCE, 0d);
        }
    }

    @SubscribeEvent
    public void onHurt(LivingDamageEvent.Post event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Mob mob)
                || !(mob.level() instanceof ServerLevel serverLevel)
                || event.getSource().getEntity() == null)
            return;
        double chance = mob.getAttributeValue(EAIAttributes.SPAWN_REINFORCEMENTS_CHANCE);
        if (chance <= 0)
            return;
        if (spawnReinforcementsChanceDamageScaled > 0)
            chance *= (event.getNewDamage() / spawnReinforcementsChanceDamageScaled);
        if (mob.getRandom().nextDouble() >= chance)
            return;
        int x = mob.getBlockX();
        int y = mob.getBlockY();
        int z = mob.getBlockZ();
        //TODO event
        LivingEntity target = mob.getTarget();
        if (target == null && event.getSource().getEntity() instanceof LivingEntity attacker)
            target = attacker;
        Mob reinforcement = (Mob) mob.getType().create(mob.level());
        if (reinforcement == null)
            return;

        for (int i = 0; i < 10; i++) {
            int x1 = x + Mth.nextInt(mob.getRandom(), 7, 30) * Mth.nextInt(mob.getRandom(), -1, 1);
            int z1 = z + Mth.nextInt(mob.getRandom(), 7, 30) * Mth.nextInt(mob.getRandom(), -1, 1);
            int worldHeight = mob.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x1, z1);
            int y1 = y + Mth.nextInt(mob.getRandom(), -20, 20);
            if (y1 > worldHeight)
                y1 = worldHeight + 1;
            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(x1, y1, z1);
            EntityType<?> entitytype = reinforcement.getType();
            reinforcement.setPos(x1 + 0.5, y1, z1 + 0.5);

            boolean foundSpawnPosition = true;
            if (y1 < worldHeight) {
                while (SpawnPlacements.isSpawnPositionOk(entitytype, mob.level(), blockPos)
                        || !SpawnPlacements.checkSpawnRules(entitytype, serverLevel, MobSpawnType.REINFORCEMENT, blockPos, mob.level().random)
                        || !mob.level().isUnobstructed(reinforcement)
                        || !mob.level().noCollision(reinforcement)
                        || mob.level().containsAnyLiquid(reinforcement.getBoundingBox())) {
                    y1++;
                    if (y1 >= mob.level().getMaxBuildHeight() || y1 > worldHeight + 1) {
                        foundSpawnPosition = false;
                        break;
                    }
                    blockPos.set(x1, y1, z1);
                    reinforcement.setPos(x1 + 0.5, y1, z1 + 0.5);
                }
            }
            if (!foundSpawnPosition) {
                foundSpawnPosition = true;
                while (/*!NaturalSpawner.isSpawnPositionOk(spawnPlacementType, mob.level(), blockPos, entitytype)
                        ||*/ !SpawnPlacements.checkSpawnRules(entitytype, serverLevel, MobSpawnType.REINFORCEMENT, blockPos, mob.level().random)
                        || !mob.level().isUnobstructed(reinforcement)
                        || !mob.level().noCollision(reinforcement)
                        || mob.level().containsAnyLiquid(reinforcement.getBoundingBox())) {
                    y1--;
                    if (y1 <= mob.level().getMinBuildHeight()) {
                        foundSpawnPosition = false;
                        break;
                    }
                    blockPos.set(x1, y1, z1);
                    reinforcement.setPos(x1 + 0.5, y1, z1 + 0.5);
                }
            }
            if (!foundSpawnPosition || mob.level().hasNearbyAlivePlayer(x1, y1, z1, 7.0D))
                continue;

            if (target != null)
                reinforcement.setTarget(target);
            reinforcement.finalizeSpawn(serverLevel, mob.level().getCurrentDifficultyAt(reinforcement.blockPosition()), MobSpawnType.REINFORCEMENT, null);
            if (!reinforcesCanSpawnAsLeader)
                LEADER.applyIfAbsent(reinforcement, false);
            serverLevel.addFreshEntityWithPassengers(reinforcement);
            AttributeModifier modifier = mob.getAttribute(EAIAttributes.SPAWN_REINFORCEMENTS_CHANCE).getModifier(REINFORCEMENT_CALLED_CHARGE_ID);
            double currentCharge = 0;
            if (modifier != null)
                currentCharge = modifier.amount();
            double newCharge = currentCharge - chargePerSpawn;
            MCUtils.applyModifier(mob, EAIAttributes.SPAWN_REINFORCEMENTS_CHANCE, REINFORCEMENT_CALLED_CHARGE_ID, -newCharge, AttributeModifier.Operation.ADD_VALUE);
            MCUtils.applyModifier(reinforcement, EAIAttributes.SPAWN_REINFORCEMENTS_CHANCE, REINFORCEMENT_CALLED_CHARGE_ID, -newCharge, AttributeModifier.Operation.ADD_VALUE);
            break;
        }
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(AFFECTED_ENTITY_TYPES))
            return;

        LEADER.applyIfAbsent(mob, mob.getRandom().nextDouble() < leaderChance);
        CHARGE_PER_SPAWN.applyIfAbsent(mob, chargePerSpawn);
        if (removeVanillaSpawnReinforcementsChance && mob.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE) != null) {
            mob.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).removeModifiers();
            mob.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0);
        }
    }

    @SubscribeEvent
    public void onTick(EntityTickEvent.Pre event) {
        if (!LEADER.get(event.getEntity())
                || !(event.getEntity().level() instanceof ServerLevel serverLevel)
                || (event.getEntity().tickCount + event.getEntity().getId()) % 10 != 0)
            return;

        serverLevel.sendParticles(ParticleTypes.INSTANT_EFFECT, event.getEntity().getX(), event.getEntity().getEyeY(), event.getEntity().getZ(), 1, 0.25, 0.25, 0.25, 0);
    }

    public static boolean isLeader(LivingEntity entity) {
        return LEADER.get(entity);
    }
}
