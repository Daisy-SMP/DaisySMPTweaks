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

import java.util.HashMap;
import java.util.Map;

import com.macuguita.daisy.utils.HomeLocation;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.Component;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WarpsComponent implements Component {

	private final Map<String, HomeLocation> warpList = new HashMap<>();
	private final Scoreboard provider;

	public WarpsComponent(Scoreboard provider, @Nullable MinecraftServer server) {
		this.provider = provider;
	}

	public Map<String, HomeLocation> getWarpList() {
		return warpList;
	}

	public HomeLocation getWarp(String name) {
		return warpList.get(name);
	}

	public Map<String, HomeLocation> getAllWarps() {
		return new HashMap<>(warpList);
	}

	public boolean addWarp(String name, BlockPos pos, RegistryKey<World> dimension) {
		return warpList.put(name, new HomeLocation(pos, dimension)) != null;
	}

	public boolean removeWarp(String name) {
		return warpList.remove(name) != null;
	}

	@Override
	public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
		warpList.clear();
		NbtList warpsNbtList = nbtCompound.getList("Warps", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < warpsNbtList.size(); i++) {
			NbtCompound warpTag = warpsNbtList.getCompound(i);
			String name = warpTag.getString("Name");
			int x = warpTag.getInt("X");
			int y = warpTag.getInt("Y");
			int z = warpTag.getInt("Z");
			String dimensionId = warpTag.getString("Dimension");
			RegistryKey<World> dimension = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dimensionId));
			warpList.put(name, new HomeLocation(new BlockPos(x, y, z), dimension));
		}
	}

	@Override
	public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
		NbtList warpsNbtList = new NbtList();
		for (Map.Entry<String, HomeLocation> entry : warpList.entrySet()) {
			NbtCompound warpTag = new NbtCompound();
			warpTag.putString("Name", entry.getKey());
			BlockPos pos = entry.getValue().getPosition();
			warpTag.putInt("X", pos.getX());
			warpTag.putInt("Y", pos.getY());
			warpTag.putInt("Z", pos.getZ());
			warpTag.putString("Dimension", entry.getValue().getDimension().getValue().toString());
			warpsNbtList.add(warpTag);
		}
		nbtCompound.put("Warps", warpsNbtList);
	}
}
