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

package com.macuguita.daisy.teleports.providers;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class TpacceptSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

	private final Map<ServerPlayerEntity, ServerPlayerEntity> pendingRequestsTo;
	private final Map<ServerPlayerEntity, ServerPlayerEntity> pendingRequestsHere;

	public TpacceptSuggestionProvider(
			Map<ServerPlayerEntity, ServerPlayerEntity> pendingRequestsTo,
			Map<ServerPlayerEntity, ServerPlayerEntity> pendingRequestsHere
	) {
		this.pendingRequestsTo = pendingRequestsTo;
		this.pendingRequestsHere = pendingRequestsHere;
	}

	@Override
	public CompletableFuture<Suggestions> getSuggestions(
			CommandContext<ServerCommandSource> context,
			SuggestionsBuilder builder
	) {
		ServerPlayerEntity acceptor;
		try {
			acceptor = context.getSource().getPlayer();
		} catch (Exception e) {
			return builder.buildFuture();
		}

		String remaining = builder.getRemaining().toLowerCase();

		for (Map.Entry<ServerPlayerEntity, ServerPlayerEntity> entry : pendingRequestsTo.entrySet()) {
			if (entry.getValue().equals(acceptor)) {
				String name = entry.getKey().getName().getString();
				if (name.toLowerCase().startsWith(remaining)) {
					builder.suggest(name);
				}
			}
		}

		if (pendingRequestsHere.containsKey(acceptor)) {
			ServerPlayerEntity requester = pendingRequestsHere.get(acceptor);
			String name = requester.getName().getString();
			if (name.toLowerCase().startsWith(remaining)) {
				builder.suggest(name);
			}
		}

		return builder.buildFuture();
	}
}
