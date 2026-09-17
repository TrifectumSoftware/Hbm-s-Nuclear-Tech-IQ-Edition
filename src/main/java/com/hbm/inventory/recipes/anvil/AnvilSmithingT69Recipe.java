package com.hbm.inventory.recipes.anvil;

import com.hbm.config.GeneralConfig;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.items.special.ItemHot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Arrays;
import java.util.List;

public class AnvilSmithingT69Recipe extends AnvilSmithingRecipe {
	public AnvilSmithingT69Recipe(int tier, ItemStack out, AStack left, AStack right) {
		super(tier, out, left, right);
	}

	public boolean doesStackMatch(ItemStack input, AStack recipe) {
		if (!input.getDisplayName().equalsIgnoreCase("tam")) return false;

		return recipe.matchesRecipe(input, false);
	}
}
