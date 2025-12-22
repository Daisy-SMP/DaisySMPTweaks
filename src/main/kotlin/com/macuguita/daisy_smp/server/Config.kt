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

import com.macuguita.daisy_smp.common.DaisySMPTweaks
import net.fabricmc.loader.api.FabricLoader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

data class Config(
    val discordWebhookUrl: String,
    val webhookPicture: String,
    val uuids: Set<UUID> = emptySet(),
) {
    companion object {

        private const val FILE_NAME = "daisy_smp.properties"

        @Throws(IOException::class)
        fun loadOrCreate(): Config {
            val configDir = FabricLoader.getInstance().configDir
            val path: Path = configDir.resolve(FILE_NAME)

            Files.createDirectories(configDir)

            val properties = Properties()

            if (Files.exists(path)) {
                Files.newBufferedReader(path).use { reader ->
                    properties.load(reader)
                }
            }

            val config = Config(
                discordWebhookUrl = get(properties, "discordWebhookUrl", ""),
                webhookPicture = get(properties, "webhookPicture", ""),
                uuids = parseUUIDs(get(properties, "uuids", "")),
            )

            Files.newBufferedWriter(path).use { writer ->
                properties.store(writer, "Daisy SMP Config")
            }

            return config
        }

        private fun get(
            properties: Properties,
            key: String,
            defaultValue: String
        ): String {
            if (!properties.containsKey(key)) {
                properties.setProperty(key, defaultValue)
            }
            return properties.getProperty(key)
        }

        private fun parseUUIDs(uuidString: String): Set<UUID> {
            return uuidString.split(',')
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .mapNotNull { parseUUID(it) }
                .toSet()
        }

        private fun parseUUID(uuidString: String): UUID? {
            return try {
                UUID.fromString(uuidString)
            } catch (e: IllegalArgumentException) {
                DaisySMPTweaks.LOGGER.info("Invalid UUID format: $uuidString (must be standard UUID format with dashes)")
                null
            }
        }
    }
}
