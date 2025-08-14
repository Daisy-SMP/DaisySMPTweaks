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

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public class DaisyObjects {

	public static final Item PRIZE_BAG = register("prize_bag", new PrizeBagItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE), DaisyTweaks.id("prize/prize")));

	public static final Item COPPER_DAISY_COIN = register("copper_daisy_coin", new DaisyCoinItem(new Item.Settings().rarity(Rarity.COMMON)));
	public static final Item GOLD_DAISY_COIN = register("gold_daisy_coin", new DaisyCoinItem(new Item.Settings().rarity(Rarity.UNCOMMON)));
	public static final Item DIAMOND_DAISY_COIN = register("diamond_daisy_coin", new DaisyCoinItem(new Item.Settings().rarity(Rarity.RARE)));
	public static final Item NETHERITE_DAISY_COIN = register("netherite_daisy_coin", new DaisyCoinItem(new Item.Settings().rarity(Rarity.EPIC)));

	private static Item register(String name, Item item) {
		return Registry.register(Registries.ITEM, DaisyTweaks.id(name), item);
	}

	public static void init() {

	}
}
