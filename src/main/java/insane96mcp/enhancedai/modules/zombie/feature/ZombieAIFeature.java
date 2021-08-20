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

@Label(name = "Zombie AI")
public class ZombieAIFeature extends Feature {
	private final ForgeConfigSpec.ConfigValue<Double> diggerChanceConfig;

	public double diggerChance = 0.05;

	public ZombieAIFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		diggerChanceConfig = Config.builder
				.comment("Chance for a Zombie to spawn with the digger ability")
				.defineInRange("Digger Chance", this.diggerChance, 0d, 1d);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.diggerChance = this.diggerChanceConfig.get();
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof ZombieEntity))
			return;

		ZombieEntity zombie = (ZombieEntity) event.getEntity();

		if (event.getWorld().rand.nextDouble() < this.diggerChance)
			zombie.goalSelector.addGoal(1, new AIZombieDigger(zombie));
	}
}
