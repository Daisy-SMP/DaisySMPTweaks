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

@file:Suppress("UnstableApiUsage")

package com.macuguita.daisy_smp.common.attachments

import com.macuguita.daisy_smp.common.DaisySMPTweaks.id
import com.mojang.serialization.Codec
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget
import net.fabricmc.fabric.api.attachment.v1.AttachmentType

object StarterItems {

    val ATTACHMENT: AttachmentType<GivenStarterItemsAttachedData> =
        AttachmentRegistry.create(
            id("starter_items")
        ) { builder ->
            builder
                .initializer { GivenStarterItemsAttachedData.DEFAULT }
                .persistent(GivenStarterItemsAttachedData.CODEC)
                .copyOnDeath()
        }

    fun get(target: AttachmentTarget): StarterItemsData =
        StarterItemsData(target)
}

data class GivenStarterItemsAttachedData(
    val givenPokeballs: Boolean
) {
    companion object {
        val DEFAULT = GivenStarterItemsAttachedData(false)

        val CODEC: Codec<GivenStarterItemsAttachedData> =
            Codec.BOOL.xmap(::GivenStarterItemsAttachedData, GivenStarterItemsAttachedData::givenPokeballs)
    }

    fun markGiven(): GivenStarterItemsAttachedData =
        copy(givenPokeballs = true)
}

data class StarterItemsData(private val target: AttachmentTarget) {

    private fun current(): GivenStarterItemsAttachedData =
        target.getAttachedOrElse(StarterItems.ATTACHMENT, GivenStarterItemsAttachedData.DEFAULT)

    var givenPokeballs: Boolean
        get() = current().givenPokeballs
        set(value) {
            target.setAttached(StarterItems.ATTACHMENT, current().copy(givenPokeballs = value))
        }

    fun markGiven() {
        target.setAttached(StarterItems.ATTACHMENT, current().markGiven())
    }
}
