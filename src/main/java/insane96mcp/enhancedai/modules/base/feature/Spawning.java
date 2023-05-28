package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.event.config.ModConfigEvent;


@Label(name = "Spawning")
@LoadFeature(module = Modules.Ids.BASE)
public class Spawning extends Feature {

	@Config(min = 0, max = 128)
	@Label(name = "Monsters Despawning Distance", description = "How far away from any player monsters will instantly despawn? Vanilla is 128")
	public static Integer monstersDespawningDistance = 96;
	@Config(min = 0, max = 128)
	@Label(name = "Min Monsters Despawning Distance", description = "How far away from any player monsters will be able to randomly despawn? Vanilla is 32")
	public static Integer minMonstersDespawningDistance = 48;

	public Spawning(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@Override
	public void readConfig(final ModConfigEvent event) {
		super.readConfig(event);
		if (this.isEnabled())
			MobCategory.MONSTER.despawnDistance = monstersDespawningDistance;
	}
}
