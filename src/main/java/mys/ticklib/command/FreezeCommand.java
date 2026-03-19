package mys.ticklib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import mys.ticklib.Ticklib;
import mys.ticklib.freeze.FreezeManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ticklib.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FreezeCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal("tick")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            boolean next = !FreezeManager.isFrozen();
                            FreezeManager.setFrozen(next);
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("timefreeze = " + next),
                                    true
                            );
                            return 1;
                        })
                        .then(Commands.literal("freeze")
                                .executes(ctx -> {
                                    if (FreezeManager.isFrozen()) {
                                        ctx.getSource().sendFailure(Component.literal("tick is already frozen!"));
                                        return -1;
                                    }
                                    FreezeManager.setFrozen(true);
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("tick has been successfully frozen."),
                                            true
                                    );
                                    return 1;
                                })
                        )
                        .then(Commands.literal("unfreeze")
                                .executes(ctx -> {
                                    if (!FreezeManager.isFrozen()) {
                                        ctx.getSource().sendFailure(Component.literal("tick is not currently frozen!"));
                                        return -1;
                                    }
                                    FreezeManager.setFrozen(false);
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("tick has been successfully unfrozen."),
                                            true
                                    );
                                    return 1;
                                })
                        )
                        .then(Commands.literal("step")
                                .then(Commands.argument("ticks", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            int ticks = IntegerArgumentType.getInteger(ctx, "ticks");
                                            FreezeManager.step(ticks);
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("try to step " + ticks + " ticks"),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
        );
    }
}