package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;

import static insane96mcp.enhancedai.setup.EAIAttributes.ATTRIBUTES;

@LoadFeature(module = Modules.Ids.MOBS, description = "Allows mobs to call reinforcements (like vanilla zombies can, but better) and have leaders that have a high chance to call reinforcements. Only entity types in the `enhancedai:mobs/leaders` tag can jump. PLEASE NOTE that this feature uses a custom attribute (enhancedai:spawn_reinforcements_chance) instead of the vanilla one. This feature has also an MPR condition `enhancedai:is_leader` to check if the mob is a leader.")
public class Leaders extends Feature {
    private static final UUID BONUS_STATS_UUID = UUID.fromString("359f73af-4f99-4d5c-82dc-693b252793c5");

    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/leaders"));

    public static final RegistryObject<Attribute> SPAWN_REINFORCEMENTS_CHANCE = ATTRIBUTES.register("spawn_reinforcements_chance", () -> new RangedAttribute("attribute.name.spawn_reinforcements_chance", 0d, 0d, Double.MAX_VALUE));

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
                MCUtils.applyModifier(mob, SPAWN_REINFORCEMENTS_CHANCE.get(), BONUS_STATS_UUID, "Enhanced AI Leader Spawn Reinforcements Chance", leaderSpawnReinforcementsChance, AttributeModifier.Operation.ADDITION);
            }
            else {
                mob.getAttribute(SPAWN_REINFORCEMENTS_CHANCE.get()).removeModifier(BONUS_STATS_UUID);
            }

            if (!bonusStats)
                return;

            if (leader) {
                MCUtils.applyModifier(mob, Attributes.MAX_HEALTH, BONUS_STATS_UUID, "Enhanced AI Leader Health", 3, AttributeModifier.Operation.MULTIPLY_BASE);
                MCUtils.applyModifier(mob, Attributes.ARMOR, BONUS_STATS_UUID, "Enhanced AI Leader Armor", 15, AttributeModifier.Operation.ADDITION);
            }
            else {
                mob.getAttribute(Attributes.MAX_HEALTH).removeModifier(BONUS_STATS_UUID);
                mob.getAttribute(Attributes.ARMOR).removeModifier(BONUS_STATS_UUID);
            }
        });
        CHARGE_PER_SPAWN = EAIData.ofDouble(this.createDataKey("charge_per_spawn"));
	}

    public static void attribute(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
            if (event.has(entityType, SPAWN_REINFORCEMENTS_CHANCE.get())/*
                    || entityType.getBaseClass().isAssignableFrom(Player.class)*/)
                continue;

            event.add(entityType, SPAWN_REINFORCEMENTS_CHANCE.get(), 0d);
        }
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Mob mob)
                || !(mob.level() instanceof ServerLevel serverLevel)
                || event.getSource().getEntity() == null)
            return;
        double chance = mob.getAttributeValue(SPAWN_REINFORCEMENTS_CHANCE.get());
        if (chance <= 0)
            return;
        if (spawnReinforcementsChanceDamageScaled > 0)
            chance *= (event.getAmount() / spawnReinforcementsChanceDamageScaled);
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
            SpawnPlacements.Type spawnplacements$type = SpawnPlacements.getPlacementType(entitytype);
            reinforcement.setPos(x1 + 0.5, y1, z1 + 0.5);

            boolean foundSpawnPosition = true;
            if (y1 < worldHeight) {
                while (!NaturalSpawner.isSpawnPositionOk(spawnplacements$type, mob.level(), blockPos, entitytype)
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
                while (!NaturalSpawner.isSpawnPositionOk(spawnplacements$type, mob.level(), blockPos, entitytype)
                        || !SpawnPlacements.checkSpawnRules(entitytype, serverLevel, MobSpawnType.REINFORCEMENT, blockPos, mob.level().random)
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
            reinforcement.finalizeSpawn(serverLevel, mob.level().getCurrentDifficultyAt(reinforcement.blockPosition()), MobSpawnType.REINFORCEMENT, null, null);
            if (!reinforcesCanSpawnAsLeader)
                LEADER.applyIfAbsent(reinforcement, false);
            serverLevel.addFreshEntityWithPassengers(reinforcement);
            MCUtils.applyModifier(mob, SPAWN_REINFORCEMENTS_CHANCE.get(), UUID.randomUUID(), "Reinforcement caller charge", -chargePerSpawn, AttributeModifier.Operation.ADDITION);
            MCUtils.applyModifier(reinforcement, SPAWN_REINFORCEMENTS_CHANCE.get(), UUID.randomUUID(), "Reinforcement callee charge", -chargePerSpawn, AttributeModifier.Operation.ADDITION);
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
    public void onTick(LivingEvent.LivingTickEvent event) {
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
