package com.hbm.inventory.recipes;

import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class WaffleIronRecipes extends GenericRecipes<WaffleIronRecipe> {
	public static final WaffleIronRecipes INSTANCE = new WaffleIronRecipes();

	@Override
	public int inputItemLimit() {
		return 1;
	}
	@Override
	public int inputFluidLimit() {
		return 0;
	}
	@Override
	public int outputItemLimit() {
		return 1;
	}
	@Override
	public int outputFluidLimit() {
		return 0;
	}


	@Override
	public String getFileName() {
		return "hbmWaffleIron.json";
	}
	@Override
	public WaffleIronRecipe instantiateRecipe(String name) {
		return new WaffleIronRecipe(name);
	}

	@Override
	public void registerDefaults() {
		this.register((WaffleIronRecipe) new WaffleIronRecipe("waffles.waffle")
			.inputItems(new ComparableStack(Items.wheat))
			.outputItems(new ItemStack(Items.bread))
		);
	}
}
