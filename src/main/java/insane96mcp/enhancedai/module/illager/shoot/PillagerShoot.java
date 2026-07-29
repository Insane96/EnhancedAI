package insane96mcp.enhancedai.module.illager.shoot;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.DifficultyBasedConfig;
import insane96mcp.insanelib.core.feature.config.MinMaxConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import javax.annotation.Nullable;

@LoadFeature(module = EAIModules.ILLAGER, description = "Use the enhancedai:illager/better_shooting entity type tag to add more skeletons that are affected by this feature")
public class PillagerShoot extends Feature {

	public static final TagKey<EntityType<?>> BETTER_PILLAGER_SHOOT = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("illager/better_shooting"));

	@Config(min = 1, max = 64, description = "The range from where a pillager will shoot a target")
	public static MinMaxConfig shootingRange = new MinMaxConfig(24, 32);
	@Config(min = 0, description = "The ticks cooldown before shooting. Vanilla is random between 20 and 40")
	public static MinMaxConfig shootingCooldown = new MinMaxConfig(20, 40);
	@Config(min = 0d, max = 30d, description = "How much inaccuracy does the arrow fired by pillagers have. Vanilla pillagers have 10/6/2 inaccuracy in easy/normal/hard difficulty.")
	public static DifficultyBasedConfig inaccuracy = new DifficultyBasedConfig(5, 3, 1);

	public static EAIData<Integer> SHOOTING_RANGE;
	public static EAIData<Integer> SHOOTING_COOLDOWN;
	public static EAIData<Double> INACCURACY;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		SHOOTING_RANGE = EAIData.ofInt(this.createDataKey("shooting_range"));
		SHOOTING_COOLDOWN = EAIData.ofInt(this.createDataKey("shooting_cooldown"));
		INACCURACY = EAIData.ofDouble(this.createDataKey("inaccuracy"));
	}

	@SubscribeEvent
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
				|| !(event.getEntity() instanceof Pillager pillager)
				|| !event.getEntity().getType().is(BETTER_PILLAGER_SHOOT))
			return;

		//Remove Crossbow Goal
		GoalHelper.removeGoal(pillager.goalSelector, RangedCrossbowAttackGoal.class);

		pillager.goalSelector.addGoal(3, new EAIPillagerAttackGoal(pillager, 1d));
		SHOOTING_RANGE.applyIfAbsent(pillager, shootingRange.getIntRandBetween(pillager.getRandom()));
		SHOOTING_COOLDOWN.applyIfAbsent(pillager, shootingCooldown.getIntRandBetween(pillager.getRandom()));
		INACCURACY.applyIfAbsent(pillager, inaccuracy.getByDifficulty(pillager.level()));
	}

	@SubscribeEvent
	public void onPillagerHitAlly(LivingDamageEvent.Post event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Pillager hitPillager)
				|| !(event.getSource().getEntity() instanceof Pillager shooter)
				|| !(event.getSource().getDirectEntity() instanceof AbstractArrow))
			return;

		tryReposition(shooter, hitPillager,3);
	}

	public static void tryReposition(Pillager pillager, @Nullable Entity ally, int baseDistance) {
		double distance = baseDistance;
		if (ally != null && pillager.getTarget() != null)
			distance += Mth.clamp(10 - ally.distanceTo(pillager.getTarget()), 0, 10);
		Vec3 viewVector = pillager.getViewVector(1.0F);
		Vec3 sideVector = viewVector.cross(new Vec3(0, 1, 0)).normalize().scale(distance);
		Vec3 position = pillager.position();

		Vec3 targetPos = position.add(sideVector.scale(pillager.getRandom().nextBoolean() ? 1 : -1));
		pillager.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1f);
	}

	@Nullable
	public static Entity mightHitAnAlly(Pillager pillager, double distance) {
		double distanceNotSqr = Math.sqrt(distance);
		Vec3 from = pillager.getEyePosition(0.5f);
		Vec3 viewVec = pillager.getViewVector(0.5f);
		Vec3 to = from.add(viewVec.x * distanceNotSqr, viewVec.y * distanceNotSqr, viewVec.z * distanceNotSqr);
		AABB aabb = pillager.getBoundingBox().expandTowards(viewVec.scale(distanceNotSqr)).inflate(1.0D, 1.0D, 1.0D);
		EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(pillager, from, to, aabb, entity -> entity.getType().is(EntityTypeTags.RAIDERS), distance);
		return entityHitResult == null ? null : entityHitResult.getEntity();
	}

}