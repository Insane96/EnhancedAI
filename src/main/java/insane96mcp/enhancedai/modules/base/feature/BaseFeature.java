package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.utils.ScheduledTickTask;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Base")
public class BaseFeature extends Feature {

	public static List<ScheduledTickTask> scheduledTickTasks;

	public BaseFeature(Module module) {
		super(Config.builder, module, true, false);
		scheduledTickTasks = new ArrayList<>();
		//Config.builder.comment(this.getDescription()).push(this.getName());
		//Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
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

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase.equals(TickEvent.Phase.END)) {
			for (ScheduledTickTask task : scheduledTickTasks) {
				task.tick();
			}
			scheduledTickTasks.removeIf(ScheduledTickTask::hasBeenExecuted);
		}
	}

	public static void scheduleTickTask(ScheduledTickTask scheduledTickTask) {
		scheduledTickTasks.add(scheduledTickTask);
	}
}
