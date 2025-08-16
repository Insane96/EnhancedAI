package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@LoadFeature(module = Modules.Ids.MOBS)
public class Spawning extends Feature {

	@Config(min = 0, max = 128, description = "How far away from any player monsters will instantly despawn? Vanilla is 128. Reducing this makes mobs more crowded around players.")
	public static Integer monstersDespawningDistance = 96;
	@Config(min = 0, max = 128, description = "How far away from any player monsters will be able to randomly despawn? Vanilla is 32")
	public static Integer minMonstersDespawningDistance = 48;

	@Override
	public void readConfig(final ModConfigEvent event) {
		super.readConfig(event);
		if (this.isEnabled())
			MobCategory.MONSTER.despawnDistance = monstersDespawningDistance;
	}
}
