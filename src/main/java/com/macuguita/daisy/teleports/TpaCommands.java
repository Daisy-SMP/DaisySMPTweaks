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

package com.macuguita.daisy.teleports;

import java.util.HashMap;
import java.util.Map;

import com.macuguita.daisy.teleports.providers.TpacceptSuggestionProvider;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class TpaCommands {

	private static final Map<ServerPlayerEntity, ServerPlayerEntity> pendingRequestsHere = new HashMap<>();
	private static final Map<ServerPlayerEntity, ServerPlayerEntity> pendingRequestsTo = new HashMap<>();
	private static final Map<ServerPlayerEntity, Long> lastRequestTimes = new HashMap<>();

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerCommands(dispatcher);
		});
	}

	private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("tpa")
				.then(CommandManager.argument("target", EntityArgumentType.player())
						.executes(context -> {
							ServerCommandSource source = context.getSource();
							ServerPlayerEntity sender = source.getPlayer();
							ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");

							if (sender == null || target == null) return 0;
							if (sender.equals(target)) {
								source.sendFeedback(() -> Text.literal("You can't teleport to yourself!").formatted(Formatting.RED), false);
								return 0;
							}
							if (!canSendRequest(sender)) {
								source.sendFeedback(() -> Text.literal("You must wait before sending another request").formatted(Formatting.RED), false);
								return 0;
							}

							pendingRequestsTo.put(sender, target);
							lastRequestTimes.put(sender, System.currentTimeMillis());

							source.sendFeedback(() -> Text.literal("Sent teleport request to " + target.getName().getString()), false);
							target.sendMessage(
									Text.literal(sender.getName().getString() + " wants to teleport to you. ")
											.append(
													Text.literal("[Click to accept]")
															.styled(style -> style
																	.withClickEvent(new ClickEvent(
																			ClickEvent.Action.RUN_COMMAND,
																			"/tpaccept " + sender.getName().getString()))
																	.withColor(Formatting.GREEN)
																	.withHoverEvent(new HoverEvent(
																			HoverEvent.Action.SHOW_TEXT,
																			Text.literal("Click to accept teleport request")
																	))
															)
											),
									false
							);
							return 1;
						})));

		dispatcher.register(CommandManager.literal("tpahere")
				.then(CommandManager.argument("target", EntityArgumentType.player())
						.executes(context -> {
							ServerCommandSource source = context.getSource();
							ServerPlayerEntity sender = source.getPlayer();
							ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");

							if (sender == null || target == null) return 0;
							if (sender.equals(target)) {
								source.sendFeedback(() -> Text.literal("You can't send a request to yourself!").formatted(Formatting.RED), false);
								return 0;
							}
							if (!canSendRequest(sender)) {
								source.sendFeedback(() -> Text.literal("You must wait before sending another request").formatted(Formatting.RED), false);
								return 0;
							}

							pendingRequestsHere.put(target, sender);
							lastRequestTimes.put(sender, System.currentTimeMillis());

							source.sendFeedback(() -> Text.literal("Requested " + target.getName().getString() + " to teleport to you"), false);
							target.sendMessage(
									Text.literal(sender.getName().getString() + " wants you to teleport to them. ")
											.append(
													Text.literal("[Click to accept]")
															.styled(style -> style
																	.withClickEvent(new ClickEvent(
																			ClickEvent.Action.RUN_COMMAND,
																			"/tpaccept " + sender.getName().getString()))
																	.withColor(Formatting.GREEN)
																	.withHoverEvent(new HoverEvent(
																			HoverEvent.Action.SHOW_TEXT,
																			Text.literal("Click to accept teleport request")
																	))
															)
											),
									false
							);
							return 1;
						})));

		dispatcher.register(CommandManager.literal("tpaccept")
				.executes(context -> {
					ServerCommandSource source = context.getSource();
					ServerPlayerEntity acceptor = source.getPlayer();

					if (acceptor == null) return 0;

					MutableText message = Text.literal("Pending teleport requests:");
					boolean hasRequests = false;

					for (Map.Entry<ServerPlayerEntity, ServerPlayerEntity> entry : pendingRequestsTo.entrySet()) {
						if (entry.getValue().equals(acceptor)) {
							hasRequests = true;
							message.append(Text.literal("\n- "))
									.append(Text.literal(entry.getKey().getName().getString())
											.styled(style -> style
													.withClickEvent(new ClickEvent(
															ClickEvent.Action.RUN_COMMAND,
															"/tpaccept " + entry.getKey().getName().getString()))
													.withColor(Formatting.GREEN)
													.withHoverEvent(new HoverEvent(
															HoverEvent.Action.SHOW_TEXT,
															Text.literal("Click to let them teleport to you")
													))
											))
									.append(Text.literal(" (wants to come to you)"));
						}
					}

					if (pendingRequestsHere.containsKey(acceptor)) {
						hasRequests = true;
						ServerPlayerEntity requester = pendingRequestsHere.get(acceptor);
						message.append("\n-")
								.append(Text.literal(requester.getName().getString() + " (wants you to go to them)")
										.styled(style -> style
												.withClickEvent(new ClickEvent(
														ClickEvent.Action.RUN_COMMAND,
														"/tpaccept " + requester.getName().getString()))
												.withColor(Formatting.GREEN)
												.withHoverEvent(new HoverEvent(
														HoverEvent.Action.SHOW_TEXT,
														Text.literal("Click to teleport to them")
												))))
								.append(Text.literal(" (wants you to go to them)"));
					}

					if (hasRequests) {
						source.sendFeedback(() -> message, false);
						return 1;
					}

					source.sendFeedback(() -> Text.literal("You have no pending requests").formatted(Formatting.RED), false);
					return 0;
				})
				.then(CommandManager.argument("requester", EntityArgumentType.player())
						.suggests(new TpacceptSuggestionProvider(pendingRequestsTo, pendingRequestsHere))
						.executes(context -> {
							ServerCommandSource source = context.getSource();
							ServerPlayerEntity acceptor = source.getPlayer();
							ServerPlayerEntity requester = EntityArgumentType.getPlayer(context, "requester");

							if (acceptor == null || requester == null) return 0;

							if (pendingRequestsTo.containsKey(requester) &&
									pendingRequestsTo.get(requester).equals(acceptor)) {

								requester.teleport(
										acceptor.getServerWorld(),
										acceptor.getX(),
										acceptor.getY(),
										acceptor.getZ(),
										acceptor.getYaw(),
										acceptor.getPitch()
								);

								source.sendFeedback(() -> Text.literal(requester.getName().getString() + " has teleported to you"), false);
								requester.sendMessage(Text.literal("You have been teleported to " + acceptor.getName().getString()), false);

								pendingRequestsTo.remove(requester);
								return 1;
							}

							if (pendingRequestsHere.containsKey(acceptor) &&
									pendingRequestsHere.get(acceptor).equals(requester)) {

								acceptor.teleport(
										requester.getServerWorld(),
										requester.getX(),
										requester.getY(),
										requester.getZ(),
										requester.getYaw(),
										requester.getPitch()
								);

								source.sendFeedback(() -> Text.literal("You have teleported to " + requester.getName().getString()), false);
								requester.sendMessage(Text.literal(acceptor.getName().getString() + " has teleported to you"), false);

								pendingRequestsHere.remove(acceptor);
								return 1;
							}

							source.sendFeedback(() -> Text.literal("No active request from " + requester.getName().getString())
									.formatted(Formatting.RED), false);
							return 0;
						})));
	}

	private static boolean canSendRequest(ServerPlayerEntity player) {
		long currentTime = System.currentTimeMillis();
		return !lastRequestTimes.containsKey(player) ||
				currentTime - lastRequestTimes.get(player) >= 60 * 1000;
	}
}
