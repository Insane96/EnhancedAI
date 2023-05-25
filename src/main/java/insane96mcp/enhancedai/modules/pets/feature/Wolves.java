package insane96mcp.enhancedai.modules.pets.feature;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;
import java.util.UUID;

@Label(name = "Wolves")
@LoadFeature(module = Modules.Ids.PETS)
public class Wolves extends Feature {
    private static final String ON_SPAWN_PROCESSED = EnhancedAI.RESOURCE_PREFIX + "wolves_on_spawn_processed";
    @Config
    @Label(name = "Double HP and Damage")
    public static Boolean doubleHpAndDamage = true;

    @Config
    @Label(name = "Entity Blacklist", description = "Entities that will not be affected by this feature.")
    public static Blacklist entityBlacklist = new Blacklist(Collections.emptyList(), false);

    public Wolves(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !doubleHpAndDamage
                || !(event.getEntity() instanceof Wolf wolf)
                || entityBlacklist.isEntityBlackOrNotWhitelist(wolf)
                || wolf.getPersistentData().contains(ON_SPAWN_PROCESSED))
            return;

        MCUtils.applyModifier(wolf, Attributes.MOVEMENT_SPEED, UUID.fromString("f9353c93-25a5-42f4-a80e-4f4834b12e77"), "More HP for Wolves", 1d, AttributeModifier.Operation.MULTIPLY_BASE, true);
        MCUtils.applyModifier(wolf, Attributes.ATTACK_DAMAGE, UUID.fromString("e5e5bb8d-3eef-4e92-8897-909acdd4be61"), "More Damage for Wolves", 1d, AttributeModifier.Operation.MULTIPLY_BASE, true);
    }
}