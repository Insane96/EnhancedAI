package insane96mcp.enhancedai.integration;

import insane96mcp.explosionoverhaul.feature.BaseFeature;
import insane96mcp.insanelib.core.ModNBTData;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;

/**
 * Soft dependency on Explosion Overhaul. Every call is guarded by {@link #isLoaded()} so that
 * {@link BaseFeature} is never resolved when the mod is missing.
 */
public class ExplosionOverhaulIntegration {
	public static final String MOD_ID = "explosionoverhaul";

	private static Boolean loaded;

	public static boolean isLoaded() {
		if (loaded == null)
			loaded = ModList.get().isLoaded(MOD_ID);
		return loaded;
	}

	public static void setKnockbackMultiplier(Entity entity, float knockbackMultiplier) {
		if (isLoaded())
			Impl.setKnockbackMultiplier(entity, knockbackMultiplier);
	}

	public static void resetKnockbackMultiplier(Entity entity) {
		if (isLoaded())
			Impl.resetKnockbackMultiplier(entity);
	}

	/// Vanilla is 0.3
	public static void setRayStrengthMultiplier(Entity entity, float rayStrengthMultiplier) {
		if (isLoaded())
			Impl.setRayStrengthMultiplier(entity, rayStrengthMultiplier);
	}

	public static void resetRayStrengthMultiplier(Entity entity) {
		if (isLoaded())
			Impl.resetRayStrengthMultiplier(entity);
	}

	/// Only loaded when Explosion Overhaul is present
	private static class Impl {
		static void setKnockbackMultiplier(Entity entity, float knockbackMultiplier) {
			BaseFeature.setKnockbackMultiplier(entity, knockbackMultiplier);
		}

		static void resetKnockbackMultiplier(Entity entity) {
			ModNBTData.remove(entity, BaseFeature.KNOCKBACK_MULTIPLIER_TAG);
		}

		static void setRayStrengthMultiplier(Entity entity, float rayStrengthMultiplier) {
			BaseFeature.setRayStrengthMultiplier(entity, rayStrengthMultiplier);
		}

		static void resetRayStrengthMultiplier(Entity entity) {
			ModNBTData.remove(entity, BaseFeature.RAY_STRENGTH_MULTIPLIER_TAG);
		}
	}
}
