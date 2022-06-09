package insane96mcp.enhancedai.modules.spider.feature;

import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.Blacklist;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Spider;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;

@Label(name = "Miscellaneous", description = "Various small changes to Spiders.")
public class Misc extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> fallDamageReductionConfig;

	private final Blacklist.Config entityBlacklistConfig;

	public double fallDamageReduction = 0.9d;

	public Blacklist entityBlacklist;

	public Misc(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		fallDamageReductionConfig = Config.builder
				.comment("Percentage reduction of the fall damage taken by spiders.")
				.defineInRange("Fall Damage Reduction", this.fallDamageReduction, 0d, 1d);
		entityBlacklistConfig = new Blacklist.Config(Config.builder, "Entity Blacklist", "Entities that shouldn't be affected by this feature")
				.setDefaultList(Collections.emptyList())
				.setIsDefaultWhitelist(false)
				.build();
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.fallDamageReduction = this.fallDamageReductionConfig.get();

		this.entityBlacklist = this.entityBlacklistConfig.get();
	}

	@SubscribeEvent
	public void onSpawn(LivingDamageEvent event) {
		if (!this.isEnabled())
			return;

		if (this.fallDamageReduction == 0d)
			return;

		if (!event.getSource().equals(DamageSource.FALL))
			return;

		if (!(event.getEntity() instanceof Spider spider))
			return;

		if (this.entityBlacklist.isBlackWhiteListed(spider.getType()))
			return;

		event.setAmount((float) (event.getAmount() * (1d - this.fallDamageReduction)));
	}
}