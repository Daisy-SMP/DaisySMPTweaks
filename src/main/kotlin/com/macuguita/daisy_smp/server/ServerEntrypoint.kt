/*
 * Copyright (c) 2025 macuguita
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.macuguita.daisy_smp.server

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.macuguita.daisy_smp.common.DaisySMPTweaks
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.level.ServerPlayer
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object ServerEntrypoint : DedicatedServerModInitializer {

    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()
    private val gson = Gson()

    override fun onInitializeServer() {
        val config = Config.loadOrCreate()

        ConnectionManager.init(config.uuids)

        if (config.discordWebhookUrl.isBlank()) return

        registerEvents(config)
    }

    private fun registerEvents(config: Config) {
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            systemMessage(config, "${handler.player.name.string} joined the server")
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            systemMessage(config, "${handler.player.name.string} left the server")
        }

        ServerMessageEvents.CHAT_MESSAGE.register { chat, sender, _ ->
            playerMessage(config, sender, chat.signedContent().trim())
        }

        ServerLifecycleEvents.SERVER_STARTING.register {
            systemMessage(config, "Server starting...")
        }
        ServerLifecycleEvents.SERVER_STARTED.register {
            systemMessage(config, "Server started.")
        }
        ServerLifecycleEvents.SERVER_STOPPING.register {
            systemMessage(config, "Server stopping...")
        }
        ServerLifecycleEvents.SERVER_STOPPED.register {
            systemMessage(config, "Server stopped.")
        }
    }

    private fun systemMessage(config: Config, message: String) =
        announce("Petal SMP", config.webhookPicture, message, config.discordWebhookUrl)

    private fun playerMessage(config: Config, player: ServerPlayer, message: String) =
        announce(player.name.string, avatarUrl(player), message, config.discordWebhookUrl)

    private fun announce(
        username: String,
        avatarUrl: String,
        message: String,
        webhookUrl: String
    ) {
        val payload = Webhook(
            username = username,
            content = message,
            avatarUrl = avatarUrl,
            allowedMentions = Webhook.AllowedMentions.NONE
        )

        val request = HttpRequest.newBuilder()
            .uri(URI.create(webhookUrl))
            .timeout(Duration.ofSeconds(5))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
            .build()

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
            .thenAccept { }
            .exceptionally {
                DaisySMPTweaks.LOGGER.error("Failed to send Discord webhook", it)
                null
            }
    }

    private fun avatarUrl(player: ServerPlayer) =
        "https://mc-heads.net/avatar/${player.stringUUID}/128"

    // https://discord.com/developers/docs/resources/webhook#execute-webhook
    private data class Webhook(
        val username: String,
        val content: String,
        @SerializedName("avatar_url")
        val avatarUrl: String,
        @SerializedName("allowed_mentions")
        val allowedMentions: AllowedMentions
    ) {
        data class AllowedMentions(val parse: List<Any>) {
            companion object {
                val NONE = AllowedMentions(emptyList())
            }
        }
    }
}
