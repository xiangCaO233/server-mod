package com.xiang.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.xiang.ServerUtility;
import com.xiang.navigate.Navigator;
import com.xiang.navigate.PlayerNavDestination;
import com.xiang.scoreborad.AllObjective;
import com.xiang.scoreborad.BetterObjective;
import com.xiang.util.ServerUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static com.mojang.brigadier.arguments.StringArgumentType.StringType.GREEDY_PHRASE;
import static com.xiang.ServerUtility.*;
import static com.xiang.alona.AlonaThread.sendGroupMessage;
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
                    literal("chat").executes((context) -> {
                                context.getSource().sendMessage(
                                        Text.literal(Formatting.GOLD + "请输入聊天消息")
                                );
                                return 0;
                            }).
                            then(argument("chatMessage", greedyString()).suggests(new ChatCommandSugp()).executes((context -> {
                                        String[] args = context.getInput().split(" ");
                                        String playerName = Objects.requireNonNull(context.getSource().getPlayer()).getEntityName();
                                        playerManager.broadcast(
                                                Text.of(" §b§n" + playerName + "§r  : " + args[1]), false
                                        );
                                        sendGroupMessage("[IH] [" + playerName + "]: " + args[1]);
                                        return 1;
                                    }))
                            )
            );
            dispatcher.register(
                    literal("setscoreboard").executes((context) -> {
                                if (context.getSource().getPlayer() != null)
                                    ServerUtil.executeCommand("tellraw " + context.getSource().getPlayer().getEntityName() + " [{\"text\":\"     ----  计分板切换菜单  ----\",\"color\":\"gold\"},{\"text\":\"   \"},{\"text\":\"[循环]\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/setscoreboard autoLoop\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"15s内循环播放榜单和信息\"}},{\"text\":\"\\n   \"},{\"text\":\"[服务器]\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/setscoreboard serverInfo\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"服务器的信息\"}},{\"text\":\"   \"},{\"text\":\"[死亡榜]\",\"color\":\"red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/setscoreboard deathRanking\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"死亡次数排行榜\"}},{\"text\":\"   \"},{\"text\":\"[挖掘榜]\",\"color\":\"yellow\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/setscoreboard minedRanking\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"挖掘方块数量排行榜\"}},{\"text\":\"\\n   \"},{\"text\":\"[放置榜]\",\"color\":\"gold\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/setscoreboard placedRanking\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"放置方块数量排行榜\"}},{\"text\":\"   \"},{\"text\":\"[交易榜]\",\"color\":\"dark_green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/setscoreboard tradeRanking\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"村民交易次数排行榜\"}},{\"text\":\"   \"},{\"text\":\"[移动榜]\",\"color\":\"dark_blue\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/setscoreboard moveRanking\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"移动距离排行榜\"}},{\"text\":\"\\n   \"},{\"text\":\"[经验榜]\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/setscoreboard expGetRanking\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"获得经验值榜\"}},{\"text\":\"   \"},{\"text\":\"[击杀榜]\",\"color\":\"dark_purple\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/setscoreboard killRanking\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"击杀生物或玩家数量榜\"}},{\"text\":\"   \"},{\"text\":\"[伤害榜]\",\"color\":\"red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/setscoreboard damageRanking\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"对生物造成的伤害量榜\"}},{\"text\":\"\\n   \"},{\"text\":\"[受伤榜]\",\"color\":\"dark_red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/setscoreboard takeDamageRanking\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"收到伤害量榜\"}},{\"text\":\"   \"},{\"text\":\"[在线榜]\",\"color\":\"white\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/setscoreboard onlineRanking\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"在线时间榜\"}},{\"text\":\"   \"},{\"text\":\"[等级榜]\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/setscoreboard levelRanking\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"玩家现有等级榜\"}}]");
                                return 1;
                            }).
                            then(argument("你想要显示的计分项", word()).suggests(new ScoreBoardCommandSugp()).executes((commandContext -> {
                                ServerPlayerEntity player = commandContext.getSource().getPlayer();
                                String name = Objects.requireNonNull(player).getEntityName();
                                String[] args = commandContext.getInput().split(" ");
                                if (name == null) {
                                    return 0;
                                }
                                {
                                    player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 0.2f, 1);
                                    //指定计分板
                                    //player.sendMessage(Text.of("已设置指定计分板:" + args[1]));
                                    AllObjective.setPlayerObjective(player, args[1]);
                                }
                                return 1;
                            })))
            );
            dispatcher.register(
                    literal("backup").executes(commandContext -> {
                        ServerUtility.createBackup();
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

    static class ChatCommandSugp implements SuggestionProvider<ServerCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> commandContext, SuggestionsBuilder suggestionsBuilder) {
            suggestionsBuilder.suggest("蚌埠住了");
            suggestionsBuilder.suggest("好好好");
            suggestionsBuilder.suggest("对对对");
            suggestionsBuilder.suggest("help");
            suggestionsBuilder.suggest("寄");
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
            //suggestionsBuilder.suggest("auto-loop");
            for (String key : AllObjective.getObjectiveNames()) {
                suggestionsBuilder.suggest(key);
            }
            return suggestionsBuilder.buildFuture();
        }
    }
}
