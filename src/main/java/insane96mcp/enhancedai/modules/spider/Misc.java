package insane96mcp.enhancedai.modules.spider;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.monster.Spider;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Miscellaneous", description = "Various small changes to Spiders.")
@LoadFeature(module = Modules.Ids.SPIDER)
public class Misc extends Feature {
	@Config(min = 0d, max = 1d)
	@Label(name = "Fall Damage Reduction", description = "Percentage reduction of the fall damage taken by spiders.")
	public static Double fallDamageReduction = 0.9d;

	public Misc(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onSpawn(LivingDamageEvent event) {
		if (!this.isEnabled()
				|| fallDamageReduction == 0d
				|| !event.getSource().is(DamageTypeTags.IS_FALL)
				|| !(event.getEntity() instanceof Spider))
			return;

		event.setAmount((float) (event.getAmount() * (1d - fallDamageReduction)));
	}
}