package insane96mcp.enhancedai.module.villager;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@LoadFeature(module = EAIModules.VILLAGER, description = "Smiths walk to nearby damaged iron golems and repair them when no enemy is around, and badly damaged golems walk to the closest smith. Use the enhancedai:can_repair_golems villager profession tag to change which professions repair, the enhancedai:villager/can_repair_golems entity type tag to add more villagers (only works with entities that extend vanilla Villagers) and the enhancedai:villager/repairable_golems entity type tag to add more golems.")
public class VillagerGolemRepair extends Feature {
    public static final TagKey<VillagerProfession> REPAIRING_PROFESSIONS = TagKey.create(Registries.VILLAGER_PROFESSION, EnhancedAI.location("can_repair_golems"));
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("villager/can_repair_golems"));
    public static final TagKey<EntityType<?>> REPAIRABLE_GOLEMS = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("villager/repairable_golems"));

    @Config(min = 0d, max = 1d, description = "Golems are repaired only when their health is below this percentage of max health")
    public static Double healthThreshold = 0.75d;
    @Config(min = 0d, description = "Health restored by a repair")
    public static Double repairAmount = 25d;
    @Config(min = 0, description = "Ticks between two repairs of the same villager")
    public static Integer repairCooldown = 40;
    @Config(min = 1, max = 16, description = "How many blocks away villagers look for golems to repair. Can't go over 16, the range at which villagers see other entities.")
    public static Integer repairRadius = 16;
    @Config(min = 0, max = 13, description = "Golems are not repaired if an enemy is within this many blocks of the villager or of the golem, even if not visible. Can't go over 13, otherwise enemies near the golem would be out of the villager's sensing range when it's repairing.")
    public static Integer enemyCheckRadius = 10;
    @Config(name = "Repair activities", description = "Villagers repair golems only while doing one of these activities. Adding activities like panic or rest makes them repair in situations this feature is meant to exclude.")
    public static List<String> repairActivitiesConfig = new ArrayList<>(List.of("minecraft:work", "minecraft:idle", "minecraft:meet"));
    private static List<Activity> repairActivities = List.of();
    @Config(description = "If true, golems built by players are repaired too")
    public static Boolean repairPlayerCreatedGolems = true;
    @Config(description = "If true, badly damaged golems that aren't fighting walk to the closest villager able to repair them and wait there")
    public static Boolean golemsSeekRepair = true;
    @Config(min = 0d, max = 1d, description = "Golems look for a villager to repair them only when their health is below this percentage of max health")
    public static Double seekHealthThreshold = 0.5d;
    @Config(min = 1, description = "How many blocks away golems look for a villager able to repair them")
    public static Integer seekRadius = 24;
    @Config(min = 0d, description = "Movement speed multiplier of golems walking to a villager")
    public static Double seekSpeed = 0.8d;
    @Config(min = 0, description = "Ticks a golem waits before looking for a villager again after giving up")
    public static Integer seekRetryCooldown = 600;

    //Set on villagers
    public static EAIData<Double> REPAIR_AMOUNT;
    public static EAIData<Integer> REPAIR_COOLDOWN;
    public static EAIData<Integer> REPAIR_RADIUS;
    //Set on both villagers and golems
    public static EAIData<Integer> ENEMY_CHECK_RADIUS;
    //Set on golems
    public static EAIData<Double> HEALTH_THRESHOLD;
    public static EAIData<Boolean> REPAIR_PLAYER_CREATED;
    public static EAIData<Boolean> SEEKS_REPAIR;
    public static EAIData<Double> SEEK_HEALTH_THRESHOLD;
    public static EAIData<Integer> SEEK_RADIUS;
    public static EAIData<Double> SEEK_SPEED;
    public static EAIData<Integer> SEEK_RETRY_COOLDOWN;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        REPAIR_AMOUNT = EAIData.ofDouble(this.createDataKey("repair_amount"));
        REPAIR_COOLDOWN = EAIData.ofInt(this.createDataKey("repair_cooldown"));
        REPAIR_RADIUS = EAIData.ofInt(this.createDataKey("repair_radius"));
        ENEMY_CHECK_RADIUS = EAIData.ofInt(this.createDataKey("enemy_check_radius"));
        HEALTH_THRESHOLD = EAIData.ofDouble(this.createDataKey("health_threshold"));
        REPAIR_PLAYER_CREATED = EAIData.ofBool(this.createDataKey("repair_player_created"));
        SEEKS_REPAIR = EAIData.ofBool(this.createDataKey("seeks_repair"));
        SEEK_HEALTH_THRESHOLD = EAIData.ofDouble(this.createDataKey("seek_health_threshold"));
        SEEK_RADIUS = EAIData.ofInt(this.createDataKey("seek_radius"));
        SEEK_SPEED = EAIData.ofDouble(this.createDataKey("seek_speed"));
        SEEK_RETRY_COOLDOWN = EAIData.ofInt(this.createDataKey("seek_retry_cooldown"));
    }

    @Override
    public void readConfig(final ModConfigEvent event) {
        super.readConfig(event);
        repairActivities = repairActivitiesConfig.stream()
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .map(BuiltInRegistries.ACTIVITY::getOptional)
                .flatMap(Optional::stream)
                .toList();
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || !(event.getEntity() instanceof Mob mob))
            return;

        if (mob instanceof Villager && mob.getType().is(AFFECTED_ENTITY_TYPES)) {
            REPAIR_AMOUNT.applyIfAbsent(mob, repairAmount);
            REPAIR_COOLDOWN.applyIfAbsent(mob, repairCooldown);
            REPAIR_RADIUS.applyIfAbsent(mob, repairRadius);
            ENEMY_CHECK_RADIUS.applyIfAbsent(mob, enemyCheckRadius);
        }
        if (mob.getType().is(REPAIRABLE_GOLEMS)) {
            HEALTH_THRESHOLD.applyIfAbsent(mob, healthThreshold);
            REPAIR_PLAYER_CREATED.applyIfAbsent(mob, repairPlayerCreatedGolems);
            SEEKS_REPAIR.applyIfAbsent(mob, golemsSeekRepair);
            SEEK_HEALTH_THRESHOLD.applyIfAbsent(mob, seekHealthThreshold);
            SEEK_RADIUS.applyIfAbsent(mob, seekRadius);
            SEEK_SPEED.applyIfAbsent(mob, seekSpeed);
            SEEK_RETRY_COOLDOWN.applyIfAbsent(mob, seekRetryCooldown);
            ENEMY_CHECK_RADIUS.applyIfAbsent(mob, enemyCheckRadius);
            mob.goalSelector.addGoal(1, new GolemSeekRepairGoal(mob));
        }
    }

    /**
     * Whether the villager is currently able and willing to repair golems. Doesn't check for nearby enemies.
     */
    public static boolean canRepair(Villager villager) {
        Brain<Villager> brain = villager.getBrain();
        return Feature.isEnabled(VillagerGolemRepair.class)
                && !villager.isBaby()
                && !villager.isSleeping()
                && !villager.isTrading()
                && villager.getTarget() == null
                && !brain.hasMemoryValue(MemoryModuleType.HURT_BY)
                && !brain.hasMemoryValue(MemoryModuleType.NEAREST_HOSTILE)
                && repairActivities.stream().anyMatch(brain::isActive)
                && villager.getType().is(AFFECTED_ENTITY_TYPES)
                && BuiltInRegistries.VILLAGER_PROFESSION.wrapAsHolder(villager.getVillagerData().getProfession()).is(REPAIRING_PROFESSIONS)
                && !Spawning.isUnaffectedByFeatures(villager);
    }

    /**
     * Whether the entity is a golem that needs a repair and isn't fighting
     */
    public static boolean needsRepair(LivingEntity entity) {
        return entity instanceof Mob golem
                && golem.isAlive()
                && golem.getType().is(REPAIRABLE_GOLEMS)
                && golem.getHealth() < golem.getMaxHealth() * HEALTH_THRESHOLD.getOrDefault(golem, healthThreshold)
                && golem.getTarget() == null
                && (REPAIR_PLAYER_CREATED.getOrDefault(golem, repairPlayerCreatedGolems) || !(golem instanceof IronGolem ironGolem && ironGolem.isPlayerCreated()));
    }
}
