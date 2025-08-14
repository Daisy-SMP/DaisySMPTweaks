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

import com.macuguita.daisy.components.DaisyComponents;
import com.macuguita.daisy.components.WarpsComponent;
import com.macuguita.daisy.teleports.providers.WarpSuggestionProvider;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class WarpCommands {

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerCommands(dispatcher);
		});
	}

	private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("spawn")
				.requires(source -> source.hasPermissionLevel(0))
				.executes(context -> {
					ServerCommandSource source = context.getSource();
					ServerPlayerEntity player = source.getPlayer();
					if (player != null) {
						ServerWorld overworld = source.getServer().getOverworld();
						Vec3d spawnPos = Vec3d.ofBottomCenter(overworld.getSpawnPos());

						player.teleport(overworld, spawnPos.x, spawnPos.y, spawnPos.z, player.getYaw(), player.getPitch());

						return 1;
					} else {
						return 0;
					}
				}));

		dispatcher.register(CommandManager.literal("addwarp")
				.requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.argument("name", StringArgumentType.string())
						.executes(context -> {
							String name = StringArgumentType.getString(context, "name");
							ServerCommandSource source = context.getSource();
							ServerPlayerEntity player = source.getPlayer();
							if (player != null) {
								WarpsComponent warpsComponent = DaisyComponents.WARPS_COMPONENT.get(source.getServer().getScoreboard());

								RegistryKey<World> dimension = player.getWorld().getRegistryKey();
								if (warpsComponent.getWarp(name) == null) {
									warpsComponent.addWarp(name, player.getBlockPos(), dimension);
									source.sendFeedback(() -> Text.literal("Warp '" + name + "' set successfully"), false);
									return 1;
								} else {
									source.sendFeedback(() -> Text.literal("Warp '" + name + "' already exists").formatted(Formatting.RED), false);
									return 0;
								}
							}
							return 0;
						})));

		dispatcher.register(CommandManager.literal("removewarp")
				.requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.argument("name", StringArgumentType.string())
						.suggests(new WarpSuggestionProvider())
						.executes(context -> {
							String name = StringArgumentType.getString(context, "name");
							ServerCommandSource source = context.getSource();
							ServerPlayerEntity player = source.getPlayer();
							if (player != null) {
								WarpsComponent warpsComponent = DaisyComponents.WARPS_COMPONENT.get(source.getServer().getScoreboard());

								if (warpsComponent.getWarp(name) != null) {
									warpsComponent.removeWarp(name);
									source.sendFeedback(() -> Text.literal("Deleted: " + name), false);
									return 1;
								} else {
									source.sendFeedback(() -> Text.literal("Warp '" + name + "' doesn't exist").formatted(Formatting.RED), false);
									return 0;
								}
							}
							return 0;
						})));

		dispatcher.register(CommandManager.literal("warp")
				.requires(source -> source.hasPermissionLevel(0))
				.then(CommandManager.argument("name", StringArgumentType.string())
						.suggests(new WarpSuggestionProvider())
						.executes(context -> {
							String name = StringArgumentType.getString(context, "name");
							ServerCommandSource source = context.getSource();
							ServerPlayerEntity player = source.getPlayer();

							WarpsComponent warpsComponent = DaisyComponents.WARPS_COMPONENT.get(source.getServer().getScoreboard());
							if (player != null && warpsComponent.getWarp(name) != null) {
								HomeLocation warp = warpsComponent.getWarp(name);
								player.teleport(source.getServer().getWorld(warp.getDimension()), warp.getPosition().toCenterPos().getX(), warp.getPosition().getY(), warp.getPosition().toCenterPos().getZ(), player.getYaw(), player.getPitch());
								source.sendFeedback(() -> Text.literal("You've been teleported to " + name), false);
								return 1;
							} else {
								source.sendFeedback(() -> Text.literal("Couldn't find warp").formatted(Formatting.RED), false);
								return 0;
							}
						})));
	}
}
