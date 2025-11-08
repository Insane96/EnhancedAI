package insane96mcp.enhancedai.modules.villager;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

@LoadFeature(module = Modules.Ids.VILLAGER, description = "Villagers will alert Iron Golems and Village Guards when panicking")
public class VillagerAlertProtectors extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("villager/alert_protectors"));
    public static final TagKey<EntityType<?>> PROTECTORS = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("villager/protectors"));

    @Config(min = 0)
    public static Integer alertRange = 40;

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(LivingEvent.LivingTickEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Villager villager)
                || !villager.getType().is(AFFECTED_ENTITY_TYPES)
                || (villager.tickCount + villager.getId()) % 10 != 0)
            return;

        villager.getBrain().getMemory(MemoryModuleType.NEAREST_HOSTILE).ifPresent(hostile -> {
            List<Mob> protectors = villager.level().getEntitiesOfClass(Mob.class, villager.getBoundingBox().inflate(alertRange),
                    possibleProtector -> possibleProtector.getType().is(PROTECTORS) && possibleProtector.getTarget() == null);
            for (Mob protector : protectors) {
                protector.setTarget(hostile);
            }
        });
    }
}