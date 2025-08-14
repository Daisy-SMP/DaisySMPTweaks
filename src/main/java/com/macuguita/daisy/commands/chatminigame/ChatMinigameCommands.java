/*
 * Copyright (c) 2025 macuguita.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.macuguita.daisy.commands.chatminigame;

import java.util.UUID;

import com.macuguita.daisy.chatminigame.ChatMinigame;
import com.macuguita.daisy.components.DaisyComponents;
import com.macuguita.daisy.components.NoMinigamesComponent;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class ChatMinigameCommands {

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerCommands(dispatcher);
		});
	}

	private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("asktrivia")
				.requires(source -> source.hasPermissionLevel(2))
				.executes(context -> {
					ServerCommandSource source = context.getSource();
					ChatMinigame.askRandomQuestion(source.getServer(), false);
					source.sendFeedback(() -> Text.literal("Chat question sent"), false);
					return 1;
				})
				.then(CommandManager.argument("showAnswer", BoolArgumentType.bool())
						.executes(context -> {
							boolean showAnswer = BoolArgumentType.getBool(context, "showAnswer");
							ServerCommandSource source = context.getSource();
							ChatMinigame.askRandomQuestion(source.getServer(), showAnswer);
							source.sendFeedback(() -> Text.literal("Chat question sent"), false);
							return 1;
						})));
		dispatcher.register(CommandManager.literal("showanswer")
				.requires(source -> source.hasPermissionLevel(2))
				.executes(context -> {
					ServerCommandSource source = context.getSource();
					source.sendFeedback(() -> Text.literal("Question answer is: ")
									.append(Text.literal(ChatMinigame.getAnswer()).formatted(Formatting.YELLOW)),
							false);
					if (source.getPlayer() != null) {
						source.sendFeedback(() -> Text.literal(source.getPlayer().getName().getString() + " revealed the answer"), true);
					}
					return 1;
				}));
		dispatcher.register(CommandManager.literal("toggleminigame")
				.executes(context -> {
					ServerCommandSource source = context.getSource();
					ServerPlayerEntity player = source.getPlayer();

					NoMinigamesComponent noMinigameComponent = DaisyComponents.NO_MINIGAMES_COMPONENT.get(source.getServer().getScoreboard());

					if (player != null) {
						UUID playerUUID = player.getUuid();

						if (noMinigameComponent.getDisabledPlayers().contains(playerUUID)) {
							noMinigameComponent.removeDisabledPlayer(playerUUID);
						} else {
							noMinigameComponent.addDisabledPlayer(playerUUID);
						}
						return 1;
					}

					return 0;
				}));
	}

}
