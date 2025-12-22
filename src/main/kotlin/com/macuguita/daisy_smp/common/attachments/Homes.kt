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
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level

object Homes {

    val ATTACHMENT: AttachmentType<HomeAttachedData> =
        AttachmentRegistry.create(
            id("homes")
        ) { builder ->
            builder
                .initializer({ HomeAttachedData.DEFAULT })
                .persistent(HomeAttachedData.CODEC)
                .copyOnDeath()
        }

    fun get(target: AttachmentTarget): HomeData =
        HomeData(target)
}

data class HomeAttachedData(
    val homes: List<Home>,
    val maxHomes: Int = 1
) {
    companion object {
        val DEFAULT = HomeAttachedData(emptyList())

        val CODEC: Codec<HomeAttachedData> =
            RecordCodecBuilder.create { i ->
                i.group(
                    Home.CODEC.listOf()
                        .optionalFieldOf("homes", emptyList())
                        .forGetter { it.homes },
                    Codec.INT
                        .optionalFieldOf("max_homes", 1)
                        .forGetter { it.maxHomes }
                ).apply(i, ::HomeAttachedData)
            }
    }

    fun addHome(name: String, pos: BlockPos, dimension: ResourceKey<Level>): HomeAttachedData {
        val n = name.lowercase()
        if (homes.size >= maxHomes) return this
        if (homes.any { it.name == n }) return this

        return copy(homes = homes + Home(n, pos, dimension))
    }

    fun removeHome(name: String): HomeAttachedData {
        val n = name.lowercase()
        return copy(homes = homes.filterNot { it.name == n })
    }
}

data class HomeData(private val target: AttachmentTarget) {

    private fun current(): HomeAttachedData =
        target.getAttachedOrElse(Homes.ATTACHMENT, HomeAttachedData.DEFAULT)

    val homes: List<Home>
        get() = current().homes

    var maxHomes: Int
        get() = current().maxHomes
        set(value) {
            target.setAttached(
                Homes.ATTACHMENT,
                current().copy(maxHomes = value)
            )
        }

    fun addHome(name: String, pos: BlockPos, dimension: ResourceKey<Level>) {
        target.setAttached(
            Homes.ATTACHMENT,
            current().addHome(name, pos, dimension)
        )
    }

    fun removeHome(name: String) {
        target.setAttached(
            Homes.ATTACHMENT,
            current().removeHome(name)
        )
    }
}

data class Home(val name: String, val position: BlockPos, val dimension: ResourceKey<Level>) {
    companion object {
        private val LEVEL_CODEC = ResourceLocation.CODEC.xmap<ResourceKey<Level>>(
            { rl -> ResourceKey.create(Registries.DIMENSION, rl) },
            ResourceKey<Level>::location
        )

        val CODEC: Codec<Home> = RecordCodecBuilder.create { i ->
            i.group(
                Codec.STRING.fieldOf("name").forGetter { it.name },
                BlockPos.CODEC.fieldOf("block_pos").forGetter { it.position },
                LEVEL_CODEC.optionalFieldOf("dimension", Level.OVERWORLD).forGetter { it.dimension },
            ).apply(i, ::Home)
        }
    }
}
