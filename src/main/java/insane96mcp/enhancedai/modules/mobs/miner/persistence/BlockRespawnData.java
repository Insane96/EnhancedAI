package insane96mcp.enhancedai.modules.mobs.miner.persistence;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.NbtUtils;

import java.util.HashMap;
import java.util.Map;

public class BlockRespawnData extends SavedData {

	private final Map<BlockPos, RespawnEntry> respawnEntries = new HashMap<>();

	public static class RespawnEntry {
		public final long time;
		public final BlockState state;
		public final CompoundTag nbt;

		public RespawnEntry(long time, BlockState state, CompoundTag nbt) {
			this.time = time;
			this.state = state;
			this.nbt = nbt != null ? nbt.copy() : null;
		}
	}

	public BlockRespawnData() {}

	// Get or create the data instance for a level
	public static BlockRespawnData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(
				nbt -> load(nbt, level),
				BlockRespawnData::new,
				"enhancedai_block_respawns"
		);
	}

	// Load from NBT (requires ServerLevel for registry access)
	public static BlockRespawnData load(CompoundTag nbt, ServerLevel level) {
		BlockRespawnData data = new BlockRespawnData();
		ListTag list = nbt.getList("Respawns", Tag.TAG_COMPOUND);

		HolderGetter<Block> blockRegistry = level.holderLookup(Registries.BLOCK);

		for (Tag t : list) {
			if (!(t instanceof CompoundTag tag)) continue;

			BlockPos pos = NbtUtils.readBlockPos(tag.getCompound("Pos"));
			long time = tag.getLong("Time");
			BlockState state = NbtUtils.readBlockState(blockRegistry, tag.getCompound("State"));
			CompoundTag beNbt = tag.contains("BlockEntity") ? tag.getCompound("BlockEntity").copy() : null;

			data.respawnEntries.put(pos, new RespawnEntry(time, state, beNbt));
		}

		return data;
	}



	@Override
	public CompoundTag save(CompoundTag nbt) {
		ListTag list = new ListTag();

		for (Map.Entry<BlockPos, RespawnEntry> entry : respawnEntries.entrySet()) {
			CompoundTag tag = new CompoundTag();
			tag.put("Pos", NbtUtils.writeBlockPos(entry.getKey()));
			tag.putLong("Time", entry.getValue().time);
			tag.put("State", NbtUtils.writeBlockState(entry.getValue().state));
			if (entry.getValue().nbt != null) tag.put("BlockEntity", entry.getValue().nbt.copy());
			list.add(tag);
		}

		nbt.put("Respawns", list);
		return nbt;
	}

	// Add or update a respawn entry
	public void set(BlockPos pos, long time, BlockState state, CompoundTag beNbt) {
		respawnEntries.put(pos, new RespawnEntry(time, state, beNbt));
		this.setDirty();
	}

	// Remove a respawn entry
	public void remove(BlockPos pos) {
		respawnEntries.remove(pos);
		this.setDirty();
	}

	// Access all entries
	public Map<BlockPos, RespawnEntry> getEntries() {
		return respawnEntries;
	}
}
