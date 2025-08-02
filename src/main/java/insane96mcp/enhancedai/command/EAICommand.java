package insane96mcp.enhancedai.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.Optional;

public class EAICommand {
    public static final SuggestionProvider<CommandSourceStack> DATA = SuggestionProviders.register(
            EnhancedAI.location("data"),
            (context, builder)
                    -> SharedSuggestionProvider.suggestResource(EAIData.DATA.stream().map(EAIData::id), builder)
    );

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext pContext) {
        pDispatcher.register(Commands.literal("enhancedai").requires((p_138819_)
                -> p_138819_.hasPermission(2))
                .then(Commands.literal("set")
                        .then(Commands.argument("targets", EntityArgument.entity())
                                .then(Commands.argument("data", ResourceLocationArgument.id()).suggests(DATA)
                                        .then(Commands.argument("value", StringArgumentType.word())
                                                .executes((ctx) -> {
                                                    ResourceLocation id = ResourceLocationArgument.getId(ctx, "data");
                                                    String valueStr = StringArgumentType.getString(ctx, "value");

                                                    Optional<EAIData<?>> optData = EAIData.DATA.stream().filter(d -> d.id().equals(id)).findFirst();
                                                    if (optData.isEmpty()) return 0;

                                                    EAIData<?> data = optData.get();
                                                    Object parsed;

                                                    try {
                                                        parsed = data.parse(valueStr);
                                                    } catch (Exception ex) {
                                                        ctx.getSource().sendFailure(Component.literal("Invalid value for data " + data.id()));
                                                        return 0;
                                                    }

                                                    for (Entity e : EntityArgument.getEntities(ctx, "targets")) {
                                                        if (e instanceof Mob mob)
                                                            apply(data, mob, parsed);
                                                    }

                                                    return 1;
                                                })
                ))))
                .then(Commands.literal("get")
                        .then(Commands.argument("targets", EntityArgument.entity())
                                .then(Commands.argument("data", ResourceLocationArgument.id()).suggests(DATA)
                                        .executes((ctx) -> {
                                            ResourceLocation id = ResourceLocationArgument.getId(ctx, "data");

                                            Optional<EAIData<?>> optData = EAIData.DATA.stream().filter(d -> d.id().equals(id)).findFirst();
                                            if (optData.isEmpty()) return 0;

                                            EAIData<?> data = optData.get();

                                            Entity entity = EntityArgument.getEntity(ctx, "targets");
                                            if (!data.has(entity)) {
                                                ctx.getSource().sendFailure(Component.literal("Entity does not have data " + data.id()));
                                                return 0;
                                            }
                                            Object value = data.get(entity);
                                            ctx.getSource().sendSuccess(() -> Component.literal(value + ""), true);
                                            return 1;
                                        })))));
    }

    @SuppressWarnings("unchecked")
    private static <T> void apply(EAIData<T> data, Mob mob, Object value) {
        data.apply(mob, (T) value);
    }
}
