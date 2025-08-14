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
import org.ladysnake.cca.api.v3.component.Component;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HomesComponent implements Component {

	private final PlayerEntity player;
	private final Map<String, HomeLocation> homeList = new HashMap<>();
	private int maxHomes = 3;

	public HomesComponent(PlayerEntity player) {
		this.player = player;
	}

	public void addHome(String name, BlockPos pos, RegistryKey<World> dimension) {
		homeList.put(name, new HomeLocation(pos, dimension));
	}

	public HomeLocation getHome(String name) {
		return homeList.get(name);
	}

	public Map<String, HomeLocation> getAllHomes() {
		return new HashMap<>(homeList);
	}

	public boolean removeHome(String name) {
		return homeList.remove(name) != null;
	}

	public int getMaxHomes() {
		return maxHomes;
	}

	public void setMaxHomes(int maxHomes) {
		this.maxHomes = maxHomes;
	}

	@Override
	public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
		this.maxHomes = nbtCompound.getInt("MaxHomes");
		homeList.clear();
		NbtList homesNbtList = nbtCompound.getList("Homes", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < homesNbtList.size(); i++) {
			NbtCompound homeTag = homesNbtList.getCompound(i);
			String name = homeTag.getString("Name");
			int x = homeTag.getInt("X");
			int y = homeTag.getInt("Y");
			int z = homeTag.getInt("Z");
			String dimensionId = homeTag.getString("Dimension");
			RegistryKey<World> dimension = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dimensionId));
			homeList.put(name, new HomeLocation(new BlockPos(x, y, z), dimension));
		}

	}

	@Override
	public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
		nbtCompound.putInt("MaxHomes", this.maxHomes);
		NbtList homesNbtList = new NbtList();
		for (Map.Entry<String, HomeLocation> entry : homeList.entrySet()) {
			NbtCompound homeTag = new NbtCompound();
			homeTag.putString("Name", entry.getKey());
			BlockPos pos = entry.getValue().getPosition();
			homeTag.putInt("X", pos.getX());
			homeTag.putInt("Y", pos.getY());
			homeTag.putInt("Z", pos.getZ());
			homeTag.putString("Dimension", entry.getValue().getDimension().getValue().toString());
			homesNbtList.add(homeTag);
		}
		nbtCompound.put("Homes", homesNbtList);
	}
}
