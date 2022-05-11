package insane96mcp.enhancedai.modules.animal.feature;

import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

@Label(name = "Animal Attacking", description = "Make animals fight back and no longer flee when attacked")
public class AnimalAttacking extends Feature {

    private final ForgeConfigSpec.ConfigValue<Boolean> animalsFightBackConfig;
    private final ForgeConfigSpec.ConfigValue<Boolean> noMoreFleeConfig;

    public boolean animalsFightBack = true;
    public boolean noMoreFlee = true;
    private static final double ATTACK_DAMAGE = 3d;

    public AnimalAttacking(Module module) {
        super(Config.builder, module, true, false);
        super.pushConfig(Config.builder);
        animalsFightBackConfig = Config.builder
                .comment("If true, when attacked, animals will call other animals for help and attack back. Animals have a slightly bigger range to attack. Attack damage can't be changed via config due to limitation so use mods like Mobs Properties Randomness to increase the damage. Base damage is " + String.format("%.1f", ATTACK_DAMAGE))
                .define("Animals Fight back", this.animalsFightBack);
        noMoreFleeConfig = Config.builder
                .comment("If true, when attacked, animals will no longer flee.")
                .define("Animals Fight back", this.noMoreFlee);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.animalsFightBack = this.animalsFightBackConfig.get();
        this.noMoreFlee = this.noMoreFleeConfig.get();
    }

    public static void attribute(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
            if (event.has(entityType, Attributes.ATTACK_DAMAGE))
                continue;

            event.add(entityType, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
        }
    }

    @SubscribeEvent
    public void onSpawn(EntityJoinWorldEvent event) {
        if (!this.isEnabled())
            return;

        if (!(event.getEntity() instanceof Animal animal))
            return;

        if (this.animalsFightBack) {
            animal.targetSelector.addGoal(1, (new HurtByTargetGoal(animal)).setAlertOthers());
            animal.goalSelector.addGoal(1, new MeleeAttackGoal(animal, 1.3d, false));
        }

        if (this.noMoreFlee) {
            ArrayList<Goal> goalsToRemove = new ArrayList<>();
            for (WrappedGoal prioritizedGoal : animal.goalSelector.availableGoals) {
                if (!(prioritizedGoal.getGoal() instanceof PanicGoal goal))
                    continue;

                goalsToRemove.add(goal);
            }

            goalsToRemove.forEach(animal.goalSelector::removeGoal);
        }
    }
}