package insane96mcp.enhancedai.modules.illager.shoot;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import insane96mcp.insanelib.base.config.MinMax;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.ILLAGER, description = "Use the enhancedai:pillager_shoot/better_shooting entity type tag to add more skeletons that are affected by this feature")
public class PillagerShoot extends Feature {

	public static final TagKey<EntityType<?>> BETTER_PILLAGER_SHOOT = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("pillager_shoot/better_shooting"));

	@Config(min = 1, max = 64, description = "The range from where a pillager will shoot a player")
	public static MinMax shootingRange = new MinMax(24, 32);
	@Config(min = 0, description = "The ticks cooldown before shooting. Vanilla is random between 20 and 40")
	public static MinMax shootingCooldown = new MinMax(20, 40);
	@Config(min = 0d, max = 30d, description = "How much inaccuracy does the arrow fired by pillagers have. Vanilla pillagers have 10/6/2 inaccuracy in easy/normal/hard difficulty.")
	public static Difficulty arrowInaccuracy = new Difficulty(5, 3, 1);

	public static EAIData<Integer> SHOOTING_RANGE;
	public static EAIData<Integer> SHOOTING_COOLDOWN;
	public static EAIData<Double> INACCURACY;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		SHOOTING_RANGE = EAIData.ofInt(this.createDataKey("shooting_range"));
		SHOOTING_COOLDOWN = EAIData.ofInt(this.createDataKey("shooting_cooldown"));
		INACCURACY = EAIData.ofDouble(this.createDataKey("inaccuracy"));
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Pillager pillager)
				|| !event.getEntity().getType().is(BETTER_PILLAGER_SHOOT))
			return;

		//Remove Crossbow Goal
		GoalHelper.removeGoal(pillager.goalSelector, RangedCrossbowAttackGoal.class);

		pillager.goalSelector.addGoal(3, new EAPillagerAttackGoal(pillager, 1d));
		SHOOTING_RANGE.applyIfAbsent(pillager, shootingRange.getIntRandBetween(pillager.getRandom()));
		SHOOTING_COOLDOWN.applyIfAbsent(pillager, shootingCooldown.getIntRandBetween(pillager.getRandom()));
		INACCURACY.applyIfAbsent(pillager, arrowInaccuracy.getByDifficulty(pillager.level()));
	}

}