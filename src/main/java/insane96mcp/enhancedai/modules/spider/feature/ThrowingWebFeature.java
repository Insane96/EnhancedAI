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
	private final ForgeConfigSpec.ConfigValue<Integer> destroyWebAfterConfig;
	private final ForgeConfigSpec.ConfigValue<Double> thrownWebDamageConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> throwingCooldownConfig;
	private final ForgeConfigSpec.ConfigValue<Double> minDistanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maxDistanceConfig;

	private final BlacklistConfig entityBlacklistConfig;

	public double webThrowChance = 0.1d;
	public int destroyWebAfter = 100;
	public double thrownWebDamage = 5d;
	public int throwingCooldown = 50;
	public double minDistance = 2.5d;
	public double maxDistance = 64d;
	public ArrayList<IdTagMatcher> entityBlacklist;
	public boolean entityBlacklistAsWhitelist;

	public ThrowingWebFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		webThrowChanceConfig = Config.builder
				.comment("Chance for a Spider to spawn with the ability to throw webs at the target.")
				.defineInRange("Web Throw Chance", this.webThrowChance, 0d, 1d);
		destroyWebAfterConfig = Config.builder
				.comment("After how many ticks will the web projectile's cobweb be destroyed?")
				.defineInRange("Destroy Web After", this.destroyWebAfter, 0, 6000);
		thrownWebDamageConfig = Config.builder
				.comment("Damage when the projectiles hits a mob. The damage is set for normal difficulty. Hard difficulty gets +50% damage and Easy gets (-50% + 1) damage.")
				.defineInRange("Web Throw Chance", this.thrownWebDamage, 0d, 128d);
		throwingCooldownConfig = Config.builder
				.comment("Every how many ticks do spiders throw the projectile")
				.defineInRange("Projectile cooldown", this.throwingCooldown, 1, 1200);
		minDistanceConfig = Config.builder
				.comment("Minimum distance required for the spider to throw webs. Setting this to 0 will make the spider throw webs even when attacking the player.")
				.defineInRange("Min Distance", this.minDistance, 0d, 64d);
		maxDistanceConfig = Config.builder
				.comment("Maximum distance at which the spider will throw webs.")
				.defineInRange("Max Distance", this.maxDistance, 0d, 64d);
		entityBlacklistConfig = new BlacklistConfig(Config.builder, "Entity Blacklist", "Entities that shouldn't get the Throwing Web AI", Collections.emptyList(), false);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.webThrowChance = this.webThrowChanceConfig.get();
		this.destroyWebAfter = this.destroyWebAfterConfig.get();
		this.thrownWebDamage = this.thrownWebDamageConfig.get();
		this.throwingCooldown = this.throwingCooldownConfig.get();
		this.minDistance = this.minDistanceConfig.get();
		this.maxDistance = this.maxDistanceConfig.get();
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