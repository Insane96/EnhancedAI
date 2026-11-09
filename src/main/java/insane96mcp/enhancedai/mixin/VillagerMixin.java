package insane96mcp.enhancedai.mixin;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import insane96mcp.enhancedai.module.villager.behavior.EatToHealBehavior;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerMixin {
    //Runs inside the LivingEntity constructor too, so feature, config and tag checks are in the behaviors
    @Inject(method = "registerBrainGoals", at = @At("TAIL"))
    private void enhancedai$addBehaviors(Brain<Villager> brain, CallbackInfo ci) {
        brain.addActivity(Activity.CORE, ImmutableList.of(Pair.of(1, new EatToHealBehavior())));
    }
}
