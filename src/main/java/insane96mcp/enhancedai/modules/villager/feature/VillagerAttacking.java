package insane96mcp.enhancedai.modules.villager.feature;

import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.setup.EAStrings;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.Blacklist;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;

@Label(name = "Villager Attacking", description = "Make villagers fight back")
public class VillagerAttacking extends Feature {

    private final ForgeConfigSpec.ConfigValue<Boolean> villagersFightBackConfig;
    private final ForgeConfigSpec.ConfigValue<Double> speedMultiplierConfig;
    private final Blacklist.Config entityBlacklistConfig;

    public boolean villagersFightBack = true;
    public double speedMultiplier = 0.8d;
    public Blacklist entityBlacklist;

    private static final double BASE_ATTACK_DAMAGE = 4d;

    public VillagerAttacking(Module module) {
        super(Config.builder, module, true, false);
        this.pushConfig(Config.builder);
        villagersFightBackConfig = Config.builder
                .comment("If true, when attacked, villagers will call other villagers for help and attack back. Attack damage can't be changed via config due to limitation so use mods like Mobs Properties Randomness to increase the damage. Base damage is " + String.format("%.1f", BASE_ATTACK_DAMAGE))
                .define("Villagers Fight back", this.villagersFightBack);
        speedMultiplierConfig = Config.builder
                .comment("Movement speed multiplier when attacking.")
                .defineInRange("Movement Speed Multiplier", this.speedMultiplier, 0d, 4d);
        entityBlacklistConfig = new Blacklist.Config(Config.builder, "Entity Blacklist", "Entities that shouldn't be affected by this feature")
                .setDefaultList(Collections.emptyList())
                .setIsDefaultWhitelist(false)
                .build();
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.villagersFightBack = this.villagersFightBackConfig.get();
        this.speedMultiplier = this.speedMultiplierConfig.get();
        this.entityBlacklist = this.entityBlacklistConfig.get();
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinWorldEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Villager villager)
                || this.entityBlacklist.isEntityBlackOrNotWhitelist(villager))
            return;

        CompoundTag persistentData = villager.getPersistentData();

        double movementSpeedMultiplier = NBTUtils.getDoubleOrPutDefault(persistentData, EAStrings.Tags.Passive.SPEED_MULTIPLIER_WHEN_AGGROED, this.speedMultiplier);

        if (this.villagersFightBack) {
            villager.targetSelector.addGoal(1, (new HurtByTargetGoal(villager)).setAlertOthers());
            villager.goalSelector.addGoal(1, new MeleeAttackGoal(villager, movementSpeedMultiplier, false));
            /*AttributeInstance kbAttribute = villager.getAttribute(Attributes.ATTACK_KNOCKBACK);
            if (kbAttribute != null)
                kbAttribute.addPermanentModifier(new AttributeModifier("Animal knockback", 3.5d, AttributeModifier.Operation.ADDITION));*/
        }
    }
}