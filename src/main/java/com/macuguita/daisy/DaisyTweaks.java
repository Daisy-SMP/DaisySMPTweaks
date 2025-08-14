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

package com.macuguita.daisy;

import java.util.List;

import com.macuguita.daisy.chatminigame.ChatMinigame;
import com.macuguita.daisy.commands.chatminigame.ChatMinigameCommands;
import com.macuguita.daisy.chatminigame.ChatMinigameConfig;
import com.macuguita.daisy.chatminigame.DatapackQuestionLoader;
import com.macuguita.daisy.client.payload.AntiCheatPayloadC2S;
import com.macuguita.daisy.client.payload.AntiCheatPayloadS2C;
import com.macuguita.daisy.commands.admin.AdminCommands;
import com.macuguita.daisy.commands.teleports.HomeCommands;
import com.macuguita.daisy.commands.teleports.TpaCommands;
import com.macuguita.daisy.commands.teleports.WarpCommands;
import com.macuguita.daisy.reg.DaisyObjects;
import com.macuguita.daisy.utils.AntiCheatConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;

public class DaisyTweaks implements ModInitializer {

	public static final String MOD_ID = "daisy";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier ANTI_CHEAT_C2S_PAYLOAD = id("hacked_client_c2s");
	public static Identifier ANTI_CHEAT_S2C_PAYLOAD = id("hacked_client_s2c");

	@Override
	public void onInitialize() {
		DaisyObjects.init();
		PayloadTypeRegistry.playC2S().register(AntiCheatPayloadC2S.ID, AntiCheatPayloadC2S.CODEC);
		PayloadTypeRegistry.playS2C().register(AntiCheatPayloadS2C.ID, AntiCheatPayloadS2C.CODEC);
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) AntiCheatConfig.load();
		ChatMinigameConfig.load();
		ResourceManagerHelper.get(ResourceType.SERVER_DATA)
				.registerReloadListener(new DatapackQuestionLoader());
		ChatMinigame.init();
		ChatMinigameCommands.init();
		AdminCommands.init();
		HomeCommands.init();
		TpaCommands.init();
		WarpCommands.init();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			List<String> suspiciousMods = AntiCheatConfig.getSuspiciousMods();

			ServerPlayNetworking.send(handler.getPlayer(), new AntiCheatPayloadS2C(suspiciousMods));
		});

		ServerPlayNetworking.registerGlobalReceiver(AntiCheatPayloadC2S.ID, ((antiCheatPayloadC2S, context) -> {
			ServerPlayerEntity player = context.player();
			Text playerDisplayName = player.getDisplayName();
			if (!antiCheatPayloadC2S.susMods().isEmpty()) {
				context.server().execute(() -> {
					String webhook = AntiCheatConfig.getWebhookUrl();
					String alertMessage = AntiCheatConfig.getAlertMessage();
					StringBuilder sb = new StringBuilder();
					if (webhook != null && alertMessage != null && webhook.startsWith("http") && playerDisplayName != null) {
						sb.append(playerDisplayName.getString());
						sb.append(' ');
						sb.append(alertMessage);
						sb.append(' ');
						sb.append(antiCheatPayloadC2S.susMods().size() == 1
								? "**" + antiCheatPayloadC2S.susMods().get(0) + "**"
								: "**" + String.join("**, **", antiCheatPayloadC2S.susMods()) + "**");
						AntiCheatConfig.sendDiscordWebhook(webhook, String.format(sb.toString()));
					}
				});
			}
		}));
	}

	public static Identifier id(String name) {
		return Identifier.of(MOD_ID, name);
	}
}
