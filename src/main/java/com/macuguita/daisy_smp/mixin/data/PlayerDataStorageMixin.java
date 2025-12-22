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

package com.macuguita.daisy_smp.mixin.data;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;

import com.macuguita.daisy_smp.common.DaisySMPTweaks;
import com.macuguita.daisy_smp.injected_interfaces.CustomPlayerDataStorage;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.DataResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.storage.PlayerDataStorage;

@Mixin(PlayerDataStorage.class)
public class PlayerDataStorageMixin implements CustomPlayerDataStorage {

	@Shadow
	@Final
	protected DataFixer fixerUpper;
	@Shadow
	@Final
	private File playerDir;

	@Override
	public DataResult<CompoundTag> daisy$edit(UUID uuid, Consumer<CompoundTag> editor) {
		CompoundTag tag;

		try {
			File file = new File(this.playerDir, uuid.toString() + ".dat");
			if (file.exists() && file.isFile()) {
				tag = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
			} else {
				return DataResult.error(() -> "Player data file for " + uuid + " does not exist");
			}
		} catch (Exception var4) {
			return DataResult.error(() -> "Failed to load player data for " + uuid);
		}

		int i = NbtUtils.getDataVersion(tag, -1);
		tag = DataFixTypes.PLAYER.updateToCurrentVersion(this.fixerUpper, tag, i);

		editor.accept(tag);
		try {
			Path path = this.playerDir.toPath();
			Path path2 = Files.createTempFile(path, uuid + "-", ".dat");
			NbtIo.writeCompressed(tag, path2);
			Path path3 = path.resolve(uuid + ".dat");
			Path path4 = path.resolve(uuid + ".dat_old");
			Util.safeReplaceFile(path3, path2, path4);
		} catch (Exception var6) {
			return DataResult.error(() -> "Failed to save player data for " + uuid);
		}
		return DataResult.success(tag);
	}

	@Override
	public CompoundTag daisy$getNbt(UUID uuid) {
		CompoundTag tag;

		try {
			File file = new File(this.playerDir, uuid.toString() + ".dat");
			if (file.exists() && file.isFile()) {
				tag = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
			} else {
				DaisySMPTweaks.INSTANCE.getLOGGER().error("Player data file for " + uuid + " does not exist");
				return null;
			}
		} catch (Exception var4) {
			DaisySMPTweaks.INSTANCE.getLOGGER().error("Failed to load player data for " + uuid);
			return null;
		}

		int i = NbtUtils.getDataVersion(tag, -1);
		return DataFixTypes.PLAYER.updateToCurrentVersion(this.fixerUpper, tag, i);
	}
}
