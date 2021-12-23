package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.entity.EntityClassification;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Base")
public class BaseFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Integer> monstersDespawningDistanceConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> minMonstersDespawningDistanceConfig;

	public int monstersDespawningDistance = 80;
	public int minMonstersDespawningDistance = 48;

	public BaseFeature(Module module) {
		super(Config.builder, module, true, false);
		super.pushConfig(Config.builder);
		monstersDespawningDistanceConfig = Config.builder
				.comment("How far away from any player monsters will instantly despawn? Vanilla is 128")
				.defineInRange("Monsters Despawning Distance", this.monstersDespawningDistance, 0, 128);
		minMonstersDespawningDistanceConfig = Config.builder
				.comment("How far away from any player monsters will be able to randomly despawn? Vanilla is 32")
				.defineInRange("Min Monsters Despawning Distance", this.minMonstersDespawningDistance, 0, 128);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.monstersDespawningDistance = this.monstersDespawningDistanceConfig.get();
		this.minMonstersDespawningDistance = this.minMonstersDespawningDistanceConfig.get();
		EntityClassification.MONSTER.despawnDistance = this.monstersDespawningDistance;
	}

	//@SubscribeEvent
	//public void onMobSpawn(EntityAttributeCreationEvent event) {
		//AttributeModifierMap.MutableAttribute attribute = AttributeModifierMap.createMutableAttribute().createMutableAttribute(XRAY_FOLLOW_RANGE);
		//event.put(, attribute);
	//}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onMobSpawn(EntityJoinWorldEvent event) {
		/*if (!(event.getEntity() instanceof MonsterEntity))
			return;

		MonsterEntity entity = (MonsterEntity) event.getEntity();

		ModifiableAttributeInstance attributeInstance = entity.getAttributeManager().createInstanceIfAbsent(ModAttributes.XRAY_FOLLOW_RANGE.get());
		LogHelper.info("attribute instance " + attributeInstance);
		LogHelper.info("" + entity.getAttribute(ModAttributes.XRAY_FOLLOW_RANGE.get()));*/
	}
}
