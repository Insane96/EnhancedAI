package insane96mcp.enhancedai.setup;

import insane96mcp.enhancedai.EnhancedAI;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EAISounds {
	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, EnhancedAI.MOD_ID);

	public static final DeferredHolder<SoundEvent, SoundEvent> CREEPER_CENA_FUSE = SOUND_EVENTS.register("creeper_cena_fuse", () -> SoundEvent.createFixedRangeEvent(EnhancedAI.location("creeper_cena_fuse"), 64f));
	public static final DeferredHolder<SoundEvent, SoundEvent> CREEPER_CENA_EXPLODE = SOUND_EVENTS.register("creeper_cena_explode", () -> SoundEvent.createFixedRangeEvent(EnhancedAI.location("creeper_cena_explode"), 64f));
	public static final DeferredHolder<SoundEvent, SoundEvent> WTF_BOOM_FUSE = SOUND_EVENTS.register("wtf_boom_fuse", () -> SoundEvent.createFixedRangeEvent(EnhancedAI.location("wtf_boom_fuse"), 64f));
	public static final DeferredHolder<SoundEvent, SoundEvent> WTF_BOOM_EXPLODE = SOUND_EVENTS.register("wtf_boom_explode", () -> SoundEvent.createFixedRangeEvent(EnhancedAI.location("wtf_boom_explode"), 64f));
	public static final DeferredHolder<SoundEvent, SoundEvent> OLD_EXPLODE = SOUND_EVENTS.register("old_explode", () -> SoundEvent.createFixedRangeEvent(EnhancedAI.location("old_explode"), 64f));
}
