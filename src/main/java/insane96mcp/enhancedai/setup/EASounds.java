package insane96mcp.enhancedai.setup;

import insane96mcp.enhancedai.EnhancedAI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EASounds {
	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, EnhancedAI.MOD_ID);

	public static final RegistryObject<SoundEvent> CREEPER_CENA_FUSE = SOUND_EVENTS.register("creeper_cena_fuse", () -> new SoundEvent(new ResourceLocation(EnhancedAI.MOD_ID, "creeper_cena_fuse")));
	public static final RegistryObject<SoundEvent> CREEPER_CENA_EXPLODE = SOUND_EVENTS.register("creeper_cena_explode", () -> new SoundEvent(new ResourceLocation(EnhancedAI.MOD_ID, "creeper_cena_explode")));
}
