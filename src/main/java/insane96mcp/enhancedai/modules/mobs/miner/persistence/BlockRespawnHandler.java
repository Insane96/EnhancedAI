package insane96mcp.enhancedai.modules.mobs.miner.persistence;

import insane96mcp.enhancedai.EnhancedAI;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.Map;

@Mod.EventBusSubscriber(modid = EnhancedAI.MOD_ID)
public class BlockRespawnHandler {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        MinecraftServer server = event.getServer();
        if (server == null) return;

        for (ServerLevel level : server.getAllLevels()) {
            BlockRespawnData data = BlockRespawnData.get(level);
            long time = level.getGameTime();
            boolean changed = false;

            Iterator<Map.Entry<BlockPos, BlockRespawnData.RespawnEntry>> it = data.getEntries().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BlockPos, BlockRespawnData.RespawnEntry> entry = it.next();
                BlockPos pos = entry.getKey();
                BlockRespawnData.RespawnEntry info = entry.getValue();

                if (time >= info.time) {
                    try {
                        BlockState existing = level.getBlockState(pos);
                        if (!existing.isAir()) {
                            BlockEntity existingBe = level.getBlockEntity(pos);

                            // --- Properly drop blocks obstructing respawn (like mining with a pickaxe) ---
                            LootParams.Builder lootBuilder = new LootParams.Builder(level)
                                    .withParameter(LootContextParams.ORIGIN, pos.getCenter())
                                    .withOptionalParameter(LootContextParams.BLOCK_ENTITY, existingBe)
                                    .withOptionalParameter(LootContextParams.TOOL, ItemStack.EMPTY);

                            for (ItemStack drop : existing.getDrops(lootBuilder)) {
                                level.addFreshEntity(new ItemEntity(level,
                                        pos.getX() + 0.5d,
                                        pos.getY() + 0.5d,
                                        pos.getZ() + 0.5d,
                                        drop));
                            }

                            existing.spawnAfterBreak(level, pos, ItemStack.EMPTY, false);
                            level.removeBlock(pos, false);
                        }

                        // --- Push any entities up to avoid suffocation ---
                        AABB box = new AABB(pos);
                        for (Entity e : level.getEntities(null, box)) {
                            e.setPos(e.getX(), e.getY() + 1.0, e.getZ());
                        }

                        // --- Restore the saved block ---
                        level.setBlock(pos, info.state, 3);

                        // --- Restore NBT if tile entity ---
                        if (info.nbt != null) {
                            BlockEntity be = level.getBlockEntity(pos);
                            if (be != null) {
                                be.load(info.nbt);
                                be.setChanged();
                            } else {
                                EnhancedAI.LOGGER.warn("BlockEntity missing at {} when respawning; NBT skipped.", pos);
                            }
                        }

                        EnhancedAI.LOGGER.debug("Respawned block {} at {}", info.state.getBlock().getName().getString(), pos);

                    } catch (Exception e) {
                        EnhancedAI.LOGGER.warn("Failed to respawn block at {}: {}", pos, e.getMessage());
                    }

                    it.remove();
                    changed = true;
                }
            }

            if (changed) data.setDirty();
        }
    }
}
