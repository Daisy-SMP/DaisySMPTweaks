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

package com.macuguita.daisy.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class PrizeBagItem extends Item {

	private final Identifier lootTableId;

	public PrizeBagItem(Settings settings, Identifier lootTableId) {
		super(settings);
		this.lootTableId = lootTableId;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		RegistryKey<LootTable> lootTableKey = RegistryKey.of(RegistryKeys.LOOT_TABLE, lootTableId);
		if (!world.isClient && world instanceof ServerWorld serverWorld) {
			LootTable lootTable = serverWorld.getServer().getReloadableRegistries().getLootTable(lootTableKey);

			LootContextParameterSet parameters = new LootContextParameterSet.Builder(serverWorld)
					.add(LootContextParameters.ORIGIN, user.getPos())
					.add(LootContextParameters.THIS_ENTITY, user)
					.build(LootContextTypes.GIFT);

			List<ItemStack> loot = lootTable.generateLoot(parameters);

			for (ItemStack stack : loot) {
				if (!user.getInventory().insertStack(stack)) {
					user.dropItem(stack, true);
				}
			}

			if (!user.getAbilities().creativeMode) user.getStackInHand(hand).decrement(1);
			return TypedActionResult.success(user.getStackInHand(hand), true);
		}

		return TypedActionResult.pass(user.getStackInHand(hand));
	}
}
