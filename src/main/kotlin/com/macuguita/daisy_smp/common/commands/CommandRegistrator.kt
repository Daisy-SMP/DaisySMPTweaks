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

package com.macuguita.daisy_smp.common.commands

import com.macuguita.daisy_smp.common.commands.admin.OfflinePlayerPosCommand
import com.macuguita.daisy_smp.common.commands.admin.OfflineTpCommand
import com.macuguita.daisy_smp.common.commands.home.DelHomeCommand
import com.macuguita.daisy_smp.common.commands.home.HomeCommand
import com.macuguita.daisy_smp.common.commands.home.SetHomeCommand
import com.macuguita.daisy_smp.common.commands.home.SetMaxHomesCommand
import com.macuguita.daisy_smp.common.commands.spawn.SpawnCommand
import com.macuguita.daisy_smp.common.commands.tpa.TpaAcceptCommand
import com.macuguita.daisy_smp.common.commands.tpa.TpaCommand
import com.macuguita.daisy_smp.common.commands.tpa.TpaHereCommand
import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

interface CommandRegistrator {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>)

    class RegisterCommands : CommandRegistrationCallback {
        override fun register(
            dispatcher: CommandDispatcher<CommandSourceStack>,
            registryAccess: CommandBuildContext,
            environment: Commands.CommandSelection
        ) {
            OfflinePlayerPosCommand.register(dispatcher)
            OfflineTpCommand.register(dispatcher)
            DelHomeCommand.register(dispatcher)
            HomeCommand.register(dispatcher)
            SetHomeCommand.register(dispatcher)
            SetMaxHomesCommand.register(dispatcher)
            SpawnCommand.register(dispatcher)
            TpaCommand.register(dispatcher)
            TpaHereCommand.register(dispatcher)
            TpaAcceptCommand.register(dispatcher)
        }
    }
}
