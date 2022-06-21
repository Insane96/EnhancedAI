package insane96mcp.enhancedai.mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(DebugPackets.class)
public class DebugPacketsMixin {

	@Inject(at = @At(value = "HEAD"), method = "sendPathFindingPacket")
	private static void sendPathFindingPacket(Level level, Mob mob, @Nullable Path path, float maxDistanceToWaypoint, CallbackInfo callbackInfo) {
		/*if (!(level instanceof ServerLevel) || path == null) return;

		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeInt(mob.getId()).writeFloat(maxDistanceToWaypoint);
		path.writeToStream(buf);
		sendPacketToAllPlayers((ServerLevel) level, buf, ClientboundCustomPayloadPacket.DEBUG_PATHFINDING_PACKET);*/
	}

	@Shadow
	private static void sendPacketToAllPlayers(ServerLevel p_133692_, FriendlyByteBuf p_133693_, ResourceLocation p_133694_) {}
}
