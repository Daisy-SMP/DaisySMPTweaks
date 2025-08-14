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

package com.macuguita.daisy.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.macuguita.daisy.DaisyTweaks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.JsonHelper;

import net.fabricmc.loader.api.FabricLoader;

public class AntiCheatConfig {

	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("daisy/anticheat.json");

	private static Configuration CONFIG = null;

	public static String getWebhookUrl() {
		return CONFIG != null ? CONFIG.webhookUrl : null;
	}

	public static String getAlertMessage() {
		return CONFIG != null ? CONFIG.alertMessage : null;
	}

	public static List<String> getSuspiciousMods() {
		return CONFIG != null ? CONFIG.suspiciousMods : List.of();
	}

	public static void load() {
		try {
			if (!Files.exists(CONFIG_PATH)) {
				createDefaultConfig();
			}

			try (var reader = Files.newBufferedReader(CONFIG_PATH)) {
				var json = JsonHelper.deserialize(reader);
				var result = Configuration.CODEC.parse(JsonOps.INSTANCE, json);

				CONFIG = result.resultOrPartial(msg -> DaisyTweaks.LOGGER.error("[AntiCheatConfig] Config parse error: " + msg)).orElse(null);
			}

		} catch (Exception e) {
			DaisyTweaks.LOGGER.error("[AntiCheatConfig] Failed to load config:");
			e.printStackTrace();
		}
	}

	private static void createDefaultConfig() throws IOException {
		Configuration defaultConfig = new Configuration(
				"PUT_YOUR_WEBHOOK_HERE",
				"joined with suspicious mod(s):",
				List.of("wurst", "meteor-client", "impact", "aristosis", "flux", "salhack", "future-client", "creeper-client", "lambda", "seedcrackerx", "seedcracker")
		);

		var result = Configuration.CODEC.encodeStart(JsonOps.INSTANCE, defaultConfig);
		var jsonElement = result.getOrThrow();

		if (!Files.exists(CONFIG_PATH.getParent())) {
			Files.createDirectories(CONFIG_PATH.getParent());
		}

		writePrettyJson(jsonElement, CONFIG_PATH);
		DaisyTweaks.LOGGER.info("[AntiCheatConfig] Created default config: " + CONFIG_PATH);
	}

	private static void writePrettyJson(JsonElement element, Path path) throws IOException {
		var gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();

		try (Writer writer = Files.newBufferedWriter(path)) {
			gson.toJson(element, writer);
		}
	}

	public static void sendDiscordWebhook(String webhookUrl, String message) {
		try {
			URL url = new URL(webhookUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");

			String jsonPayload = String.format("{\"content\": \"%s\"}", escapeJson(message));

			byte[] postData = jsonPayload.getBytes(StandardCharsets.UTF_8);
			connection.setRequestProperty("Content-Length", String.valueOf(postData.length));

			try (OutputStream os = connection.getOutputStream()) {
				os.write(postData);
			}

			int responseCode = connection.getResponseCode();
			if (responseCode != 204 && responseCode != 200) {
				DaisyTweaks.LOGGER.error("[AntiCheatConfig] Failed to send webhook, response code: " + responseCode);
			}

		} catch (Exception e) {
			DaisyTweaks.LOGGER.error("[AntiCheatConfig] Error sending webhook:");
			e.printStackTrace();
		}
	}

	private static String escapeJson(String message) {
		return message.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "");
	}

	public record Configuration(
			String webhookUrl,
			String alertMessage,
			List<String> suspiciousMods
	) {

		public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("webhook_url").forGetter(Configuration::webhookUrl),
				Codec.STRING.fieldOf("alert_message").forGetter(Configuration::alertMessage),
				Codec.STRING.listOf().fieldOf("suspicious_mods").forGetter(Configuration::suspiciousMods)
		).apply(instance, Configuration::new));
	}
}
