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

package com.macuguita.daisy.mixin.admin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.macuguita.daisy.DaisyTweaks;
import com.macuguita.daisy.admin.CustomPlayerSaveHandler;
import com.macuguita.daisy.teleports.HomeLocation;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.DataResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PlayerSaveHandler;
import net.minecraft.world.World;

@Mixin(PlayerSaveHandler.class)
public class PlayerSaveHandlerMixin implements CustomPlayerSaveHandler {

	@Shadow
	@Final
	protected DataFixer dataFixer;
	@Shadow
	@Final
	private File playerDataDir;

	@Override
	public DataResult<NbtCompound> daisy$edit(UUID uuid, Consumer<NbtCompound> editor) {
		NbtCompound tag;

		try {
			File file = new File(this.playerDataDir, uuid.toString() + ".dat");
			if (file.exists() && file.isFile()) {
				tag = NbtIo.readCompressed(file.toPath(), NbtSizeTracker.ofUnlimitedBytes());
			} else {
				return DataResult.error(() -> "Player data file for " + uuid + " does not exist");
			}
		} catch (Exception var4) {
			return DataResult.error(() -> "Failed to load player data for " + uuid);
		}

		int i = NbtHelper.getDataVersion(tag, -1);
		tag = DataFixTypes.PLAYER.update(this.dataFixer, tag, i);

		editor.accept(tag);
		try {
			Path path = this.playerDataDir.toPath();
			Path path2 = Files.createTempFile(path, uuid + "-", ".dat");
			NbtIo.writeCompressed(tag, path2);
			Path path3 = path.resolve(uuid + ".dat");
			Path path4 = path.resolve(uuid + ".dat_old");
			Util.backupAndReplace(path3, path2, path4);
		} catch (Exception var6) {
			return DataResult.error(() -> "Failed to save player data for " + uuid);
		}
		return DataResult.success(tag);
	}

	@Override
	public BlockPos daisy$getPos(UUID uuid) {
		NbtCompound tag;

		try {
			File file = new File(this.playerDataDir, uuid.toString() + ".dat");
			if (file.exists() && file.isFile()) {
				tag = NbtIo.readCompressed(file.toPath(), NbtSizeTracker.ofUnlimitedBytes());
			} else {
				DaisyTweaks.LOGGER.error("Player data file for " + uuid + " does not exist");
				return null;
			}
		} catch (Exception var4) {
			DaisyTweaks.LOGGER.error("Failed to load player data for " + uuid);
			return null;
		}

		int i = NbtHelper.getDataVersion(tag, -1);
		tag = DataFixTypes.PLAYER.update(this.dataFixer, tag, i);

		NbtList list = tag.getList("Pos", NbtElement.DOUBLE_TYPE);

		double X = list.getDouble(0);
		double Y = list.getDouble(1);
		double Z = list.getDouble(2);

		return new BlockPos((int) X, (int) Y, (int) Z);
	}

	@Override
	public Map<String, HomeLocation> daisy$getHomes(UUID uuid) {
		NbtCompound tag;

		try {
			File file = new File(this.playerDataDir, uuid.toString() + ".dat");
			if (file.exists() && file.isFile()) {
				tag = NbtIo.readCompressed(file.toPath(), NbtSizeTracker.ofUnlimitedBytes());
			} else {
				DaisyTweaks.LOGGER.error("Player data file for " + uuid + " does not exist");
				return null;
			}
		} catch (Exception var4) {
			DaisyTweaks.LOGGER.error("Failed to load player data for " + uuid);
			return null;
		}

		int i = NbtHelper.getDataVersion(tag, -1);
		tag = DataFixTypes.PLAYER.update(this.dataFixer, tag, i);
		Map<String, HomeLocation> homeList = new HashMap<>();

		NbtCompound componentsNbt = tag.getCompound("cardinal_components");
		if (componentsNbt.contains("daisy:homes", NbtElement.COMPOUND_TYPE)) {
			NbtCompound homes = componentsNbt.getCompound("daisy:homes");
			NbtList homesNbtList = homes.getList("Homes", NbtElement.COMPOUND_TYPE);

			for (int j = 0; j < homesNbtList.size(); j++) {
				NbtCompound homeTag = homesNbtList.getCompound(j);
				String name = homeTag.getString("Name");
				int x = homeTag.getInt("X");
				int y = homeTag.getInt("Y");
				int z = homeTag.getInt("Z");
				String dimensionId = homeTag.getString("Dimension");
				RegistryKey<World> dimension = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dimensionId));
				homeList.put(name, new HomeLocation(new BlockPos(x, y, z), dimension));
			}
		} else {
			DaisyTweaks.LOGGER.error("Could not find component nbt for " + uuid);
		}
		return homeList;
	}
}
