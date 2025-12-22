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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.SERVER)
@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
	@Unique
	private final Random random = new Random();
	@Unique
	private Vec3 lastPosition = Vec3.ZERO;
	@Unique
	private int teleportCooldown = 0;

	@Inject(
			method = "checkMovementStatistics",
			at = @At("HEAD")
	)
	private void daisy$onPositionCheck(double dx, double dy, double dz, CallbackInfo ci) {
		ServerPlayer player = (ServerPlayer) (Object) this;

		if (ConnectionManager.INSTANCE.shouldManage(player.getUUID())) {
			Vec3 currentPos = player.position();

			double movementDistance = currentPos.distanceTo(lastPosition);
			lastPosition = currentPos;

			if (teleportCooldown > 0) {
				teleportCooldown--;
			}

			if (movementDistance > 0.01 && teleportCooldown == 0) {
				if (random.nextFloat() < 0.15f) {
					Vec3 velocity = player.getDeltaMovement();
					double speed = velocity.length();

					if (speed > 0.05) {
						double pullback = 0.05 + random.nextDouble() * 0.15;

						Vec3 direction = velocity.normalize();

						double newX = currentPos.x - direction.x * pullback;
						double newY = currentPos.y;
						double newZ = currentPos.z - direction.z * pullback;

						player.connection.teleport(newX, newY, newZ, player.getYRot(), player.getXRot());
						teleportCooldown = 40 + random.nextInt(40);
					}
				}
			}
		}
	}
}
