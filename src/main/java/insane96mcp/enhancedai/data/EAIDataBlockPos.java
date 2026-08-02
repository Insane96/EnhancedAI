package insane96mcp.enhancedai.data;

import insane96mcp.insanelib.core.ModNBTData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.function.BiConsumer;

public class EAIDataBlockPos extends EAIData<BlockPos> {

	private EAIDataBlockPos(ResourceLocation id, BiConsumer<Mob, BlockPos> onChange) {
		super(id, onChange, BlockPos.class);
	}

	public static EAIDataBlockPos of(ResourceLocation id) {
		return of(id, (mob, value) -> {});
	}

	public static EAIDataBlockPos of(ResourceLocation id, BiConsumer<Mob, BlockPos> onChange) {
		EAIDataBlockPos data = new EAIDataBlockPos(id, onChange);
		DATA.add(data);
		return data;
	}

	@Override
	public void apply(Mob mob, BlockPos value) {
		ModNBTData.put(mob, this.id, value.asLong());
		onChange.accept(mob, value);
	}

	@Override
	public BlockPos get(Entity entity) {
		if (!ModNBTData.contains(entity, this.id))
			return null;
		long stored = ModNBTData.get(entity, this.id, Long.class);
		return BlockPos.of(stored);
	}

	@Override
	public BlockPos parse(String input) {
		String[] parts = input.split(",");
		if (parts.length != 3)
			throw new IllegalArgumentException("Invalid BlockPos value: " + input + ". Expected format: x,y,z");
		return new BlockPos(
				Integer.parseInt(parts[0].trim()),
				Integer.parseInt(parts[1].trim()),
				Integer.parseInt(parts[2].trim())
		);
	}
}
