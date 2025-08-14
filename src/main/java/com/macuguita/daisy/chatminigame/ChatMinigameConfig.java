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

package com.macuguita.daisy.chatminigame;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.macuguita.daisy.DaisyTweaks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.JsonHelper;

import net.fabricmc.loader.api.FabricLoader;

public class ChatMinigameConfig {

	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("daisy/chat_minigames.json");

	private static Configuration CONFIG = null;

	public static boolean getEnabled(QuestionType type) {
		return CONFIG != null ? CONFIG.enabledQuestions.get(type) : false;
	}

	public static String getQuestionIcon(QuestionType type) {
		return CONFIG != null ? CONFIG.questionIcons.get(type) : null;
	}

	public static String getQuestionPrompt(QuestionType type) {
		return CONFIG != null ? CONFIG.questionPrompt.get(type) : null;
	}

	public static String getForbiddenRegex() {
		return CONFIG != null ? CONFIG.forbiddenRegex : ".^";
	}

	public static List<String> getForbiddenModIds() {
		return CONFIG != null ? CONFIG.forbiddenModIds : List.of();
	}

	public static void load() {
		try {
			if (!Files.exists(CONFIG_PATH)) {
				createDefaultConfig();
			}

			try (var reader = Files.newBufferedReader(CONFIG_PATH)) {
				var json = JsonHelper.deserialize(reader); // no need for Gson parser here
				var result = Configuration.CODEC.parse(JsonOps.INSTANCE, json);

				CONFIG = result.resultOrPartial(msg -> DaisyTweaks.LOGGER.error("[Chat Minigames] Config parse error: " + msg)).orElse(null);
			}

		} catch (Exception e) {
			DaisyTweaks.LOGGER.error("[Chat Minigames] Failed to load config:");
			e.printStackTrace();
		}
	}

	private static void createDefaultConfig() throws IOException {
		Configuration defaultConfig = new Configuration(
				Map.of(
						QuestionType.UNSCRAMBLE_ITEM, true,
						QuestionType.FILL_IN_THE_BLANKS, true,
						QuestionType.REVERSE_ITEM, true,
						QuestionType.DATA_DRIVEN, true
				),
				Map.of(
						QuestionType.UNSCRAMBLE_ITEM, "🌀",
						QuestionType.FILL_IN_THE_BLANKS, "✏",
						QuestionType.REVERSE_ITEM, "🔁",
						QuestionType.DATA_DRIVEN, "❓"
				),
				Map.of(
						QuestionType.UNSCRAMBLE_ITEM, "Unscramble this Minecraft item: ",
						QuestionType.FILL_IN_THE_BLANKS, "Fill in this Minecraft item: ",
						QuestionType.REVERSE_ITEM, "What Minecraft item is this when reversed? "
				),
				"(creative|debug)",
				List.of("everycomp", "stonezone", "gemsrealm")
		);

		var result = Configuration.CODEC.encodeStart(JsonOps.INSTANCE, defaultConfig);
		var jsonElement = result.getOrThrow();

		if (!Files.exists(CONFIG_PATH.getParent())) {
			Files.createDirectories(CONFIG_PATH.getParent());
		}

		writePrettyJson(jsonElement, CONFIG_PATH);
		DaisyTweaks.LOGGER.info("[Chat Minigames] Created default config: " + CONFIG_PATH);
	}

	private static void writePrettyJson(JsonElement element, Path path) throws IOException {
		var gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();

		try (Writer writer = Files.newBufferedWriter(path)) {
			gson.toJson(element, writer);
		}
	}


	public record Configuration(
			Map<QuestionType, Boolean> enabledQuestions,
			Map<QuestionType, String> questionIcons,
			Map<QuestionType, String> questionPrompt,
			String forbiddenRegex,
			List<String> forbiddenModIds
	) {

		public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.unboundedMap(QuestionType.CODEC, Codec.BOOL)
						.fieldOf("enabled_questions")
						.forGetter(Configuration::enabledQuestions),
				Codec.unboundedMap(QuestionType.CODEC, Codec.STRING)
						.fieldOf("question_icons")
						.forGetter(Configuration::questionIcons),
				Codec.unboundedMap(QuestionType.CODEC, Codec.STRING)
						.fieldOf("question_prompt")
						.forGetter(Configuration::questionPrompt),
				Codec.STRING
						.fieldOf("forbidden_regex")
						.forGetter(Configuration::forbiddenRegex),
				Codec.STRING
						.listOf()
						.fieldOf("forbidden_modids")
						.forGetter(Configuration::forbiddenModIds)
		).apply(instance, Configuration::new));
	}
}
