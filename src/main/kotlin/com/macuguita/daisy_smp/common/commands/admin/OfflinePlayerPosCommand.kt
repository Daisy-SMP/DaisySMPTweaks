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
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.GameProfileArgument
import net.minecraft.core.BlockPos
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent


object OfflinePlayerPosCommand : CommandRegistrator {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            literal("offlineplayerpos")
                .requires { it.hasPermission(2) }
                .then(
                    argument("player", GameProfileArgument.gameProfile())
                        .executes { ctx ->
                            val player = GameProfileArgument.getGameProfiles(ctx, "player").singleOrNull()
                                ?: return@executes CommandResult.ERROR.value
                            val server = ctx.source.server

                            server.playerList.getPlayer(player.id)?.let {
                                ctx.source.sendFailure(Component.literal("The player has to be offline"))
                                return@executes CommandResult.ERROR.value
                            }

                            val handler = (server.playerList as PlayerListAccessor).`daisy$getPlayerIo`()
                            val nbt = handler.`daisy$getNbt`(player.id)

                            val list = nbt.getList("Pos", Tag.TAG_DOUBLE.toInt())

                            val x = list.getDouble(0)
                            val y = list.getDouble(1)
                            val z = list.getDouble(2)

                            val blockPos = BlockPos(x.toInt(), y.toInt(), z.toInt())

                            val text = Component.literal("${player.name} was last seen at ")
                                .append(
                                    Component.literal("[${blockPos.x}, ${blockPos.y}, ${blockPos.z}]")
                                        .withStyle({
                                            it
                                                .withClickEvent(
                                                    ClickEvent(
                                                        ClickEvent.Action.RUN_COMMAND,
                                                        "/tp ${blockPos.x} ${blockPos.y} ${blockPos.z}"
                                                    )
                                                )
                                                .withColor(ChatFormatting.GREEN)
                                                .withHoverEvent(
                                                    HoverEvent(
                                                        HoverEvent.Action.SHOW_TEXT,
                                                        Component.literal("Click to be teleported")
                                                    )
                                                )
                                        })
                                )

                            ctx.source.sendSuccess({ text }, false)

                            return@executes CommandResult.SUCCESS.value
                        })

        )
    }
}
