package insane96mcp.enhancedai.modules.mobs.riding;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Riding", description = "Makes mobs ride other mobs")
@LoadFeature(module = Modules.Ids.MOBS)
public class Riding extends Feature {
    public static final TagKey<EntityType<?>> CAN_BE_MOUNTED = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "can_be_mounted"));
    public static final TagKey<EntityType<?>> CAN_MOUNT = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "can_mount"));
    public static final String CAN_SEARCH_MOUNT = EnhancedAI.RESOURCE_PREFIX + "can_search_mount";
    public static final String SUFFOCATION_WHILE_RIDING = EnhancedAI.RESOURCE_PREFIX + "suffocation_while_riding";
    @Config(min = 0d, max = 1d)
    @Label(name = "Chance to have Riding AI", description = "Chance for a mob to have an AI to go and ride mobs. Use enhancedai:can_be_mounted and enhancedai:can_mount entity type tags")
    public static Difficulty ridingAiChance = new Difficulty(0.025d, 0.05d, 0.075d);

    @Config
    @Label(name = "Stop mounting if too much suffocation", description = "If true, riding mobs will dismount if take too much suffocation damage.")
    public static Boolean stopMountingIfSuffocating = true;

    public Riding(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onMobJoinWorld(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(CAN_MOUNT))
            return;

        boolean canSearchMount = NBTUtils.getBooleanOrPutDefault(mob.getPersistentData(), CAN_SEARCH_MOUNT, mob.getRandom().nextDouble() < ridingAiChance.getByDifficulty(mob.level()));
        if (!canSearchMount)
            return;

        mob.targetSelector.addGoal(1, new SearchMountGoal(mob));
    }

    @SubscribeEvent
    public void onDamageTaken(LivingDamageEvent event) {
        if (!this.isEnabled()
                || !stopMountingIfSuffocating
                || !event.getSource().is(DamageTypes.IN_WALL)
                || event.getEntity().getVehicle() == null)
            return;

        float suffocatingDamageTaken = event.getEntity().getPersistentData().getFloat(SUFFOCATION_WHILE_RIDING);
        suffocatingDamageTaken += event.getAmount();
        if (suffocatingDamageTaken >= 6f)
            event.getEntity().stopRiding();
        else
            event.getEntity().getPersistentData().putFloat(SUFFOCATION_WHILE_RIDING, 6f);
    }
}
