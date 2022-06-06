package insane96mcp.enhancedai.modules.witch.feature;

import insane96mcp.enhancedai.modules.base.ai.EAAvoidEntityGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Witch Flee Target", description = "Witches flee from the target.")
public class WitchFleeTarget extends Feature {

    private final ForgeConfigSpec.ConfigValue<Double> avoidPlayerChanceConfig;
    private final ForgeConfigSpec.ConfigValue<Double> attackWhenAvoidingChanceConfig;
    private final ForgeConfigSpec.ConfigValue<Double> fleeDistanceConfig;
    private final ForgeConfigSpec.ConfigValue<Double> fleeDistanceNearConfig;
    private final ForgeConfigSpec.ConfigValue<Double> fleeSpeedNearConfig;
    private final ForgeConfigSpec.ConfigValue<Double> fleeSpeedFarConfig;

    public double avoidPlayerChance = 1d;
    public double attackWhenAvoidingChance = 0.5d;
    public double fleeDistance = 16;
    public double fleeDistanceNear = 8;
    public double fleeSpeedNear = 1.25d;
    public double fleeSpeedFar = 1.1d;

    public WitchFleeTarget(Module module) {
        super(Config.builder, module);
        super.pushConfig(Config.builder);
        avoidPlayerChanceConfig = Config.builder
                .comment("Chance for a Witch to spawn with the ability to avoid the player")
                .defineInRange("Avoid Player chance", this.avoidPlayerChance, 0d, 1d);
        attackWhenAvoidingChanceConfig = Config.builder
                .comment("Chance for a Witch to be able to throw potions while running from a player")
                .defineInRange("Attack When Avoiding Chance", this.attackWhenAvoidingChance, 0d, 1d);
        fleeDistanceConfig = Config.builder
                .comment("Distance from a player that will make the Witch run away.")
                .defineInRange("Flee Distance", this.fleeDistance, 0d, 32d);
        fleeDistanceNearConfig = Config.builder
                .comment("Distance from a player that counts as near and will make the Witch run away faster.")
                .defineInRange("Flee Distance Near", this.fleeDistanceNear, 0d, 32d);
        fleeSpeedFarConfig = Config.builder
                .comment("Speed multiplier when the Witch avoids the player and it's farther than 'Flee Distance Near' blocks from him.")
                .defineInRange("Flee speed Multiplier Far", this.fleeSpeedFar, 0d, 4d);
        fleeSpeedNearConfig = Config.builder
                .comment("Speed multiplier when the Witch avoids the player and it's within 'Flee Distance Near' blocks from him.")
                .defineInRange("Flee speed Multiplier Near", this.fleeSpeedNear, 0d, 4d);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.avoidPlayerChance = this.avoidPlayerChanceConfig.get();
        this.attackWhenAvoidingChance = this.attackWhenAvoidingChanceConfig.get();
        this.fleeDistance = this.fleeDistanceConfig.get();
        this.fleeDistanceNear = this.fleeDistanceNearConfig.get();
        this.fleeSpeedNear = this.fleeSpeedNearConfig.get();
        this.fleeSpeedFar = this.fleeSpeedFarConfig.get();
    }

    @SubscribeEvent
    public void onSpawn(EntityJoinWorldEvent event) {
        if (!this.isEnabled())
            return;

        if (event.getWorld().isClientSide)
            return;

        if (!(event.getEntity() instanceof Witch witch))
            return;

        boolean avoidTarget = witch.level.random.nextDouble() < this.avoidPlayerChance;

        if (!avoidTarget)
            return;

        EAAvoidEntityGoal<Player> avoidEntityGoal = new EAAvoidEntityGoal<>(witch, Player.class, (float) this.fleeDistance, (float) this.fleeDistanceNear, this.fleeSpeedNear, this.fleeSpeedFar);
        avoidEntityGoal.setAttackWhenRunning(witch.level.random.nextDouble() < this.attackWhenAvoidingChance);
        witch.goalSelector.addGoal(1, avoidEntityGoal);
    }
}
