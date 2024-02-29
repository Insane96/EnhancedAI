package insane96mcp.enhancedai.modules.illager.shoot;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.base.config.MinMax;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Pillager Shoot", description = "Use the enhancedai:better_pillager_shoot entity type tag to add more skeletons that are affected by this feature")
@LoadFeature(module = Modules.Ids.ILLAGER)
public class PillagerShoot extends Feature {

	public static final TagKey<EntityType<?>> BETTER_PILLAGER_SHOOT = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "better_pillager_shoot"));

	public static final String SHOOTING_RANGE = EnhancedAI.RESOURCE_PREFIX + "shooting_range";
	public static final String SHOOTING_COOLDOWN = EnhancedAI.RESOURCE_PREFIX + "shooting_cooldown";
	public static final String INACCURACY = EnhancedAI.RESOURCE_PREFIX + "inaccuracy";

	@Config(min = 1, max = 64)
	@Label(name = "Shooting Range", description = "The range from where a pillager will shoot a player")
	public static MinMax shootingRange = new MinMax(24, 32);
	@Config(min = 0)
	@Label(name = "Shooting Cooldown", description = "The ticks cooldown before shooting. Vanilla is random between 20 and 40")
	public static MinMax shootingCooldown = new MinMax(20, 40);
	@Config(min = 0d, max = 30d)
	@Label(name = "Arrow Inaccuracy", description = "How much inaccuracy does the arrow fired by pillagers have. Vanilla pillagers have 10/6/2 inaccuracy in easy/normal/hard difficulty.")
	public static Difficulty arrowInaccuracy = new Difficulty(5, 3, 1);

	public PillagerShoot(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Pillager pillager)
				|| !event.getEntity().getType().is(BETTER_PILLAGER_SHOOT))
			return;

		CompoundTag persistentData = pillager.getPersistentData();

		int shootingRange1 = NBTUtils.getIntOrPutDefault(persistentData, SHOOTING_RANGE, shootingRange.getIntRandBetween(pillager.getRandom()));
		double inaccuracy = NBTUtils.getDoubleOrPutDefault(persistentData, INACCURACY, arrowInaccuracy.getByDifficulty(pillager.level()));
		int shootingCooldown1 = NBTUtils.getIntOrPutDefault(persistentData, SHOOTING_COOLDOWN, shootingCooldown.getIntRandBetween(pillager.getRandom()));

		//Remove Crossbow Goal
		pillager.goalSelector.removeAllGoals(goal -> goal instanceof RangedCrossbowAttackGoal<?>);

		/*if (pillager.level().getDifficulty().equals(Difficulty.HARD))
			shootingCooldown1 /= 2;

		EARangedBowAttackGoal rangedBowAttackGoal = (EARangedBowAttackGoal) new EARangedBowAttackGoal(skeleton, 1.0d, shootingRange1, strafe)
				.setBowChargeTicks(bowChargeTicks1)
				.setAttackCooldown(shootingCooldown1)
				.setInaccuracy((float) inaccuracy);
		skeleton.goalSelector.addGoal(2, rangedBowAttackGoal);*/
		EAPillagerAttackGoal attackGoal = new EAPillagerAttackGoal(pillager, 1d, shootingRange1, shootingCooldown1, (float) inaccuracy);
		pillager.goalSelector.addGoal(3, attackGoal);
	}

}