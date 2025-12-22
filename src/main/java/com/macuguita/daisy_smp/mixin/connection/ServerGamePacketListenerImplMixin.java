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

package com.macuguita.daisy_smp.mixin.connection;

import java.util.Random;

import com.macuguita.daisy_smp.server.ConnectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.SERVER)
@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

	@Unique
	private final Random random = new Random();
	@Shadow
	public ServerPlayer player;
	@Shadow
	private int tickCount;

	@Inject(
			method = "tick",
			at = @At("HEAD")
	)
	private void daisy$onTick(CallbackInfo ci) {
		if (ConnectionManager.INSTANCE.shouldManage(player.getUUID())) {
			tickCount++;

			if (tickCount % 20 == 0) {
				return;
			}

			if (random.nextFloat() < 0.15f) {
				try {
					Thread.sleep(10 + random.nextInt(40));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	@Inject(
			method = "handleUseItemOn",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V",
					shift = At.Shift.AFTER
			),
			cancellable = true
	)
	private void daisy$onBlockPlaceAttempt(CallbackInfo ci) {
		if (ConnectionManager.INSTANCE.shouldManage(player.getUUID())) {
			if (random.nextFloat() < 0.25f) {
				ci.cancel();
			}
		}
	}

	@Inject(
			method = "handleMovePlayer",
			at = @At("HEAD"),
			cancellable = true
	)
	private void daisy$onMovementPacket(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
		if (ConnectionManager.INSTANCE.shouldManage(player.getUUID())) {
			if (random.nextFloat() < 0.3f) {
				ci.cancel();
			}

			if (random.nextFloat() < 0.15f) {
				try {
					Thread.sleep(50 + random.nextInt(100));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	@Inject(
			method = "handlePaddleBoat",
			at = @At("HEAD"),
			cancellable = true
	)
	private void daisy$onPaddleBoatPacket(ServerboundPaddleBoatPacket packet, CallbackInfo ci) {
		if (ConnectionManager.INSTANCE.shouldManage(player.getUUID())) {
			if (random.nextFloat() < 0.5f) {
				ci.cancel();
			}

			if (random.nextFloat() < 0.45f) {
				try {
					Thread.sleep(50 + random.nextInt(100));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	@Inject(
			method = "handleContainerClick",
			at = @At("HEAD"),
			cancellable = true
	)
	private void daisy$onContainerClickPacket(ServerboundContainerClickPacket packet, CallbackInfo ci) {
		if (ConnectionManager.INSTANCE.shouldManage(player.getUUID())) {
			if (random.nextFloat() < 0.15f) {
				ci.cancel();
			}

			if (random.nextFloat() < 0.25f) {
				try {
					Thread.sleep(50 + random.nextInt(100));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	@Inject(
			method = "handleContainerClose",
			at = @At("HEAD"),
			cancellable = true
	)
	private void daisy$onContainerClosePacket(ServerboundContainerClosePacket packet, CallbackInfo ci) {
		if (ConnectionManager.INSTANCE.shouldManage(player.getUUID())) {
			if (random.nextFloat() < 0.05f) {
				ci.cancel();
			}

			if (random.nextFloat() < 0.25f) {
				try {
					Thread.sleep(50 + random.nextInt(100));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	@Inject(
			method = "handlePickItem",
			at = @At("HEAD"),
			cancellable = true
	)
	private void daisy$onPickItemPacket(ServerboundPickItemPacket packet, CallbackInfo ci) {
		if (ConnectionManager.INSTANCE.shouldManage(player.getUUID())) {
			if (random.nextFloat() < 0.05f) {
				ci.cancel();
			}

			if (random.nextFloat() < 0.65f) {
				try {
					Thread.sleep(50 + random.nextInt(100));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	@Inject(
			method = "handleMoveVehicle",
			at = @At("HEAD"),
			cancellable = true
	)
	private void daisy$onVehicleMovementPacket(ServerboundMoveVehiclePacket packet, CallbackInfo ci) {
		if (ConnectionManager.INSTANCE.shouldManage(player.getUUID())) {
			if (random.nextFloat() < 0.3f) {
				ci.cancel();
			}

			if (random.nextFloat() < 0.15f) {
				try {
					Thread.sleep(50 + random.nextInt(100));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	@Inject(
			method = "handleContainerClick",
			at = @At("HEAD"),
			cancellable = true
	)
	private void daisy$onInventoryClick(CallbackInfo ci) {
		if (ConnectionManager.INSTANCE.shouldManage(player.getUUID())) {
			if (random.nextFloat() < 0.3f) {
				ci.cancel();
			}

			if (random.nextFloat() < 0.3f) {
				try {
					Thread.sleep(50 + random.nextInt(150));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
}
