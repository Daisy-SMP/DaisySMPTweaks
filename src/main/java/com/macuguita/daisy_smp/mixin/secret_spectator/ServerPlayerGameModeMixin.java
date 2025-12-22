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

package com.macuguita.daisy_smp.mixin.secret_spectator;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.macuguita.daisy_smp.server.SecretSpectator;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.players.PlayerList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Debug(export = true)
@Environment(EnvType.SERVER)
@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

	@Shadow
	@Final
	protected ServerPlayer player;

	@WrapOperation(
			method = "changeGameModeForPlayer",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"
			)
	)
	private void daisy$wrapChangeGameMode(
			PlayerList playerList,
			Packet<?> packet,
			Operation<Void> original
	) {
		if (!(packet instanceof ClientboundPlayerInfoUpdatePacket info)) {
			original.call(playerList, packet);
			return;
		}

		for (ServerPlayer receiver : playerList.getPlayers()) {
			ClientboundPlayerInfoUpdatePacket filtered =
					SecretSpectator.INSTANCE.filterPacketForReceiver(receiver, info);
			receiver.connection.send(filtered);
		}
	}
}
