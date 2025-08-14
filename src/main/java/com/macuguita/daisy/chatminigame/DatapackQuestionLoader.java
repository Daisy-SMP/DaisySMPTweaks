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

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.macuguita.daisy.DaisyTweaks;
import com.mojang.serialization.JsonOps;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

public class DatapackQuestionLoader implements SimpleSynchronousResourceReloadListener {

	public static final List<ChatMinigame.Question> DATA_QUESTIONS = new ArrayList<>();
	private static final Identifier ID = DaisyTweaks.id("datapack_question_loader");
	private static final Identifier QUESTIONS_DIR = DaisyTweaks.id("chat_minigame_questions");

	@Override
	public Identifier getFabricId() {
		return ID;
	}

	@Override
	public void reload(ResourceManager manager) {
		DATA_QUESTIONS.clear();

		var resources = manager.findResources(QUESTIONS_DIR.getPath(), path -> path.getPath().endsWith(".json"));
		for (var entry : resources.entrySet()) {
			var id = entry.getKey();

			try (var reader = new InputStreamReader(entry.getValue().getInputStream())) {
				var json = JsonHelper.deserialize(reader);

				var result = ChatMinigame.Question.CODEC.parse(JsonOps.INSTANCE, json);
				result.resultOrPartial(error -> DaisyTweaks.LOGGER.warn("Failed to parse question at {}: {}", id, error))
						.ifPresent(DATA_QUESTIONS::add);
			} catch (Exception e) {
				DaisyTweaks.LOGGER.error("Error reading question at {}: {}", id, e.getMessage(), e);
			}
		}
	}
}
