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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Random;
import java.util.function.Supplier;

import com.macuguita.daisy.components.DaisyComponents;
import com.macuguita.daisy.components.NoMinigamesComponent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

public class ChatMinigame {

	private static final Random RANDOM = new Random();
	private static final Queue<Question> shuffledDataQuestions = new ArrayDeque<>();
	private static int lastAskedMinute = -1;
	private static Question currentQuestion = null;
	private static QuestionType lastQuestionType = null;

	public static void init() {
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			Calendar calendar = Calendar.getInstance();
			int minute = calendar.get(Calendar.MINUTE);
			int second = calendar.get(Calendar.SECOND);

			if ((minute == 0 || minute == 30) && second < 5 && minute != lastAskedMinute) {
				if (server.getCurrentPlayerCount() > 0) {
					askRandomQuestion(server, false);
					lastAskedMinute = minute;
				}
			}
		});

		ServerMessageEvents.CHAT_MESSAGE.register((SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params) -> {
			Text playerDisplayName = sender.getDisplayName();
			NoMinigamesComponent noMinigameComponent = DaisyComponents.NO_MINIGAMES_COMPONENT.get(sender.getScoreboard());
			if (playerDisplayName != null && !noMinigameComponent.getDisabledPlayers().contains(sender.getUuid())) {
				String raw = message.getContent().getString().trim().toLowerCase(Locale.ROOT);

				if (currentQuestion != null && currentQuestion.isCorrect(raw)) {
					ItemStack prizeBagStack = new ItemStack(Items.DIAMOND);
					if (!sender.getInventory().insertStack(prizeBagStack)) {
						sender.dropItem(prizeBagStack, true);
					}
					Text textMessage = Text.empty()
							.append(Text.literal(sender.getDisplayName().getString()).formatted(Formatting.YELLOW))
							.append(Text.literal(" got it right! The answer was: "))
							.append(Text.literal(formatLists(currentQuestion.acceptableAnswers)).formatted(Formatting.YELLOW));
					serverBroadcast(sender.getServer(), textMessage);

					currentQuestion = null;
				}
			}
		});
	}

	private static String formatLists(List<String> input) {
		return input.size() == 1
				? input.get(0)  // Only one answer
				: String.join(", ", input);  // Multiple answers
	}

	public static void askRandomQuestion(MinecraftServer server, boolean showAnswer) {
		List<Supplier<Question>> generators = List.of(
				() -> generateUnscrambleQuestion(RANDOM),
				() -> generateFillInTheBlanksQuestion(RANDOM),
				() -> generateReverseItemQuestion(RANDOM),
				() -> getRandomDatapackQuestion(RANDOM)
		);

		List<Supplier<Question>> filtered = generators.stream()
				.filter(supplier -> {
					Question test = supplier.get();
					return test != null && test.type() != lastQuestionType && ChatMinigameConfig.getEnabled(test.type);
				})
				.toList();

		if (filtered.isEmpty()) return;

		Supplier<Question> selectedSupplier = filtered.get(RANDOM.nextInt(filtered.size()));
		Question selected = selectedSupplier.get();

		if (selected != null) {
			currentQuestion = selected;
			lastQuestionType = selected.type();
			serverBroadcast(server, selected.promptText());

			if (showAnswer) {
				serverBroadcastToOps(server, Text.literal("Question answer is: ").
						append(Text.literal(formatLists(selected.acceptableAnswers)).formatted(Formatting.YELLOW)));
			}
		}
	}

	// -- Question Generator (type: unscramble item) --

	private static Question generateUnscrambleQuestion(Random random) {
		String displayName = getRandomItemName(random);
		String prompt = scrambleWordsKeepingSpaces(displayName);

		return new Question(QuestionType.UNSCRAMBLE_ITEM, prompt, List.of(displayName));
	}

	// -- Question Generator (type: fill in the blanks) --

	private static Question generateFillInTheBlanksQuestion(Random random) {
		String displayName = getRandomItemName(random);
		String prompt = blankOutLetters(displayName, random);

		return new Question(QuestionType.FILL_IN_THE_BLANKS, prompt, List.of(displayName));
	}

	// -- Question Generator (type: unreverse) --

	private static Question generateReverseItemQuestion(Random random) {
		String displayName = getRandomItemName(random);
		String prompt = new StringBuilder(displayName).reverse().toString();

		return new Question(QuestionType.REVERSE_ITEM, prompt, List.of(displayName));
	}

	// -- Question Generator (type: datapack) --

	private static Question getRandomDatapackQuestion(Random random) {
		if (shuffledDataQuestions.isEmpty()) {
			List<Question> pool = DatapackQuestionLoader.DATA_QUESTIONS;
			if (pool.isEmpty()) return null;

			List<Question> shuffled = new ArrayList<>(pool);
			Collections.shuffle(shuffled, random);
			shuffledDataQuestions.addAll(shuffled);
		}

		Question selectedQuestion = shuffledDataQuestions.poll();
		if (selectedQuestion != null) {
			return new Question(selectedQuestion.type(), selectedQuestion.prompt(), selectedQuestion.acceptableAnswers());
		} else {
			return null;
		}
	}

	// -- Question Helpers --

	public static String getAnswer() {
		return formatLists(currentQuestion.acceptableAnswers);
	}

	private static Item getRandomItem(Random random) {
		List<String> forbiddenModIds = ChatMinigameConfig.getForbiddenModIds();
		List<Identifier> allowedIds = Registries.ITEM.getIds().stream()
				.filter(id -> forbiddenModIds.stream().noneMatch(modid -> id.getNamespace().contains(modid)))
				.toList();

		return Registries.ITEM.get(allowedIds.get(random.nextInt(allowedIds.size())));
	}

	private static String getRandomItemName(Random random) {
		Item item = getRandomItem(random);
		String rawId = Registries.ITEM.getId(item).getPath();

		if (rawId.length() <= 3 || rawId.matches(ChatMinigameConfig.getForbiddenRegex())) {
			return getRandomItemName(random);
		}

		return capitalizeWords(rawId.replace("_", " "));
	}

	private static String scrambleWordsKeepingSpaces(String input) {
		String[] words = input.split(" ");
		StringBuilder scrambled = new StringBuilder();

		for (String word : words) {
			List<Character> letters = new ArrayList<>();
			for (char c : word.toCharArray()) {
				letters.add(c);
			}
			Collections.shuffle(letters);
			for (char c : letters) {
				scrambled.append(c);
			}
			scrambled.append(" ");
		}

		return scrambled.toString().trim();
	}

	private static String capitalizeWords(String string) {
		char[] chars = string.toLowerCase(Locale.ROOT).toCharArray();
		boolean capitalizeNext = true;

		for (int i = 0; i < chars.length; i++) {
			if (Character.isWhitespace(chars[i])) {
				capitalizeNext = true;
			} else if (capitalizeNext) {
				chars[i] = Character.toUpperCase(chars[i]);
				capitalizeNext = false;
			}
		}

		return new String(chars);
	}

	private static String blankOutLetters(String input, Random random) {
		StringBuilder result = new StringBuilder();
		for (char c : input.toCharArray()) {
			if (Character.isLetter(c) && random.nextBoolean()) {
				result.append('_');
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	private static void serverBroadcast(MinecraftServer server, String message) {
		serverBroadcast(server, Text.literal(message));
	}

	private static void serverBroadcast(MinecraftServer server, Text message) {
		if (server != null) {
			NoMinigamesComponent noMinigameComponent = DaisyComponents.NO_MINIGAMES_COMPONENT.get(server.getScoreboard());
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				if (!noMinigameComponent.getDisabledPlayers().contains(player.getUuid())) player.sendMessage(message);
			}
		}
	}

	private static void serverBroadcastToOps(MinecraftServer server, String message) {
		serverBroadcastToOps(server, Text.literal(message));
	}

	private static void serverBroadcastToOps(MinecraftServer server, Text message) {
		if (server != null) {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				if (server.getPlayerManager().isOperator(player.getGameProfile())) player.sendMessage(message);
			}
		}
	}

	// -- Question Record --

	public record Question(QuestionType type, String prompt, List<String> acceptableAnswers) {

		public static final Codec<Question> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.STRING.fieldOf("prompt").forGetter(Question::prompt),
				Codec.STRING.listOf().fieldOf("answers").forGetter(Question::acceptableAnswers)
		).apply(i, (prompt, answers) -> new Question(QuestionType.DATA_DRIVEN, prompt, answers)));

		public boolean isCorrect(String input) {
			String norm = normalize(input);
			return acceptableAnswers.stream().anyMatch(ans -> normalize(ans).equals(norm));
		}

		private String normalize(String s) {
			return s.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
		}

		public Text promptText() {
			MutableText text = Text.empty();

			text.append(Text.literal("\n" + ChatMinigameConfig.getQuestionIcon(type) + " ").formatted(Formatting.YELLOW));

			if (type == QuestionType.DATA_DRIVEN) {
				text.append(Text.literal(prompt));
			} else {
				String questionPrompt = ChatMinigameConfig.getQuestionPrompt(type);
				if (questionPrompt != null) {
					text.append(Text.literal(questionPrompt).formatted(Formatting.WHITE))
							.append(Text.literal(prompt).formatted(Formatting.YELLOW));
				}
			}

			return text.append(Text.literal("\n"));
		}
	}
}
