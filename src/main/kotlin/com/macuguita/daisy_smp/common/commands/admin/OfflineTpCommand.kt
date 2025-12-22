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

package com.macuguita.daisy_smp.common.commands.admin

import com.macuguita.daisy_smp.common.commands.CommandRegistrator
import com.macuguita.daisy_smp.common.commands.CommandResult
import com.macuguita.daisy_smp.mixin.data.PlayerListAccessor
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.GameProfileArgument
import net.minecraft.commands.arguments.coordinates.BlockPosArgument
import net.minecraft.nbt.DoubleTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.chat.Component

object OfflineTpCommand : CommandRegistrator {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            literal("offlinetp")
                .requires { it.hasPermission(2) }
                .then(
                    argument("player", GameProfileArgument.gameProfile())
                        .then(
                            argument("pos", BlockPosArgument.blockPos())
                                .executes { ctx ->
                                    val player = GameProfileArgument.getGameProfiles(ctx, "player")
                                        .singleOrNull() ?: return@executes CommandResult.ERROR.value
                                    val pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos")
                                    val server = ctx.source.server

                                    server.playerList.getPlayer(player.id)?.let {
                                        ctx.source.sendFailure(Component.literal("The player has to be offline"))
                                        return@executes CommandResult.ERROR.value
                                    }

                                    val handler = (server.playerList as PlayerListAccessor).`daisy$getPlayerIo`()
                                    handler.`daisy$edit`(player.id) {
                                        it.put(
                                            "Pos",
                                            newDoubleList(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
                                        )
                                        it.putString("Dimension", ctx.source.level.dimension().toString())
                                    }

                                    ctx.source.sendSuccess(
                                        { Component.literal("Teleported ${player.name} to ${pos.x}, ${pos.y}, ${pos.z}") },
                                        true
                                    )
                                    CommandResult.SUCCESS.value
                                }
                        )
                )
        )
    }

    fun newDoubleList(vararg numbers: Double): ListTag {
        val nbtList = ListTag()
        for (d in numbers) {
            nbtList.add(DoubleTag.valueOf(d))
        }

        return nbtList
    }
}
