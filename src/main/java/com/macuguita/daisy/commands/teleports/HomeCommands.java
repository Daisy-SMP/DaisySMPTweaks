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

package com.macuguita.daisy.commands.teleports;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.macuguita.daisy.commands.admin.CustomPlayerSaveHandler;
import com.macuguita.daisy.commands.teleports.providers.HomeSuggestionProvider;
import com.macuguita.daisy.components.DaisyComponents;
import com.macuguita.daisy.components.HomesComponent;
import com.macuguita.daisy.mixin.admin.PlayerManagerAccessor;
import com.macuguita.daisy.utils.HomeLocation;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class HomeCommands {

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerCommands(dispatcher);
		});
	}

	private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("sethome")
				.requires(source -> source.hasPermissionLevel(0))
				.executes(context -> {
					ServerCommandSource source = context.getSource();
					ServerPlayerEntity player = source.getPlayer();
					if (player != null) {
						HomesComponent homesComponent = DaisyComponents.HOMES_COMPONENT.get(player);
						int homeCount = homesComponent.getAllHomes().size();
						int maxHomes = homesComponent.getMaxHomes();

						if (homeCount < maxHomes) {
							RegistryKey<World> dimension = player.getWorld().getRegistryKey();
							if (homesComponent.getHome("home") == null) {
								homesComponent.addHome("home", player.getBlockPos(), dimension);
								source.sendFeedback(() -> Text.literal("Home set successfully"), false);
								return 1;
							} else {
								source.sendFeedback(() -> Text.literal("A home with that name already exists").formatted(Formatting.RED), false);
								return 0;
							}
						} else {
							source.sendFeedback(() -> Text.literal("You have reached the maximum number of homes (" + maxHomes + ")").formatted(Formatting.RED), false);
							return 0;
						}
					}
					return 0;
				})
				.then(CommandManager.argument("name", StringArgumentType.string())
						.executes(context -> {
							String name = StringArgumentType.getString(context, "name");
							ServerCommandSource source = context.getSource();
							ServerPlayerEntity player = source.getPlayer();
							if (player != null) {
								HomesComponent homesComponent = DaisyComponents.HOMES_COMPONENT.get(player);
								int homeCount = homesComponent.getAllHomes().size();
								int maxHomes = homesComponent.getMaxHomes();

								if (homeCount < maxHomes) {
									RegistryKey<World> dimension = player.getWorld().getRegistryKey();
									if (homesComponent.getHome(name) == null) {
										homesComponent.addHome(name, player.getBlockPos(), dimension);
										source.sendFeedback(() -> Text.literal("Home '" + name + "' set successfully"), false);
										return 1;
									} else {
										source.sendFeedback(() -> Text.literal("A home with that name already exists").formatted(Formatting.RED), false);
										return 0;
									}
								} else {
									source.sendFeedback(() -> Text.literal("You have reached the maximum number of homes (" + maxHomes + ")").formatted(Formatting.RED), false);
									return 0;
								}
							}
							return 0;
						})));

		dispatcher.register(CommandManager.literal("home")
				.requires(source -> source.hasPermissionLevel(0))
				.executes(context -> {
					ServerCommandSource source = context.getSource();
					ServerPlayerEntity player = source.getPlayer();
					if (player != null && DaisyComponents.HOMES_COMPONENT.get(player).getHome("home") != null) {
						HomeLocation home = DaisyComponents.HOMES_COMPONENT.get(player).getHome("home");
						player.teleport(source.getServer().getWorld(home.getDimension()), home.getPosition().toCenterPos().getX(), home.getPosition().getY(), home.getPosition().toCenterPos().getZ(), player.getYaw(), player.getPitch());
						source.sendFeedback(() -> Text.literal("You've been teleported home"), false);
						return 1;
					} else {
						source.sendFeedback(() -> Text.literal("Couldn't find home").formatted(Formatting.RED), false);
						return 0;
					}
				})
				.then(CommandManager.argument("name", StringArgumentType.string())
						.suggests(new HomeSuggestionProvider())
						.executes(context -> {
							String name = StringArgumentType.getString(context, "name");
							ServerCommandSource source = context.getSource();
							ServerPlayerEntity player = source.getPlayer();
							if (player != null && DaisyComponents.HOMES_COMPONENT.get(player).getHome(name) != null) {
								HomeLocation home = DaisyComponents.HOMES_COMPONENT.get(player).getHome(name);
								player.teleport(source.getServer().getWorld(home.getDimension()), home.getPosition().toCenterPos().getX(), home.getPosition().getY(), home.getPosition().toCenterPos().getZ(), player.getYaw(), player.getPitch());
								source.sendFeedback(() -> Text.literal("You've been teleported to " + name), false);
								return 1;
							} else {
								source.sendFeedback(() -> Text.literal("Couldn't find home").formatted(Formatting.RED), false);
								return 0;
							}
						})));

		dispatcher.register(CommandManager.literal("homes")
				.requires(source -> source.hasPermissionLevel(0))
				.executes(context -> {
					ServerPlayerEntity player = context.getSource().getPlayer();
					if (player != null) {
						Set<String> homeNames = DaisyComponents.HOMES_COMPONENT.get(player).getAllHomes().keySet();
						if (homeNames.isEmpty()) {
							context.getSource().sendFeedback(() -> Text.literal("You don't have any homes!").formatted(Formatting.RED), false);
						} else {
							String homesList = String.join(", ", homeNames);
							context.getSource().sendFeedback(() -> Text.literal("Your homes are: " + homesList), false);
						}
						return 1;
					}
					return 0;
				}));

		dispatcher.register(CommandManager.literal("delhome")
				.requires(source -> source.hasPermissionLevel(0))
				.executes(context -> {
					ServerCommandSource source = context.getSource();
					source.sendFeedback(() -> Text.literal("Couldn't delete home").formatted(Formatting.RED), false);
					return 0;
				})
				.then(CommandManager.argument("name", StringArgumentType.string())
						.suggests(new HomeSuggestionProvider())
						.executes(context -> {
							String name = StringArgumentType.getString(context, "name");
							ServerCommandSource source = context.getSource();
							ServerPlayerEntity player = source.getPlayer();
							if (player != null && DaisyComponents.HOMES_COMPONENT.get(player).getHome(name) != null) {
								DaisyComponents.HOMES_COMPONENT.get(player).removeHome(name);
								source.sendFeedback(() -> Text.literal("Deleted: " + name), false);
								return 1;
							} else {
								source.sendFeedback(() -> Text.literal("Couldn't delete home").formatted(Formatting.RED), false);
								return 0;
							}
						})));

		dispatcher.register(CommandManager.literal("setmaxhomes")
				.requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.argument("player", EntityArgumentType.player())
						.then(CommandManager.argument("number", IntegerArgumentType.integer(0))
								.executes(context -> {
									ServerCommandSource source = context.getSource();
									ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "player");
									int maxHomes = IntegerArgumentType.getInteger(context, "number");

									if (targetPlayer == null) {
										source.sendFeedback(() -> Text.literal("Couldn't find player").formatted(Formatting.RED), false);
										return 0;
									}
									HomesComponent homesComponent = DaisyComponents.HOMES_COMPONENT.get(targetPlayer);

									homesComponent.setMaxHomes(maxHomes);

									source.sendFeedback(() -> Text.literal("Set " + targetPlayer.getName().getString() + "'s max amount of homes to " + maxHomes), false);
									return 1;
								}))));

		dispatcher.register(CommandManager.literal("playerhomes")
				.requires(context -> context.hasPermissionLevel(2))
				.then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
						.executes(context -> {
							ServerCommandSource source = context.getSource();
							Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(context, "player");
							if (profiles.size() != 1) {
								return 0;
							}
							MinecraftServer server = context.getSource().getServer();
							GameProfile profile = profiles.iterator().next();
							Map<String, HomeLocation> homes = new HashMap<>();

							if (server.getPlayerManager().getPlayer(profile.getId()) != null) {
								ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(profile.getId());
								if (playerEntity == null) {
									source.sendFeedback(() -> Text.literal("Couldn't find player").formatted(Formatting.RED), false);
									return 0;
								}
								HomesComponent homesComponent = DaisyComponents.HOMES_COMPONENT.get(playerEntity);
								homes = homesComponent.getAllHomes();
							} else {
								CustomPlayerSaveHandler handler = (CustomPlayerSaveHandler) ((PlayerManagerAccessor) server.getPlayerManager()).daisy$getSaveHandler();
								NbtCompound nbt = handler.daisy$getNbt(profile.getId());

								NbtCompound componentsNbt = nbt.getCompound("cardinal_components");
								if (componentsNbt.contains("daisy:homes", NbtElement.COMPOUND_TYPE)) {
									NbtCompound homesNbt = componentsNbt.getCompound("daisy:homes");
									NbtList homesNbtList = homesNbt.getList("Homes", NbtElement.COMPOUND_TYPE);

									for (int j = 0; j < homesNbtList.size(); j++) {
										NbtCompound homeTag = homesNbtList.getCompound(j);
										String name = homeTag.getString("Name");
										int x = homeTag.getInt("X");
										int y = homeTag.getInt("Y");
										int z = homeTag.getInt("Z");
										String dimensionId = homeTag.getString("Dimension");
										RegistryKey<World> dimension = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dimensionId));
										homes.put(name, new HomeLocation(new BlockPos(x, y, z), dimension));
									}
								} else {
									source.sendFeedback(() -> Text.literal("Couldn't find nbt compound for " + profile.getName()).formatted(Formatting.RED), false);
									return 0;
								}
							}

							MutableText text = Text.literal(profile.getName() + "'s Homes:");
							for (var home : homes.entrySet()) {
								text.append(Text.literal("\n" + home.getKey() + ": ")
										.append(Text.literal(home.getValue().getDimension().getValue() + " [" + home.getValue().getPosition().getX() + ", " + home.getValue().getPosition().getY() + ", " + home.getValue().getPosition().getZ() + "]")
												.styled(style -> style
														.withClickEvent(new ClickEvent(
																ClickEvent.Action.RUN_COMMAND,
																"/execute in " + home.getValue().getDimension().getValue() + " run tp @s " + home.getValue().getPosition().getX() + " " + home.getValue().getPosition().getY() + " " + home.getValue().getPosition().getZ()))
														.withColor(Formatting.GREEN)
														.withHoverEvent(new HoverEvent(
																HoverEvent.Action.SHOW_TEXT,
																Text.literal("Click to teleport")
														)))));
							}
							source.sendFeedback(() -> text, false);
							return 1;
						})));
	}
}
