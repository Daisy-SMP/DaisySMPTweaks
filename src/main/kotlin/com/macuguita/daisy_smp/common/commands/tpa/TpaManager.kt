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

package com.macuguita.daisy_smp.common.commands.tpa

import java.util.*

object TpaManager {
    private val requests = mutableMapOf<UUID, MutableList<TpaRequest>>()

    fun sendRequest(request: TpaRequest, expiryMs: Int?): Boolean {
        val list = requests.getOrPut(request.target) { mutableListOf() }

        if (list.any { it.requester == request.requester && !isExpired(it, expiryMs) }) {
            return false
        }

        list.add(request)
        return true
    }

    fun popMostRecent(target: UUID, expiryMs: Int?): TpaRequest? {
        cleanup(target, expiryMs)
        return requests[target]?.maxByOrNull { it.timestamp }
            ?.also { requests[target]?.remove(it) }
    }

    fun popFromRequester(target: UUID, requester: UUID, expiryMs: Int?): TpaRequest? {
        cleanup(target, expiryMs)
        val list = requests[target] ?: return null
        val req = list.find { it.requester == requester }
        req?.let { list.remove(it) }
        return req
    }

    private fun cleanup(target: UUID, expiryMs: Int?) {
        requests[target]?.removeIf { isExpired(it, expiryMs) }
        if (requests[target]?.isEmpty() == true) {
            requests.remove(target)
        }
    }

    private fun isExpired(req: TpaRequest, expiryMs: Int?): Boolean {
        return System.currentTimeMillis() - req.timestamp > (expiryMs ?: 60_000)
    }
}

