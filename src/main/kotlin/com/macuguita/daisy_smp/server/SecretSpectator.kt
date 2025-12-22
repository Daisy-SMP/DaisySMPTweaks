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

import com.macuguita.daisy_smp.mixin.secret_spectator.ClientboundPlayerInfoUpdatePacketAccessor
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameType

@Suppress("KotlinConstantConditions")
object SecretSpectator {

    fun canPlayerSeeSpectatorOf(other: ServerPlayer, player: ServerPlayer): Boolean =
        player == other || canSeeOtherSpectators(player)

    fun canSeeOtherSpectators(player: ServerPlayer): Boolean =
        player.isSpectator || player.hasPermissions(2)

    fun copyPacketWithModifiedEntries(
        packet: ClientboundPlayerInfoUpdatePacket,
        mapper: (ClientboundPlayerInfoUpdatePacket.Entry) -> ClientboundPlayerInfoUpdatePacket.Entry
    ): ClientboundPlayerInfoUpdatePacket {
        val newPacket = ClientboundPlayerInfoUpdatePacket(packet.actions(), emptyList())
        (newPacket as ClientboundPlayerInfoUpdatePacketAccessor).`daisy$setEntries`(packet.entries().map { mapper(it) })
        return newPacket
    }

    fun cloneEntryWithGamemode(
        entry: ClientboundPlayerInfoUpdatePacket.Entry,
        newGamemode: GameType
    ): ClientboundPlayerInfoUpdatePacket.Entry =
        ClientboundPlayerInfoUpdatePacket.Entry(
            entry.profileId,
            entry.profile,
            entry.listed,
            entry.latency,
            newGamemode,
            entry.displayName,
            entry.chatSession
        )

    fun filterPacketForReceiver(
        receiver: ServerPlayer,
        packet: ClientboundPlayerInfoUpdatePacket
    ): ClientboundPlayerInfoUpdatePacket {
        if (canSeeOtherSpectators(receiver)) return packet

        return copyPacketWithModifiedEntries(packet) { entry ->
            val targetIsSpectator =
                entry.gameMode == GameType.SPECTATOR

            if (targetIsSpectator) {
                cloneEntryWithGamemode(entry, GameType.SURVIVAL)
            } else entry
        }
    }

}
