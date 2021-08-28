package insane96mcp.enhancedai.modules.zombie.feature;

import insane96mcp.enhancedai.modules.zombie.ai.AIZombieDigger;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Digger Zombie")
public class DiggerZombieFeature extends Feature {
	private final ForgeConfigSpec.ConfigValue<Double> diggerChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> diggerToolOnlyConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> diggerProperToolOnlyConfig;
	private final ForgeConfigSpec.ConfigValue<Double> miningSpeedMultiplierConfig;

	public double diggerChance = 0.05;
	public boolean diggerToolOnly = false;
	public boolean diggerProperToolOnly = false;
	public double miningSpeedMultiplier = 1d;

	public DiggerZombieFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		diggerChanceConfig = Config.builder
				.comment("Chance for a Zombie to spawn with the digger ability")
				.defineInRange("Digger Chance", this.diggerChance, 0d, 1d);
		diggerToolOnlyConfig = Config.builder
				.comment("Zombies with Digger AI will mine only if they have a tool in the off-hand")
				.define("Digger Tool Only", this.diggerToolOnly);
		diggerProperToolOnlyConfig = Config.builder
				.comment("Zombies with Digger AI will mine only if their off-hand tool can mine targeted blocks (e.g. zombies with shovels will not mine stone). Blocks that require no tool (e.g. planks) will be minable.")
				.define("Digger Proper Tool Only", this.diggerProperToolOnly);
		miningSpeedMultiplierConfig = Config.builder
				.comment("Multiplier for digger zombies mining speed. E.g. with this set to 2, zombies will take twice the time to mine a block.")
				.defineInRange("Digger Speed Multiplier", this.miningSpeedMultiplier, 0d, 128d);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.diggerChance = this.diggerChanceConfig.get();
		this.diggerToolOnly = this.diggerToolOnlyConfig.get();
		this.diggerProperToolOnly = this.diggerProperToolOnlyConfig.get();
		this.miningSpeedMultiplier = this.miningSpeedMultiplierConfig.get();
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof ZombieEntity))
			return;

		ZombieEntity zombie = (ZombieEntity) event.getEntity();

		if (event.getWorld().rand.nextDouble() < this.diggerChance)
			zombie.goalSelector.addGoal(1, new AIZombieDigger(zombie, this.diggerToolOnly, this.diggerProperToolOnly));
	}
}
