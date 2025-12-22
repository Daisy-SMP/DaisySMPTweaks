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

package com.macuguita.daisy_smp.common.commands.home

import com.macuguita.daisy_smp.common.attachments.HomeData
import com.macuguita.daisy_smp.common.attachments.Homes
import com.macuguita.daisy_smp.common.commands.CommandRegistrator
import com.macuguita.daisy_smp.common.commands.CommandResult
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object DelHomeCommand : CommandRegistrator {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            literal("delhome")
                .then(
                    Commands.argument("name", StringArgumentType.word())
                        .suggests { context, builder -> HomeUtils.suggestHomes(context, builder) }
                        .executes { ctx ->
                            val player = ctx.source.playerOrException
                            val name = StringArgumentType.getString(ctx, "name")
                            CommandResult.fromBoolean(deleteHome(player, name)).value
                        }
                )
        )
    }

    private fun deleteHome(player: ServerPlayer, name: String): Boolean {
        val homeData: HomeData = Homes.get(player)
        val n = name.lowercase()
        val homeExists = homeData.homes.any { it.name == n }

        if (!homeExists) {
            player.sendSystemMessage(Component.literal("Home '$name' does not exist.").withStyle(ChatFormatting.RED))
            return false
        }

        homeData.removeHome(n)
        player.sendSystemMessage(Component.literal("Home '$name' has been removed."))
        return true
    }
}
