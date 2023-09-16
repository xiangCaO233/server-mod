package com.xiang.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.xiang.ServerUtility;
import com.xiang.navigate.Navigator;
import com.xiang.navigate.PlayerNavDestination;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.xiang.ServerUtility.*;
import static com.xiang.ServerUtility.stopScoreboardT;
import static com.xiang.navigate.Navigator.playerManager;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * @author xiang2333
 */
public class AllCommands implements ModInitializer, Navigator.NewNavCallback, ServerUtility.SetAutoCallback {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    literal("nav").executes((context) -> {
                                context.getSource().sendMessage(
                                        Text.literal("请指定导航目标")
                                );
                                return 0;
                            }).
                            then(argument("playerName/[x,z]/[x,y,z]", word()).suggests(new NavCommandSugp()).executes((commandContext -> {
                                        //commandContext.getSource().sendMessage(Text.literal("导航到:" + commandContext.getNodes().get(1)));
                                        String[] args = commandContext.getInput().split(" ");
                                        Navigator.addNavDes(
                                                new PlayerNavDestination(
                                                        Objects.requireNonNull(commandContext.getSource().getPlayer()).getEntityName(),
                                                        args[1]
                                                ),
                                                this
                                        );
                                        return 1;
                                    }))
                            )
            );
            dispatcher.register(
                    literal("setscoreboard").
                            then(argument("scoreboardobjective/auto-loop", word()).suggests(new ScoreBoardCommandSugp()).executes((commandContext -> {
                                String name = Objects.requireNonNull(commandContext.getSource().getPlayer()).getEntityName();
                                String[] args = commandContext.getInput().split(" ");
                                if (name == null) {
                                    return 0;
                                }
                                if ("auto-loop".equals(args[1])) {
                                    //设置auto
                                    if (!autoScoreBoardPlayers.contains(name)) {
                                        autoScoreBoardPlayers.add(name);
                                        commandContext.getSource().sendMessage(Text.literal("设置自动切换计分板"));
                                        ServerUtility.newAuto(this);
                                    }
                                } else {
                                    //指定计分板
                                    Scoreboard scoreboard = Objects.requireNonNull(playerManager.getPlayer(name)).getScoreboard();
                                    ScoreboardObjective objective = scoreboard.getObjective(args[1]);
                                    if (objective != null) {
                                        autoScoreBoardPlayers.remove(name);
                                        scoreboard.setObjectiveSlot(
                                                Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, objective
                                        );
                                        commandContext.getSource().sendMessage(Text.literal("设置计分板:" + args[1]));
                                        ServerUtility.newAuto(this);
                                    } else {
                                        commandContext.getSource().sendMessage(Text.literal(args[1] + "计分项不存在"));
                                    }
                                }
                                return 1;
                            })))
            );
            dispatcher.register(
                    literal("backup").executes(commandContext -> {
                        Objects.requireNonNull(Objects.requireNonNull(commandContext.getSource().getPlayer()).getServer()).getPlayerManager().broadcast(Text.of("开始备份"), false);
                        Objects.requireNonNull(Objects.requireNonNull(commandContext.getSource().getPlayer()).getServer()).getPlayerManager().broadcast(Text.of(("备份花费:" + ServerUtility.createBackup() + "ms")), false);
                        LOGGER.info(("备份花费:" + ServerUtility.createBackup() + "ms"));
                        return 1;
                    })
            );
        });
    }

    @Override
    public void updateTimer() {
        if (Navigator.DESTINATIONS.size() > 0) {
            if (Navigator.stopNavThread) {
                //                如果是停止状态
                Navigator.stopNavThread = false;
                Navigator.startTimer();
            }
        } else {
            Navigator.pauseTimer();
        }
    }

    @Override
    public void server_mod$updateTimer() {
        if (autoScoreBoardPlayers.size() > 0) {
            if (stopScoreboardT) {
//                如果是停止状态
                stopScoreboardT = false;
                ServerUtility.startScoreBoardTimer();
            }

        } else {
            stopScoreboardT = true;
        }
    }

    static class NavCommandSugp implements SuggestionProvider<ServerCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> commandContext, SuggestionsBuilder suggestionsBuilder) {
            suggestionsBuilder.suggest("stop");
            for (PlayerEntity player : playerManager.getPlayerList()) {
                suggestionsBuilder.suggest(player.getEntityName());
            }
            return suggestionsBuilder.buildFuture();
        }
    }

    static class ScoreBoardCommandSugp implements SuggestionProvider<ServerCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> commandContext, SuggestionsBuilder suggestionsBuilder) {
            for (ScoreboardObjective objective : scoreboardObjectives) {
                suggestionsBuilder.suggest(objective.getName());
            }
            suggestionsBuilder.suggest("auto-loop");
            return suggestionsBuilder.buildFuture();
        }
    }
}
