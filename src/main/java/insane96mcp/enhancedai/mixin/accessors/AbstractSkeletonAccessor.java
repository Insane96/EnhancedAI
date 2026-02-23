package insane96mcp.enhancedai.mixin.accessors;

import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

@Mixin(AbstractSkeleton.class)
public interface AbstractSkeletonAccessor {
	@Invoker
	AbstractArrow invokeGetArrow(ItemStack arrow, float velocity, @Nullable ItemStack weapon);
}
