package insane96mcp.enhancedai.modules.villager;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.EATags;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Villager Attacking", description = "Make villagers fight back. Use the enhancedai:villagers_can_attack entity type tag to add more villagers.")
@LoadFeature(module = Modules.Ids.VILLAGER)
public class VillagerAttacking extends Feature {
    public static final TagKey<EntityType<?>> VILLAGERS_CAN_ATTACK = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "villagers_can_attack"));
    @Config
    @Label(name = "Villagers Fight back", description = "If true, when attacked, villagers will call other villagers for help and attack back. Attack damage can't be changed via config due to limitation so use mods like Mobs Properties Randomness to change the damage. Base damage is 4")
    public static Boolean villagersFightBack = true;
    @Config
    @Label(name = "Villagers Fight back Enemies", description = "If false villagers will not attack back monsters")
    public static Boolean villagersFightBackEnemies = false;
    @Config
    @Label(name = "Reputation for Fight back", description = "Villagers will only attack players that have below this reputation (like Iron Golems by default). https://minecraft.wiki/w/Villager#Gossiping")
    public static Integer minReputationFightBack = -100;
    @Config(min = 0d, max = 4d)
    @Label(name = "Movement Speed Multiplier", description = "Movement speed multiplier when attacking")
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

        if (villagersFightBack) {
            villager.targetSelector.addGoal(1, (new EAVillagerHurtByTargetGoal(villager)).setAlertOthers());
            villager.goalSelector.addGoal(1, new MeleeAttackGoal(villager, movementSpeedMultiplier, false));
            //villager.getBrain().removeAllBehaviors();
        }
    }

    /*@SubscribeEvent(priority = EventPriority.LOWEST)
    public void onHit(LivingAttackEvent event) {
        if (!this.isEnabled()
                || !(event.getSource().getEntity() instanceof Player)
                || !(event.getEntity() instanceof Villager villager)
                || entityBlacklist.isEntityBlackOrNotWhitelist(villager))
            return;

        villager.getBrain().eraseMemory(MemoryModuleType.HURT_BY);
        villager.getBrain().eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
        villager.getBrain().updateActivityFromSchedule(villager.level().getDayTime(), villager.level().getGameTime());
    }*/
}