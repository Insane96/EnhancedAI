package insane96mcp.enhancedai.modules.spider.feature;

import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.BlacklistConfig;
import insane96mcp.insanelib.util.IdTagMatcher;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Spider;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;

@Label(name = "Miscellaneous", description = "Various small changes to Spiders.")
public class MiscFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> fallDamageReductionConfig;

	private final BlacklistConfig entityBlacklistConfig;

	public double fallDamageReduction = 0.9d;

	public ArrayList<IdTagMatcher> entityBlacklist;
	public boolean entityBlacklistAsWhitelist;

	public MiscFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		fallDamageReductionConfig = Config.builder
				.comment("Percentage reduction of the fall damage taken by spiders.")
				.defineInRange("Fall Damage Reduction", this.fallDamageReduction, 0d, 1d);
		entityBlacklistConfig = new BlacklistConfig(Config.builder, "Entity Blacklist", "Entities that shouldn't be affected by this feature", Collections.emptyList(), false);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.fallDamageReduction = this.fallDamageReductionConfig.get();

		this.entityBlacklist = (ArrayList<IdTagMatcher>) IdTagMatcher.parseStringList(this.entityBlacklistConfig.listConfig.get());
		this.entityBlacklistAsWhitelist = this.entityBlacklistConfig.listAsWhitelistConfig.get();
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

		//Check for black/whitelist
		boolean isInWhitelist = false;
		boolean isInBlacklist = false;
		for (IdTagMatcher blacklistEntry : this.entityBlacklist) {
			if (blacklistEntry.matchesEntity(spider)) {
				if (!this.entityBlacklistAsWhitelist)
					isInBlacklist = true;
				else
					isInWhitelist = true;
				break;
			}
		}
		if (isInBlacklist || (!isInWhitelist && this.entityBlacklistAsWhitelist))
			return;

		event.setAmount((float) (event.getAmount() * (1d - this.fallDamageReduction)));
	}
}