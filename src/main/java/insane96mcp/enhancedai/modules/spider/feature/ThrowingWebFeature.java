package insane96mcp.enhancedai.modules.spider.feature;

import insane96mcp.enhancedai.modules.spider.ai.AISpiderWebThrow;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.BlacklistConfig;
import insane96mcp.insanelib.utils.IdTagMatcher;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;

@Label(name = "Throwing Web", description = "Makes spiders throw a web at a player, slowing them.")
public class ThrowingWebFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> webThrowChanceConfig;

	private final BlacklistConfig entityBlacklistConfig;

	public double webThrowChance = 0.1d;
	public ArrayList<IdTagMatcher> entityBlacklist;
	public boolean entityBlacklistAsWhitelist;

	public ThrowingWebFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		webThrowChanceConfig = Config.builder
				.comment("Chance for a Spider to spawn with the ability to throw webs at the target.")
				.defineInRange("Web Throw Chance", this.webThrowChance, 0d, 1d);
		entityBlacklistConfig = new BlacklistConfig(Config.builder, "Entity Blacklist", "Entities that shouldn't get the Throwing Web AI", Collections.emptyList(), false);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.webThrowChance = this.webThrowChanceConfig.get();
		this.entityBlacklist = IdTagMatcher.parseStringList(this.entityBlacklistConfig.listConfig.get());
		this.entityBlacklistAsWhitelist = this.entityBlacklistConfig.listAsWhitelistConfig.get();
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof SpiderEntity))
			return;

		SpiderEntity spider = (SpiderEntity) event.getEntity();

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

		if (event.getWorld().random.nextDouble() < this.webThrowChance)
			spider.goalSelector.addGoal(2, new AISpiderWebThrow(spider));
	}
}