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

package com.macuguita.daisy.admin;

import java.util.Collection;

import com.macuguita.daisy.mixin.admin.PlayerManagerAccessor;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class AdminCommands {

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerCommands(dispatcher);
		});
	}

	private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("offlinetp")
				.requires(context -> context.hasPermissionLevel(2))
				.then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
						.then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
								.executes(context -> {
									Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(context, "player");
									if (profiles.size() != 1) {
										return 0;
									}
									MinecraftServer server = context.getSource().getServer();
									GameProfile profile = profiles.iterator().next();
									BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");

									if (server.getPlayerManager().getPlayer(profile.getId()) != null) {
										context.getSource().sendFeedback(() -> Text.literal("The player has to be offline").formatted(Formatting.RED), false);
										return 0;
									}

									CustomPlayerSaveHandler handler = (CustomPlayerSaveHandler) ((PlayerManagerAccessor) server.getPlayerManager()).daisy$getSaveHandler();
									handler.daisy$edit(profile.getId(), tag -> {
										tag.put("Pos", newDoubleList(pos.getX(), pos.getY(), pos.getZ()));
										tag.putString("Dimension", context.getSource().getWorld().getRegistryKey().toString());
									});
									context.getSource().sendFeedback(() -> Text.literal("Teleported " + profile.getName() + " to: " + " " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), true);
									return 1;
								})
						)
				));

		dispatcher.register(CommandManager.literal("offlineplayerpos")
				.requires(context -> context.hasPermissionLevel(2))
				.then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
						.executes(context -> {
							Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(context, "player");
							if (profiles.size() != 1) {
								return 0;
							}
							MinecraftServer server = context.getSource().getServer();
							GameProfile profile = profiles.iterator().next();

							if (server.getPlayerManager().getPlayer(profile.getId()) != null) {
								context.getSource().sendFeedback(() -> Text.literal("The player has to be offline").formatted(Formatting.RED), false);
								return 0;
							}

							CustomPlayerSaveHandler handler = (CustomPlayerSaveHandler) ((PlayerManagerAccessor) server.getPlayerManager()).daisy$getSaveHandler();
							BlockPos blockPos = handler.daisy$getPos(profile.getId());
							Text text = Text.literal(profile.getName() + " was last seen at ")
									.append(Text.literal("[" + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ() + "]")
											.styled(style -> style
													.withClickEvent(new ClickEvent(
															ClickEvent.Action.RUN_COMMAND,
															"/tp " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ()))
													.withColor(Formatting.GREEN)
													.withHoverEvent(new HoverEvent(
															HoverEvent.Action.SHOW_TEXT,
															Text.literal("Click to be teleported")
													))));

							context.getSource().sendFeedback(() -> text, false);
							return 1;
						})));
	}

	protected static NbtList newDoubleList(double... numbers) {
		NbtList nbtList = new NbtList();
		for (double d : numbers) {
			nbtList.add(NbtDouble.of(d));
		}

		return nbtList;
	}
}
