package insane96mcp.enhancedai.modules.zombie.feature;

import insane96mcp.enhancedai.modules.zombie.ai.DiggingGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.setup.Strings;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.BlacklistConfig;
import insane96mcp.insanelib.util.IdTagMatcher;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;

@Label(name = "Digger Zombie", description = "Zombies can mine blocks to reach the target")
public class DiggerZombie extends Feature {
	private final ForgeConfigSpec.ConfigValue<Double> diggerChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> diggerToolOnlyConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> diggerProperToolOnlyConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> blacklistTileEntitiesConfig;
	private final ForgeConfigSpec.ConfigValue<Double> miningSpeedMultiplierConfig;
	private final BlacklistConfig blockBlacklistConfig;
	private final BlacklistConfig entityBlacklistConfig;

	public double diggerChance = 0.05;
	public boolean diggerToolOnly = false;
	public boolean diggerProperToolOnly = false;
	public double miningSpeedMultiplier = 1d;
	public boolean blacklistTileEntities = false;
	public ArrayList<IdTagMatcher> blockBlacklist;
	public boolean blockBlacklistAsWhitelist;
	public ArrayList<IdTagMatcher> entityBlacklist;
	public boolean entityBlacklistAsWhitelist;

	public DiggerZombie(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		diggerChanceConfig = Config.builder
				.comment("Chance for a Zombie to spawn with the digger ability")
				.defineInRange("Digger Chance", this.diggerChance, 0d, 1d);
		//TODO Main and off-hand
		diggerToolOnlyConfig = Config.builder
				.comment("Zombies with Digger AI will mine only if they have a tool in the off-hand")
				.define("Digger Tool Only", this.diggerToolOnly);
		diggerProperToolOnlyConfig = Config.builder
				.comment("Zombies with Digger AI will mine only if their off-hand tool can mine targeted blocks (e.g. zombies with shovels will not mine stone). Blocks that require no tool (e.g. planks) will be minable.")
				.define("Digger Proper Tool Only", this.diggerProperToolOnly);
		miningSpeedMultiplierConfig = Config.builder
				.comment("Multiplier for digger zombies mining speed. E.g. with this set to 2, zombies will take twice the time to mine a block.")
				.defineInRange("Digger Speed Multiplier", this.miningSpeedMultiplier, 0d, 128d);
		blacklistTileEntitiesConfig = Config.builder
				.comment("Zombies with Digger AI will not be able to break tile entities")
				.define("Blacklist Tile Entities", this.blacklistTileEntities);
		blockBlacklistConfig = new BlacklistConfig(Config.builder, "Block Blacklist", "Blocks in here will not be minable by zombies (or will be the only minable in case it's whitelist)", Collections.emptyList(), false);
		entityBlacklistConfig = new BlacklistConfig(Config.builder, "Entity Blacklist", "Entities that shouldn't get the Digger AI", Collections.emptyList(), false);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.diggerChance = this.diggerChanceConfig.get();
		this.diggerToolOnly = this.diggerToolOnlyConfig.get();
		this.diggerProperToolOnly = this.diggerProperToolOnlyConfig.get();
		this.miningSpeedMultiplier = this.miningSpeedMultiplierConfig.get();
		this.blacklistTileEntities = this.blacklistTileEntitiesConfig.get();
		this.blockBlacklist = (ArrayList<IdTagMatcher>) IdTagMatcher.parseStringList(this.blockBlacklistConfig.listConfig.get());
		this.blockBlacklistAsWhitelist = this.blockBlacklistConfig.listAsWhitelistConfig.get();
		this.entityBlacklist = (ArrayList<IdTagMatcher>) IdTagMatcher.parseStringList(this.entityBlacklistConfig.listConfig.get());
		this.entityBlacklistAsWhitelist = this.entityBlacklistConfig.listAsWhitelistConfig.get();
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof Zombie zombie))
			return;

		//Check for black/whitelist
		boolean isInWhitelist = false;
		boolean isInBlacklist = false;
		for (IdTagMatcher blacklistEntry : this.entityBlacklist) {
			if (blacklistEntry.matchesEntity(zombie)) {
				if (!this.entityBlacklistAsWhitelist)
					isInBlacklist = true;
				else
					isInWhitelist = true;
				break;
			}
		}
		if (isInBlacklist || (!isInWhitelist && this.entityBlacklistAsWhitelist))
			return;

		boolean processed = zombie.getPersistentData().getBoolean(Strings.Tags.PROCESSED);

		boolean miner = zombie.level.random.nextDouble() < this.diggerChance;

		if (processed) {
			miner = zombie.getPersistentData().getBoolean(Strings.Tags.Zombie.MINER);
		}
		else {
			zombie.getPersistentData().putBoolean(Strings.Tags.Zombie.MINER, miner);
			zombie.getPersistentData().putBoolean(Strings.Tags.PROCESSED, true);
		}

		if (miner)
			zombie.goalSelector.addGoal(1, new DiggingGoal(zombie, this.diggerToolOnly, this.diggerProperToolOnly));

		zombie.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(zombie, Endermite.class, true));
	}
}
