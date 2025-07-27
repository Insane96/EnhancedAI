package insane96mcp.enhancedai.modules.villager;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.EATags;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.VILLAGER, description = "Make villagers fight back. Use the enhancedai:villagers_can_attack entity type tag to add more villagers. Attack damage can't be changed via config due to limitation so use mods like Mobs Properties Randomness to change the damage. Base damage is 4.")
public class VillagerAttacking extends Feature {
    public static final TagKey<EntityType<?>> VILLAGERS_CAN_ATTACK = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("villagers_can_attack"));
    @Config(description = "If true, villagers will attack back monsters")
    public static Boolean villagersFightBackEnemies = false;
    @Config(description = "Villagers will only attack players that have below this reputation (like Iron Golems by default). https://minecraft.wiki/w/Villager#Gossiping")
    public static Integer minReputationFightBack = -100;
    @Config(min = 0d, max = 4d, description = "Movement speed multiplier when attacking")
    public static Double speedMultiplier = 0.4d;

    public VillagerAttacking(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Villager villager)
                || !villager.getType().is(VILLAGERS_CAN_ATTACK))
            return;

        CompoundTag persistentData = villager.getPersistentData();

        double movementSpeedMultiplier = NBTUtils.getDoubleOrPutDefault(persistentData, EATags.Passive.SPEED_MULTIPLIER_WHEN_AGGROED, speedMultiplier);

        villager.targetSelector.addGoal(1, (new EAVillagerHurtByTargetGoal(villager)).setAlertOthers());
        villager.goalSelector.addGoal(1, new MeleeAttackGoal(villager, movementSpeedMultiplier, false));
    }
}