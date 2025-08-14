package insane96mcp.enhancedai.setup;

import insane96mcp.enhancedai.EnhancedAI;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EAISounds {
	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, EnhancedAI.MOD_ID);

	public static final RegistryObject<SoundEvent> CREEPER_CENA_FUSE = SOUND_EVENTS.register("creeper_cena_fuse", () -> SoundEvent.createFixedRangeEvent(EnhancedAI.location("creeper_cena_fuse"), 64f));
	public static final RegistryObject<SoundEvent> CREEPER_CENA_EXPLODE = SOUND_EVENTS.register("creeper_cena_explode", () -> SoundEvent.createFixedRangeEvent(EnhancedAI.location("creeper_cena_explode"), 64f));
	public static final RegistryObject<SoundEvent> WTF_BOOM_FUSE = SOUND_EVENTS.register("wtf_boom_fuse", () -> SoundEvent.createFixedRangeEvent(EnhancedAI.location("wtf_boom_fuse"), 64f));
	public static final RegistryObject<SoundEvent> WTF_BOOM_EXPLODE = SOUND_EVENTS.register("wtf_boom_explode", () -> SoundEvent.createFixedRangeEvent(EnhancedAI.location("wtf_boom_explode"), 64f));
	public static final RegistryObject<SoundEvent> OLD_EXPLODE = SOUND_EVENTS.register("old_explode", () -> SoundEvent.createFixedRangeEvent(EnhancedAI.location("old_explode"), 64f));
}
