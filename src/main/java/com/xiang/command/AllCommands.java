package com.xiang.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.xiang.ServerUtility;
import com.xiang.navigate.Navigator;
import com.xiang.navigate.PlayerNavDestination;
import com.xiang.scoreborad.AllObjective;
import com.xiang.scoreborad.BetterObjective;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.xiang.ServerUtility.*;
import static com.xiang.navigate.Navigator.playerManager;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * @author xiang2333
 */
public class AllCommands implements ModInitializer, Navigator.NewNavCallback {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            //注册指令
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
                    literal("setscoreboard").executes((context)->{

                                return 1;
                            }).
                            then(argument("scoreboardobjective/auto-loop", word()).suggests(new ScoreBoardCommandSugp()).executes((commandContext -> {
                                String name = Objects.requireNonNull(commandContext.getSource().getPlayer()).getEntityName();
                                String[] args = commandContext.getInput().split(" ");
                                if (name == null) {
                                    return 0;
                                }
                                if ("auto-loop".equals(args[1])) {
                                    //设置auto

                                } else {
                                    //指定计分板

                                }
                                return 1;
                            })))
            );
            dispatcher.register(
                    literal("backup").executes(commandContext -> {
                        Objects.requireNonNull(Objects.requireNonNull(commandContext.getSource().getPlayer()).getServer()).getPlayerManager().broadcast(Text.of(Formatting.GOLD +"开始备份"), false);
                        long timeUsed = ServerUtility.createBackup();
                        Objects.requireNonNull(Objects.requireNonNull(commandContext.getSource().getPlayer()).getServer()).getPlayerManager().broadcast(Text.of((Formatting.GREEN+"备份花费:"+Formatting.RESET + timeUsed + "ms")), false);
                        return 1;
                    })
            );
        });
    }

    /**
     * 更新导航线程回调内容
     */
    @Override
    public void updateTimer() {
        if (Navigator.DESTINATIONS.size() > 0) {
            if (Navigator.stopNavThread) {
                //如果是停止状态
                Navigator.stopNavThread = false;
                Navigator.startTimer();
            }
        } else {
            Navigator.pauseTimer();
        }
    }

    static class NavCommandSugp implements SuggestionProvider<ServerCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> commandContext, SuggestionsBuilder suggestionsBuilder) {
            suggestionsBuilder.suggest("stop");
            //导航指令补全内容
            for (PlayerEntity player : playerManager.getPlayerList()) {
                suggestionsBuilder.suggest(player.getEntityName());
            }
            return suggestionsBuilder.buildFuture();
        }
    }

    static class ScoreBoardCommandSugp implements SuggestionProvider<ServerCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> commandContext, SuggestionsBuilder suggestionsBuilder) {
            //setscoreboard指令 补全内容
            suggestionsBuilder.suggest("auto-loop");
            for (String key: AllObjective.objectiveMap.keySet()){
                suggestionsBuilder.suggest(key);
            }
            return suggestionsBuilder.buildFuture();
        }
    }
}
