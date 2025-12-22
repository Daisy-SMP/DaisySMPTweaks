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

package com.macuguita.daisy_smp.common

import com.cobblemon.mod.common.CobblemonItems
import com.macuguita.daisy_smp.common.attachments.Homes
import com.macuguita.daisy_smp.common.attachments.StarterItems
import com.macuguita.daisy_smp.common.commands.CommandRegistrator
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameRules
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object DaisySMPTweaks : ModInitializer {
    const val MOD_ID: String = "daisy_tweaks"
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    fun id(name: String) = ResourceLocation.fromNamespaceAndPath(MOD_ID, name)

    val REQUEST_EXPIRY_MS: GameRules.Key<GameRules.IntegerValue?> = GameRuleRegistry.register(
        "tpaRequestTimeoutMilliSeconds",
        GameRules.Category.MISC, GameRuleFactory.createIntRule(60_000)
    )

    override fun onInitialize() {
        Homes
        StarterItems
        registerEvents()
    }

    fun registerEvents() {
        ServerPlayConnectionEvents.JOIN.register { handler, _, server ->
            val player = handler.player
            val attachedData = StarterItems.get(player)
            if (!attachedData.givenPokeballs) {
                givePokeballs(player)
                givePokedex(player)
                server.playerList.broadcastSystemMessage(
                    Component.literal(player.name.string + " has joined for the first time, say hi!")
                        .withStyle(ChatFormatting.YELLOW), false
                )
                attachedData.markGiven()
            }
        }
        CommandRegistrationCallback.EVENT.register(CommandRegistrator.RegisterCommands())
    }

    fun giveStacks(player: ServerPlayer, stack: ItemStack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, true)
        }
    }

    fun givePokeballs(player: ServerPlayer) {
        val pokeballs = numToPokeball(player.uuid.hashCode() % 6, 10)
        giveStacks(player, pokeballs)
    }

    fun givePokedex(player: ServerPlayer) {
        val pokedex = numToPokedex(player.uuid.hashCode() % 6)
        giveStacks(player, pokedex)
    }

    fun numToPokedex(num: Int): ItemStack {
        return when (num) {
            0 -> ItemStack(CobblemonItems.POKEDEX_RED)
            1 -> ItemStack(CobblemonItems.POKEDEX_YELLOW)
            2 -> ItemStack(CobblemonItems.POKEDEX_BLUE)
            3 -> ItemStack(CobblemonItems.POKEDEX_PINK)
            4 -> ItemStack(CobblemonItems.POKEDEX_BLACK)
            5 -> ItemStack(CobblemonItems.POKEDEX_WHITE)
            else -> ItemStack(CobblemonItems.POKEDEX_RED)
        }
    }

    fun numToPokeball(num: Int, quantity: Int = 1): ItemStack {
        return when (num) {
            0 -> ItemStack(CobblemonItems.POKE_BALL, quantity)
            1 -> ItemStack(CobblemonItems.CITRINE_BALL, quantity)
            2 -> ItemStack(CobblemonItems.AZURE_BALL, quantity)
            3 -> ItemStack(CobblemonItems.ROSEATE_BALL, quantity)
            4 -> ItemStack(CobblemonItems.SLATE_BALL, quantity)
            5 -> ItemStack(CobblemonItems.VERDANT_BALL, quantity)
            else -> ItemStack(CobblemonItems.POKE_BALL, quantity)
        }
    }
}
