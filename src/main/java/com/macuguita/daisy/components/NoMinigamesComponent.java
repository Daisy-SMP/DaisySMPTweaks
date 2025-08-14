/*
 * Copyright (c) 2025 macuguita.
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.macuguita.daisy.components;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.Component;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;

public class NoMinigamesComponent implements Component {

	private final List<UUID> disabledPlayers = new ArrayList<>();
	private final Scoreboard provider;

	public NoMinigamesComponent(Scoreboard provider, @Nullable MinecraftServer server) {
		this.provider = provider;
	}

	public List<UUID> getDisabledPlayers() {
		return disabledPlayers;
	}

	public boolean addDisabledPlayer(UUID player) {
		if (player != null) {
			return disabledPlayers.add(player);
		} else {
			return false;
		}
	}

	public boolean removeDisabledPlayer(UUID player) {
		if (player != null) {
			return disabledPlayers.remove(player);
		} else {
			return false;
		}
	}

	@Override
	public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
		disabledPlayers.clear();
		NbtList disabledPlayersNbtList = nbtCompound.getList("DisabledPlayers", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < disabledPlayersNbtList.size(); i++) {
			NbtCompound disabledPlayerTag = disabledPlayersNbtList.getCompound(i);
			String playerUUID = disabledPlayerTag.getString("UUID");
			disabledPlayers.add(UUID.fromString(playerUUID));
		}
	}

	@Override
	public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
		NbtList disabledPlayersNbtList = new NbtList();
		for (UUID uuid : disabledPlayers) {
			NbtCompound disabledPlayerTag = new NbtCompound();
			disabledPlayerTag.putString("UUID", uuid.toString());
			disabledPlayersNbtList.add(disabledPlayerTag);
		}
		nbtCompound.put("DisabledPlayers", disabledPlayersNbtList);
	}
}
