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

import com.macuguita.daisy.DaisyTweaks;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentInitializer;

public class DaisyComponents implements EntityComponentInitializer, ScoreboardComponentInitializer {

	public static final ComponentKey<HomesComponent> HOMES_COMPONENT =
			ComponentRegistry.getOrCreate(DaisyTweaks.id("homes"), HomesComponent.class);

	public static final ComponentKey<WarpsComponent> WARPS_COMPONENT =
			ComponentRegistry.getOrCreate(DaisyTweaks.id("warps"), WarpsComponent.class);

	//public static final ComponentKey<WelcomeComponent> WELCOME_COMPONENT =
	//		ComponentRegistry.getOrCreate(DaisyTweaks.id("welcome"), WelcomeComponent.class);

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry entityComponentFactoryRegistry) {
		entityComponentFactoryRegistry.registerForPlayers(
				DaisyComponents.HOMES_COMPONENT,
				HomesComponent::new,
				RespawnCopyStrategy.CHARACTER
		);
	}

	@Override
	public void registerScoreboardComponentFactories(ScoreboardComponentFactoryRegistry scoreboardComponentFactoryRegistry) {
		scoreboardComponentFactoryRegistry.registerScoreboardComponent(
				DaisyComponents.WARPS_COMPONENT,
				WarpsComponent::new
		);
	}
}
