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

package com.macuguita.daisy.reg;

import com.macuguita.daisy.DaisyTweaks;
import com.macuguita.daisy.item.DaisyCoinItem;
import com.macuguita.daisy.item.PrizeBagItem;

import com.macuguita.lib.platform.registry.GuitaRegistries;
import com.macuguita.lib.platform.registry.GuitaRegistry;
import com.macuguita.lib.platform.registry.GuitaRegistryEntry;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Rarity;

public class DaisyObjects {

	public static final GuitaRegistry<Item> ITEMS = GuitaRegistries.create(Registries.ITEM, DaisyTweaks.MOD_ID);

	public static final GuitaRegistryEntry<Item> PRIZE_BAG = ITEMS.register("prize_bag", () -> new PrizeBagItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE), DaisyTweaks.id("prize/prize")));

	public static final GuitaRegistryEntry<Item> COPPER_DAISY_COIN = ITEMS.register("copper_daisy_coin", () -> new DaisyCoinItem(new Item.Settings().rarity(Rarity.COMMON)));
	public static final GuitaRegistryEntry<Item> GOLD_DAISY_COIN = ITEMS.register("gold_daisy_coin", () -> new DaisyCoinItem(new Item.Settings().rarity(Rarity.UNCOMMON)));
	public static final GuitaRegistryEntry<Item> DIAMOND_DAISY_COIN = ITEMS.register("diamond_daisy_coin", () -> new DaisyCoinItem(new Item.Settings().rarity(Rarity.RARE)));
	public static final GuitaRegistryEntry<Item> NETHERITE_DAISY_COIN = ITEMS.register("netherite_daisy_coin", () -> new DaisyCoinItem(new Item.Settings().rarity(Rarity.EPIC)));

	public static void init() {
		ITEMS.init();
	}
}
